package com.busanit501.bootproject.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * RoomParticipantId 클래스
 * - 매칭방 참가자 엔티티(RoomParticipant)의 복합 키를 정의하는 클래스.
 * - JPA에서 복합 키를 사용할 때 @Embeddable 어노테이션을 통해 키 클래스를 정의.
 */
@Data
@Embeddable
public class RoomParticipantId implements Serializable {

    /**
     * 매칭방 ID
     * - 참가자가 속한 매칭방의 고유 ID.
     */
    private Long roomId;

    /**
     * 사용자 ID
     * - 참가자 유저의 고유 ID.
     */
    private Long userId;

    /**
     * 반려동물 ID
     * - 참가자가 등록한 반려동물의 고유 ID.
     */
    private Long petId;

    /**
     * 기본 생성자
     * - JPA 표준에서는 기본 생성자가 필수.
     * - 리플렉션을 통해 엔티티를 초기화하거나 데이터베이스에서 값을 로드할 때 사용.
     */
    public RoomParticipantId() {}

    /**
     * 모든 필드를 초기화하는 생성자
     * @param roomId 매칭방 ID
     * @param userId 사용자 ID
     * @param petId 반려동물 ID
     */
    public RoomParticipantId(Long roomId, Long userId, Long petId) {
        this.roomId = roomId;
        this.userId = userId;
        this.petId = petId;
    }

    /**
     * equals 메서드 오버라이드
     * - 두 RoomParticipantId 객체를 비교하여 동일한 키를 가지는지 확인.
     * - roomId, userId, petId 필드를 기준으로 비교.
     * @param o 비교할 객체
     * @return 두 객체가 동일한 키를 가지면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 동일한 객체인 경우
        if (o == null || getClass() != o.getClass()) return false; // 타입이 다르거나 null인 경우
        RoomParticipantId that = (RoomParticipantId) o;
        return Objects.equals(roomId, that.roomId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(petId, that.petId);
    }

    /**
     * hashCode 메서드 오버라이드
     * - 객체를 해시 기반 컬렉션(Map, Set 등)에서 사용할 때 고유한 해시 코드를 생성.
     * - roomId, userId, petId를 기반으로 해시 코드를 생성.
     * @return 객체의 고유한 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(roomId, userId, petId);
    }
}

/**
 * 상세 설명
 * @Embeddable:
 *
 * 이 클래스는 JPA에서 엔티티의 복합 키를 정의하기 위해 사용.
 * RoomParticipant 엔티티에서 @EmbeddedId를 통해 사용됨.
 * 필드:
 *
 * roomId:
 * 참가자가 속한 매칭방의 고유 ID.
 * userId:
 * 참가자 유저의 고유 ID.
 * petId:
 * 참가자가 등록한 반려동물의 고유 ID.
 * 생성자:
 *
 * 기본 생성자:
 * JPA는 기본 생성자를 필요로 하며, 이는 프록시 객체 생성 및 데이터 로딩 시 사용.
 * 필드를 초기화하는 생성자:
 * 복합 키 객체를 초기화하기 위해 사용.
 * equals 메서드:
 *
 * 객체 비교를 위한 메서드로, roomId, userId, petId가 모두 동일한지 확인.
 * 동일한 복합 키를 가지는 객체는 동일하다고 간주.
 * hashCode 메서드:
 *
 * 해시 기반 컬렉션에서 고유성을 보장하기 위해 해시 코드를 생성.
 * roomId, userId, petId를 기반으로 해시 코드 생성.
 * 용도:
 *
 * 매칭방 참가자의 복합 키를 관리하여 데이터베이스의 복합 키 구조를 매핑.
 * 동일한 참가자를 중복 저장하지 않도록 보장.
 */