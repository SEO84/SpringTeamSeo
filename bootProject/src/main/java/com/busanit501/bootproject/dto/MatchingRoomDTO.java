package com.busanit501.bootproject.dto;

import com.busanit501.bootproject.domain.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class MatchingRoomDTO {
    // ★ 수정/생성 시 방을 구분하기 위한 필드
    private Long roomId;

    // 이미지 URL (업로드 시 저장 경로)
    private String imageUrl;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "설명은 필수 입력 항목입니다.")
    private String description;

    @NotBlank(message = "장소는 필수 입력 항목입니다.")
    private String place;

    @NotNull(message = "날짜는 필수 입력 항목입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate meetingDate;

    @NotNull(message = "시간은 필수 입력 항목입니다.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime meetingTime;

    @NotNull(message = "최대 인원은 필수 입력 항목입니다.")
    @Min(value = 1, message = "최대 인원은 최소 1명 이상이어야 합니다.")
    private Long maxParticipants;
    // 추가된 필드: 현재 참가 인원 수
    private Long currentParticipants;

    @NotEmpty(message = "최소 한 마리의 반려동물을 선택해야 합니다.")
    private List<Long> petIds;

    // 추가 참가 펫 ID 목록(선택적)
    private List<Long> additionalPetIds;

    // 추가된 필드: 참여자의 펫 목록
    private List<PetDTO> pets;

    // 펫 타입을 문자열로 추가 (여러 타입을 하나의 문자열로 결합)
    private String petType;

    // 추가된 필드: 호스트 정보
    private UserDTO host;
}
