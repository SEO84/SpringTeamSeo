package com.busanit501.bootproject.controller;

import com.busanit501.bootproject.domain.MatchingRoom;
import com.busanit501.bootproject.domain.Pet;
import com.busanit501.bootproject.domain.RoomParticipant;
import com.busanit501.bootproject.domain.User;
import com.busanit501.bootproject.dto.MatchingRoomDTO;
import com.busanit501.bootproject.exception.ResourceNotFoundException;
import com.busanit501.bootproject.repository.UserRepository;
import com.busanit501.bootproject.service.CalendarService;
import com.busanit501.bootproject.service.MatchingService;
import com.busanit501.bootproject.service.PetService;
import com.busanit501.bootproject.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/matching")
public class MatchingController {

    private final MatchingService matchingService;
    private final PetService petService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CalendarService calendarService;

    @Autowired
    public MatchingController(MatchingService matchingService,
                              PetService petService,
                              UserRepository userRepository,
                              UserService userService,
                              CalendarService calendarService) {
        this.matchingService = matchingService;
        this.petService = petService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.calendarService = calendarService;
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            return "redirect:/user/login";
        }
        List<MatchingRoom> rooms = matchingService.getAllRooms();
        model.addAttribute("rooms", rooms);
        return "matching/list";
    }

    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            return "redirect:/user/login";
        }
        List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
        model.addAttribute("userPets", userPets);

        MatchingRoomDTO dto = new MatchingRoomDTO();
        dto.setMeetingDate(LocalDate.now());
        dto.setMeetingTime(LocalTime.now().withSecond(0).withNano(0));
        dto.setMaxParticipants(4L);
        model.addAttribute("matchingRoomDTO", dto);

        return "matching/create";
    }

    @PostMapping("/create")
    public String createSubmit(@Valid @ModelAttribute("matchingRoomDTO") MatchingRoomDTO dto,
                               BindingResult bindingResult,
                               @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
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
            return "matching/create";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String savedImageUrl = "/upload/" + originalFilename;
                dto.setImageUrl(savedImageUrl);
            }

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
            if (!room.getUser().getUserId().equals(loginUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "방장만 수정할 수 있습니다.");
                return "redirect:/matching/detail/" + roomId;
            }

            MatchingRoomDTO dto = matchingService.convertToDto(room);
            dto.setRoomId(roomId);

            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("matchingRoomDTO", dto);
            model.addAttribute("userPets", userPets);
            return "matching/edit";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/matching/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable("id") Long roomId,
                             @Valid @ModelAttribute("matchingRoomDTO") MatchingRoomDTO dto,
                             BindingResult bindingResult,
                             @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
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

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loginUser = getManagedLoginUser(session);
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("deleteErrorMessage", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        try {
            MatchingRoom room = matchingService.getRoomById(id);
            if (!room.getUser().getUserId().equals(loginUser.getUserId())) {
                redirectAttributes.addFlashAttribute("deleteErrorMessage", "방장만 삭제할 수 있습니다.");
                return "redirect:/matching/list";
            }

            matchingService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("deleteSuccessMessage", "매칭방이 성공적으로 삭제되었습니다.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("deleteErrorMessage", "삭제 중 문제가 발생했습니다.");
        }

        return "redirect:/matching/list";
    }

    @PostMapping("/confirm/{id}")
    public String confirmSchedule(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("Confirm request received for roomId: {}", id);
        try {
            User loginUser = getManagedLoginUser(session);
            if (loginUser == null) {
                log.warn("No logged-in user found.");
                return "redirect:/user/login";
            }

            MatchingRoom room = matchingService.getRoomById(id);
            List<User> participants = matchingService.getAcceptedParticipantsByRoomId(id);

            calendarService.saveSchedule(loginUser, room, participants);
            redirectAttributes.addFlashAttribute("successMessage", "스케줄이 확정되었습니다.");

            return "redirect:/matching/list";
        } catch (Exception ex) {
            log.error("Error while confirming schedule: ", ex);
            redirectAttributes.addFlashAttribute("errorMessage", "스케줄 확정 중 문제가 발생했습니다.");
            return "redirect:/matching/detail/" + id;
        }
    }


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

    private User getManagedLoginUser(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return null;
        return userRepository.findById(loginUser.getUserId()).orElse(null);
    }

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

        boolean isHost = room.getUser().getUserId().equals(loginUser.getUserId());
        model.addAttribute("isHost", isHost);

        if (!isHost) {
            List<Pet> userPets = petService.findAllByUserId(loginUser.getUserId());
            model.addAttribute("userPets", userPets);
        }
    }
}
