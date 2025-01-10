package com.busanit501.bootproject;
import com.busanit501.bootproject.domain.Calendar;
import com.busanit501.bootproject.domain.MatchingRoom;
import com.busanit501.bootproject.domain.User;
import com.busanit501.bootproject.enums.ScheduleStatus;
import com.busanit501.bootproject.repository.CalendarRepository;
import com.busanit501.bootproject.service.CalendarService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Log4j2
public class CalenderServiceTests {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarRepository calendarRepository;

    @Test
    public void testSaveSchedule() {
        // Given: 테스트 데이터 준비
        User user = User.builder()
                .userId(1L)
                .name("testUser")
                .build();

        MatchingRoom room = MatchingRoom.builder()
                .roomId(1L)
                .title("Morning Walk")
                .meetingDate(LocalDate.of(2025, 1, 15))
                .meetingTime(LocalTime.of(7, 30))
                .place("Central Park")
                .build();

        // When: 서비스 메서드 호출
        calendarService.saveSchedule(user, room);

        // Then: 저장 결과 검증
        Optional<Calendar> savedSchedule = calendarRepository.findByUserAndSchedulename(user, room.getTitle());
        assertThat(savedSchedule).isPresent();

        Calendar calendar = savedSchedule.get();
        assertThat(calendar.getSchedulename()).isEqualTo("Morning Walk");
        assertThat(calendar.getWalkDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(calendar.getWalkTime()).isEqualTo(LocalTime.of(7, 30));
        assertThat(calendar.getWalkPlace()).isEqualTo("Central Park");
        assertThat(calendar.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);

        log.info("Saved schedule: {}", calendar);
    }
}

