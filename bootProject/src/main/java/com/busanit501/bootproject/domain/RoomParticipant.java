package com.busanit501.bootproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RoomParticipant 엔티티 클래스
 * - 매칭방에 참가하는 유저와 펫의 정보를 관리하는 엔티티로, 데이터베이스의 room_participants 테이블과 매핑된다.
 * - 매칭방, 참가 유저, 참가 유저의 펫, 참가 상태 정보를 포함한다.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "room_participants")
public class RoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId;
    // 참가자의 고유 식별자 (Primary Key)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_room_id", nullable = false)
    private MatchingRoom matchingRoom;
    // 매칭방 정보 (MatchingRoom 엔티티와 다대일 관계)
    // 매칭방 ID로 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // 참가 유저 정보 (User 엔티티와 다대일 관계)
    // 참가 유저 ID로 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
    // 참가 유저의 반려동물 정보 (Pet 엔티티와 다대일 관계)
    // 참가 펫 ID로 매핑

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;
    // 참가자의 상태 (Enum)
    // Pending: 대기 중, Accepted: 승인됨, Rejected: 거절됨

    /**
     * 참가 상태를 정의하는 열거형
     * - Pending: 참가 요청이 대기 상태.
     * - Accepted: 참가 요청이 승인된 상태.
     * - Rejected: 참가 요청이 거절된 상태.
     */
    public enum ParticipantStatus {
        Pending,
        Accepted,
        Rejected
    }
}

/**
 * 상세 설명
 * @Entity와 @Table:
 *
 * 이 클래스는 매칭방의 참가 정보를 저장하는 엔티티로, 데이터베이스의 room_participants 테이블에 매핑된다.
 * 테이블 이름은 @Table(name = "room_participants")를 통해 지정.
 * 필드 설명:
 *
 * participantId:
 * 참가자의 고유 식별자.
 * @Id와 @GeneratedValue를 통해 자동 생성.
 *         matchingRoom:
 * 참가자가 속한 매칭방 정보.
 * MatchingRoom 엔티티와 ManyToOne 관계를 가지며, matching_room_id 컬럼으로 매핑.
 *         user:
 * 매칭방에 참가하는 유저 정보.
 * User 엔티티와 ManyToOne 관계를 가지며, user_id 컬럼으로 매핑.
 *         pet:
 * 참가 유저가 등록한 반려동물 정보.
 * Pet 엔티티와 ManyToOne 관계를 가지며, pet_id 컬럼으로 매핑.
 *         status:
 * 참가 상태를 나타내는 열거형(Enum).
 * @Enumerated(EnumType.STRING)를 통해 문자열 형태로 데이터베이스에 저장.
 * Pending, Accepted, Rejected의 세 가지 상태를 가짐.
 * 열거형 설명:
 *
 * ParticipantStatus:
 * 참가자의 현재 상태를 정의.
 * Pending: 대기 상태로, 승인이나 거절 대기 중.
 *         Accepted: 참가 요청이 승인된 상태.
 * Rejected: 참가 요청이 거절된 상태.
 * 관계 매핑:
 *
 * @ManyToOne(fetch = FetchType.LAZY):
 * 각 참가자는 하나의 매칭방, 유저, 펫과 연결.
 * LAZY 로딩을 사용하여 필요할 때 데이터를 로드.
 * @JoinColumn:
 * 외래 키(Foreign Key) 컬럼 이름을 지정하여 관계를 데이터베이스에 매핑.
 * 용도:
 *
 * 매칭방에 참가 요청을 한 유저와 관련된 펫 및 상태 정보를 관리.
 * 매칭방의 참가 현황을 조회하거나 참가 요청을 처리할 때 활용.
 */