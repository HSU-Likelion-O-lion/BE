package com.likelion.olion.domain.mate.service;

import com.likelion.olion.domain.mate.dto.MateDashboardResponse;
import com.likelion.olion.domain.mate.dto.MatePinResponse;
import com.likelion.olion.domain.reading.dto.BadgeResponse;
import com.likelion.olion.domain.reading.dto.StreakResponse;
import com.likelion.olion.domain.reading.service.ReadingSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MateDashboardServiceTest {
    @Mock
    private MatePinService matePinService;

    @Mock
    private ReadingSessionService readingSessionService;

    @Test
    void combinesDashboardData() {
        MateDashboardService service = new MateDashboardService(matePinService, readingSessionService);
        StreakResponse streaks = new StreakResponse(List.of(new StreakResponse.Day(LocalDate.now(), true)));
        MatePinResponse pins = new MatePinResponse(List.of(new MatePinResponse.Pin(30L, 1)));
        BadgeResponse badges = new BadgeResponse(3, List.of());
        given(readingSessionService.getStreaks(1L)).willReturn(streaks);
        given(matePinService.getPins(1L)).willReturn(pins);
        given(readingSessionService.getBadges(1L)).willReturn(badges);

        MateDashboardResponse response = service.getDashboard(1L);

        assertThat(response.week()).hasSize(1);
        assertThat(response.pins()).hasSize(1);
        assertThat(response.badgeCount()).isEqualTo(3);
    }
}
