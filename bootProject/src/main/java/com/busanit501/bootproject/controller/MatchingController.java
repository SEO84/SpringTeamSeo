package com.busanit501.bootproject.controller;

import com.busanit501.bootproject.domain.MatchingRoom;
import com.busanit501.bootproject.domain.Pet;
import com.busanit501.bootproject.domain.RoomParticipant;
import com.busanit501.bootproject.domain.User;
import com.busanit501.bootproject.dto.MatchingRoomDTO;
import com.busanit501.bootproject.exception.ResourceNotFoundException;
import com.busanit501.bootproject.repository.UserRepository;
import com.busanit501.bootproject.service.MatchingService;
import com.busanit501.bootproject.service.PetService;
import com.busanit501.bootproject.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 매칭 컨트롤러 클래스
 * 매칭방 생성, 조회, 수정, 삭제 및 참가자 관리에 대한 기능을 처리한다.
 */
@Log4j2
@Controller
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService; // 매칭방 관련 비즈니스 로직 처리
    private final PetService petService; // 반려동물 관련 비즈니스 로직 처리
    private final UserRepository userRepository; // 사용자 데이터 접근


    /**
     * 생성자 주입 방식으로 서비스와 레포지토리를 연결
     */
    @Autowired
    public MatchingController(MatchingService matchingService,
                              PetService petService,
                              UserRepository userRepository
                             ) {
        this.matchingService = matchingService;
        this.petService = petService;
        this.userRepository = userRepository;

    }

    /**
     * 매칭방 목록 페이지를 반환
     * 로그인한 사용자만 접근 가능
     */
    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
       //  로그인된 사용자 가져오기 (가정)
         User loginUser = getManagedLoginUser(session);
         if (loginUser == null) {
             return "redirect:/user/login";
         }

        // 모든 매칭 방 가져오기
        List<MatchingRoomDTO> rooms = matchingService.getAllRooms();

        model.addAttribute("rooms", rooms);
        return "matching/list";
    }


    /**
     * 매칭방 생성 폼 페이지 반환
     * 로그인된 사용자만 접근 가능
     */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        User loginUser = getManagedLoginUser(session); // 로그인된 사용자 확인
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        // 로그인한 사용자의 반려동물 리스트를 조회하여 모델에 추가
        List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
        model.addAttribute("userPets", userPets);

        // 기본값을 가진 DTO 생성
        MatchingRoomDTO dto = new MatchingRoomDTO();
        dto.setMeetingDate(LocalDate.now()); // 기본 날짜는 오늘
        dto.setMeetingTime(LocalTime.now().withSecond(0).withNano(0)); // 기본 시간
        dto.setMaxParticipants(4L); // 기본 최대 참가자 수
        model.addAttribute("matchingRoomDTO", dto);

        return "matching/create"; // 매칭방 생성 페이지 반환
    }

    /**
     * 매칭방 생성 요청을 처리
     * 폼 데이터를 검증 후 매칭방을 생성
     */
    @PostMapping("/create")
    public String createSubmit(@Valid @ModelAttribute("matchingRoomDTO") MatchingRoomDTO dto,
                               BindingResult bindingResult,
                               @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session); // 로그인된 사용자 확인
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        // 폼 유효성 검사
        if (bindingResult.hasErrors()) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            return "matching/create"; // 유효성 오류 발생 시 폼 페이지로 돌아감
        }

        try {
            // 이미지 업로드 처리 (임시로 저장 URL을 설정)
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String savedImageUrl = "/upload/" + originalFilename; // 예시 저장 경로
                dto.setImageUrl(savedImageUrl);
            }

            // 매칭방 생성 서비스 호출
            matchingService.createRoom(dto, loginUser);

            redirectAttributes.addFlashAttribute("successMessage", "매칭방이 성공적으로 생성되었습니다.");
            return "redirect:/matching/list"; // 생성 성공 후 매칭방 목록 페이지로 이동
        } catch (RuntimeException e) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            model.addAttribute("errorMessage", e.getMessage());
            return "matching/create"; // 에러 발생 시 폼 페이지로 복귀
        }
    }

    /**
     * 매칭방 상세 페이지 반환
     * 매칭방 정보를 조회하고 참가자 데이터를 모델에 추가
     */
    @GetMapping("/detail/{id}")
    public String getMatchingRoomDetail(@PathVariable("id") Long roomId,
                                        Model model, HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session); // 로그인된 사용자 확인
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            // 매칭방 및 참가자 정보 조회
            MatchingRoom room = matchingService.getRoomById(roomId);
            List<RoomParticipant> participants = matchingService.getParticipantsByRoomId(roomId);

            // 참가자 상태별로 필터링
            List<RoomParticipant> pending = matchingService.filterParticipants(
                    participants, RoomParticipant.ParticipantStatus.Pending);
            List<RoomParticipant> accepted = matchingService.filterParticipants(
                    participants, RoomParticipant.ParticipantStatus.Accepted);

            // 호스트 여부 확인
            boolean isHost = room.getHost().getUserId().equals(loginUser.getUserId());

            // 대기 상태의 참가자 Map 생성
            Map<User, List<Pet>> pendingMap = matchingService.getPendingUserPets(room);
            model.addAttribute("pendingMap", pendingMap);

            // 승인 상태의 참가자 Map 생성
            Map<User, List<Pet>> acceptedMap = matchingService.getAcceptedUserPets(room);
            model.addAttribute("acceptedMap", acceptedMap);

            // 모델에 필요한 데이터 추가
            model.addAttribute("room", room);
            model.addAttribute("pendingParticipants", pending);
            model.addAttribute("acceptedParticipants", accepted);
            model.addAttribute("isHost", isHost);

            // 호스트가 아닌 경우 반려동물 리스트 추가
            if (!isHost) {
                List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
                model.addAttribute("userPets", userPets);
            }

            return "matching/detail"; // 매칭방 상세 페이지 반환
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/matching/list"; // 에러 발생 시 목록 페이지로 이동
        }
    }

    // 이외의 메서드들도 비슷한 방식으로 상세한 주석 작성 가능

    /**
     * 매칭방 수정 폼 페이지 반환
     * 매칭방 정보와 호스트의 반려동물 목록을 모델에 추가
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long roomId,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session); // 로그인된 사용자 확인
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            // 매칭방 조회
            MatchingRoom room = matchingService.getRoomById(roomId);

            // 로그인 사용자가 호스트인지 확인
            if (!room.getHost().getUserId().equals(loginUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "방장만 수정할 수 있습니다.");
                return "redirect:/matching/detail/" + roomId;
            }

            // 매칭방 데이터를 DTO로 변환
            MatchingRoomDTO dto = matchingService.convertToDto(room);
            dto.setRoomId(roomId); // DTO에 매칭방 ID 추가

            // 호스트의 반려동물 목록 조회
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("matchingRoomDTO", dto); // 매칭방 데이터
            model.addAttribute("userPets", userPets); // 반려동물 목록

            return "matching/edit"; // 매칭방 수정 페이지 반환
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/matching/list"; // 에러 발생 시 목록 페이지로 이동
        }
    }

    /**
     * 매칭방 수정 요청 처리
     * 수정된 데이터를 검증하고 DB에 업데이트
     */
    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable("id") Long roomId,
                             @Valid @ModelAttribute("matchingRoomDTO") MatchingRoomDTO dto,
                             BindingResult bindingResult,
                             @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session); // 로그인된 사용자 확인
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        // 폼 유효성 검사
        if (bindingResult.hasErrors()) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            return "matching/edit"; // 유효성 오류 발생 시 수정 폼으로 복귀
        }

        try {
            // 이미지 파일 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String savedImageUrl = "/upload/" + originalFilename; // 임시 저장 URL
                dto.setImageUrl(savedImageUrl);
            }

            // 매칭방 수정 서비스 호출
            matchingService.updateRoom(roomId, dto, loginUser);

            redirectAttributes.addFlashAttribute("successMessage", "매칭방이 성공적으로 수정되었습니다.");
            return "redirect:/matching/detail/" + roomId; // 수정 후 상세 페이지로 이동
        } catch (RuntimeException e) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            model.addAttribute("errorMessage", e.getMessage());
            return "matching/edit"; // 에러 발생 시 수정 페이지로 복귀
        }
    }

    /**
     * 참가 신청 요청 처리
     * 사용자가 선택한 반려동물을 매칭방에 신청
     */
    @PostMapping("/apply/{roomId}")
    public String applyRoom(@PathVariable("roomId") Long roomId,
                            @RequestParam(value = "additionalPetIds", required = false) List<Long> petIds,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session); // 로그인된 사용자 확인
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            // 참가 신청 서비스 호출
            matchingService.applyRoom(roomId, loginUser.getUserId(), petIds);
            redirectAttributes.addFlashAttribute("successMessage", "참가 신청 완료!");
            return "redirect:/matching/list"; // 신청 후 목록 페이지로 이동
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/matching/detail/" + roomId; // 에러 발생 시 상세 페이지로 복귀
        }
    }

    /**
     * 참가자 승인 요청 처리
     * 호스트가 참가자를 승인
     */
    @PostMapping("/accept/{roomId}/{userId}")
    public String acceptParticipant(@PathVariable("roomId") Long roomId,
                                    @PathVariable("userId") Long userId,
                                    RedirectAttributes redirectAttributes) {
        try {
            // 참가자 승인 서비스 호출
            matchingService.acceptParticipant(roomId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "참가자가 승인되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/matching/detail/" + roomId; // 처리 후 상세 페이지로 이동
    }

    /**
     * 참가자 거절 요청 처리
     * 호스트가 참가자를 거절
     */
    @PostMapping("/reject/{roomId}/{userId}")
    public String rejectParticipant(@PathVariable("roomId") Long roomId,
                                    @PathVariable("userId") Long userId,
                                    RedirectAttributes redirectAttributes) {
        try {
            // 참가자 거절 서비스 호출
            matchingService.rejectParticipant(roomId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "참가자가 거절되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/matching/detail/" + roomId; // 처리 후 상세 페이지로 이동
    }

    /**
     * 세션에서 로그인된 사용자 가져오기
     * 로그인된 사용자 객체를 DB에서 조회하여 반환
     */
    private User getManagedLoginUser(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser"); // 세션에서 사용자 가져오기
        if (loginUser == null) {
            return null;
        }
        return userRepository.findById(loginUser.getUserId()).orElse(null); // DB에서 사용자 조회
    }
}



//    // 상세 페이지 model 세팅
//    private void populateModelForDetail(Model model, MatchingRoom room, User loginUser) {
//        model.addAttribute("room", room);
//        model.addAttribute("loginUser", loginUser);
//
//        List<RoomParticipant> participants = matchingService.getParticipantsByRoomId(room.getRoomId());
//        List<RoomParticipant> pendingParticipants =
//                matchingService.filterParticipants(participants, RoomParticipant.ParticipantStatus.Pending);
//        List<RoomParticipant> acceptedParticipants =
//                matchingService.filterParticipants(participants, RoomParticipant.ParticipantStatus.Accepted);
//
//        model.addAttribute("pendingParticipants", pendingParticipants);
//        model.addAttribute("acceptedParticipants", acceptedParticipants);
//
//        boolean isHost = room.getHost().getUserId().equals(loginUser.getUserId());
//        model.addAttribute("isHost", isHost);
//
//        if (!isHost) {
//            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
//            model.addAttribute("userPets", userPets);
//        }
//    }
