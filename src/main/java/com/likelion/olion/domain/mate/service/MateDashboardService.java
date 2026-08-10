package com.likelion.olion.domain.mate.service;

import com.likelion.olion.domain.mate.dto.MateDashboardResponse;
import com.likelion.olion.domain.mate.dto.MatePinResponse;
import com.likelion.olion.domain.reading.dto.BadgeResponse;
import com.likelion.olion.domain.reading.dto.StreakResponse;
import com.likelion.olion.domain.reading.service.ReadingSessionService;
import org.springframework.stereotype.Service;

@Service
public class MateDashboardService {
    private final MatePinService matePinService;
    private final ReadingSessionService readingSessionService;

    public MateDashboardService(MatePinService matePinService, ReadingSessionService readingSessionService) {
        this.matePinService = matePinService;
        this.readingSessionService = readingSessionService;
    }

    public MateDashboardResponse getDashboard(Long userId) {
        StreakResponse streaks = readingSessionService.getStreaks(userId);
        MatePinResponse pins = matePinService.getPins(userId);
        BadgeResponse badges = readingSessionService.getBadges(userId);
        return new MateDashboardResponse(streaks.week(), pins.pins(), badges.badgeCount());
    }
}
