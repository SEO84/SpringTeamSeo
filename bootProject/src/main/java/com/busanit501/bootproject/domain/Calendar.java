package com.busanit501.bootproject.domain;


import com.busanit501.bootproject.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "calendar")
public class Calendar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;  // 일정 ID (자동 증가)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 사용자와의 관계 (외래 키)

//    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<MatchingRoom> matchingRooms;

//    @Column(name = "matching_id", nullable = true)
//    private Long matchingId;

    @Column(name = "schedule_name", nullable = false)
    private String schedulename;

    @Column(name = "walk_date", nullable = false)
    private LocalDate walkDate;  // 산책 날짜

    @Column(name = "walk_time", nullable = false)
    private LocalTime walkTime;  // 산책 시간

    @Column(name = "walk_place", nullable = false)
    private String walkPlace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;  // 산책 상태 (예정, 완료, 취소)



    @Column(name = "matching")
    private Boolean matching;
    @Column(name = "schedul_start")
    private LocalTime schedulStart;
    @Column(name = "schedul_end")
    private LocalTime schedulEnd;

    public void changeStatus(ScheduleStatus status) {

        this.status = status;
    }


}