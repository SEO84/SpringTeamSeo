package com.busanit501.bootproject.domain;

import com.busanit501.bootproject.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pets")
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long petId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private String personality;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    // ★ 새로 추가: 반려동물 프로필 사진 URL
    @Column(name = "profile_picture")
    private String profilePicture;
}
