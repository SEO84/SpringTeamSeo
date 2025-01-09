package com.busanit501.bootproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 매칭방(MatchingRoom) 엔티티 클래스
 * - 매칭방 정보와 관련된 데이터를 관리하는 엔티티로, 데이터베이스의 matching_rooms 테이블과 매핑된다.
 * - 매칭방의 호스트, 제목, 설명, 장소, 일정, 참가자 정보 등을 포함한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "matching_rooms")
public class MatchingRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId; // 매칭방 ID (Primary Key)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host; // 매칭방 호스트(방장) 정보 (User 엔티티와 다대일 관계)

    @Column(nullable = false)
    private String title; // 매칭방 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // 매칭방 설명 (TEXT 타입)

    @Column(nullable = false)
    private String place; // 매칭방 장소

    @Column(nullable = false)
    private LocalDate meetingDate; // 매칭방 날짜

    @Column(nullable = false)
    private LocalTime meetingTime; // 매칭방 시간

    @Column(nullable = false)
    private Long maxParticipants; // 매칭방 최대 참가자 수

    private String imageUrl; // 매칭방 이미지 URL (옵션 필드)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 매칭방 생성자 정보 (User 엔티티와 다대일 관계)

    @OneToMany(mappedBy = "matchingRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomParticipant> participants = new ArrayList<>();
    // 매칭방 참가자 목록 (RoomParticipant 엔티티와 일대다 관계)

    /**
     * 현재 매칭방에 참여 중인 참가자의 수를 반환
     * - 참가 상태가 ACCEPTED인 참가자만 포함한다.
     *
     * @return 현재 참가자 수
     */
    public Long getCurrentParticipants() {
        return participants.stream()
                .filter(p -> p.getStatus() == RoomParticipant.ParticipantStatus.Accepted) // 참가 상태가 ACCEPTED인 경우 필터링
                .map(RoomParticipant::getUser) // User 객체 추출
                .distinct() // 중복 제거
                .count(); // 참가자 수 계산
    }
}
