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

@Controller
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService;
    private final PetService petService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public MatchingController(MatchingService matchingService,
                              PetService petService,
                              UserRepository userRepository,
                              UserService userService) {
        this.matchingService = matchingService;
        this.petService = petService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * 매칭방 목록 페이지
     */
    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            return "redirect:/user/login";
        }
        // DB에서 엔티티 목록 조회
        List<MatchingRoom> rooms = matchingService.getAllRooms();
        model.addAttribute("rooms", rooms);
        return "matching/list";
    }

    /**
     * 매칭방 생성 폼 페이지
     */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        // 로그인 사용자의 반려동물 목록
        List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
        model.addAttribute("userPets", userPets);

        // DTO 기본값
        MatchingRoomDTO dto = new MatchingRoomDTO();
        dto.setMeetingDate(LocalDate.now());
        dto.setMeetingTime(LocalTime.now().withSecond(0).withNano(0));
        dto.setMaxParticipants(4L);
        model.addAttribute("matchingRoomDTO", dto);

        return "matching/create";
    }

    /**
     * 매칭방 생성 처리
     */
    @PostMapping("/create")
    public String createSubmit(@Valid @ModelAttribute("matchingRoomDTO") MatchingRoomDTO dto,
                               BindingResult bindingResult,
                               @RequestParam(name="imageFile", required=false) MultipartFile imageFile,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        // 폼 검증
        if (bindingResult.hasErrors()) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            return "matching/create";
        }

        try {
            // 이미지 업로드 처리 (예시)
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                // 실제 로직: fileService.saveImage(imageFile)
                // 예: "/upload/" + 파일명
                String savedImageUrl = "/upload/" + originalFilename;
                dto.setImageUrl(savedImageUrl);
            }

            // 매칭방 생성
            matchingService.createRoom(dto, loginUser);

            redirectAttributes.addFlashAttribute("successMessage", "매칭방이 성공적으로 생성되었습니다.");
            return "redirect:/matching/list";

        } catch (RuntimeException e) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            model.addAttribute("errorMessage", e.getMessage());
            return "matching/create";
        }
    }

    /**
     * 매칭방 상세 페이지
     */
    @GetMapping("/detail/{id}")
    public String getMatchingRoomDetail(@PathVariable("id") Long roomId,
                                        Model model,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            MatchingRoom room = matchingService.getRoomById(roomId);
            populateModelForDetail(model, room, loginUser);
            return "matching/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/matching/list";
        }
    }

    /**
     * 매칭방 수정 폼 페이지
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long roomId,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            MatchingRoom room = matchingService.getRoomById(roomId);
            if (!room.getHost().getUserId().equals(loginUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "방장만 수정할 수 있습니다.");
                return "redirect:/matching/detail/" + roomId;
            }

            // 엔티티 -> DTO 변환
            MatchingRoomDTO dto = matchingService.convertToDto(room);
            dto.setRoomId(roomId); // 식별자

            // 호스트 펫 목록
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("matchingRoomDTO", dto);
            model.addAttribute("userPets", userPets);
            return "matching/edit";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/matching/list";
        }
    }

    /**
     * 매칭방 수정 처리
     */
    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable("id") Long roomId,
                             @Valid @ModelAttribute("matchingRoomDTO") MatchingRoomDTO dto,
                             BindingResult bindingResult,
                             @RequestParam(name="imageFile", required=false) MultipartFile imageFile,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        if (bindingResult.hasErrors()) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            return "matching/edit";
        }

        try {
            // 이미지 수정 로직
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String savedImageUrl = "/upload/" + originalFilename;
                dto.setImageUrl(savedImageUrl);
            }

            matchingService.updateRoom(roomId, dto, loginUser);

            redirectAttributes.addFlashAttribute("successMessage", "매칭방이 성공적으로 수정되었습니다.");
            return "redirect:/matching/detail/" + roomId;

        } catch (RuntimeException e) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
            model.addAttribute("errorMessage", e.getMessage());
            return "matching/edit";
        }
    }

    /**
     * 참가 신청 처리
     */
    @PostMapping("/apply/{roomId}")
    public String applyRoom(@PathVariable("roomId") Long roomId,
                            @RequestParam(value = "additionalPetIds", required = false) List<Long> additionalPetIds,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            matchingService.applyRoom(roomId, loginUser.getUserId(), additionalPetIds);
            redirectAttributes.addFlashAttribute("successMessage", "참가 신청이 성공적으로 완료되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/matching/detail/" + roomId;
    }

    /**
     * 참가자 승인
     */
    @PostMapping("/accept/{roomId}/{userId}")
    public String acceptParticipant(@PathVariable("roomId") Long roomId,
                                    @PathVariable("userId") Long userId,
                                    RedirectAttributes redirectAttributes) {
        try {
            matchingService.acceptParticipant(roomId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "참가자가 승인되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/matching/detail/" + roomId;
    }

    /**
     * 참가자 거절
     */
    @PostMapping("/reject/{roomId}/{userId}")
    public String rejectParticipant(@PathVariable("roomId") Long roomId,
                                    @PathVariable("userId") Long userId,
                                    RedirectAttributes redirectAttributes) {
        try {
            matchingService.rejectParticipant(roomId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "참가자가 거절되었습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/matching/detail/" + roomId;
    }

    // 세션에서 로그인 유저 가져오기
    private User getManagedLoginUser(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return null;
        return userRepository.findById(loginUser.getUserId()).orElse(null);
    }

    // 상세 페이지 model 세팅
    private void populateModelForDetail(Model model, MatchingRoom room, User loginUser) {
        model.addAttribute("room", room);
        model.addAttribute("loginUser", loginUser);

        List<RoomParticipant> participants = matchingService.getParticipantsByRoomId(room.getRoomId());
        List<RoomParticipant> pendingParticipants =
                matchingService.filterParticipants(participants, RoomParticipant.ParticipantStatus.Pending);
        List<RoomParticipant> acceptedParticipants =
                matchingService.filterParticipants(participants, RoomParticipant.ParticipantStatus.Accepted);

        model.addAttribute("pendingParticipants", pendingParticipants);
        model.addAttribute("acceptedParticipants", acceptedParticipants);

        boolean isHost = room.getHost().getUserId().equals(loginUser.getUserId());
        model.addAttribute("isHost", isHost);

        if (!isHost) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
        }
    }
}
