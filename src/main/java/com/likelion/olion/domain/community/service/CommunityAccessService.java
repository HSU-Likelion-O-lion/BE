package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityAccessResponse;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class CommunityAccessService {
    private final ReadingSessionRepository readingSessionRepository;

    public CommunityAccessService(ReadingSessionRepository readingSessionRepository) {
        this.readingSessionRepository = readingSessionRepository;
    }

    public CommunityAccessResponse getAccess(Long userId) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);
        boolean canEnter = readingSessionRepository
                .findByUserIdAndStatus(userId, ReadingSessionStatus.COMPLETED).stream()
                .anyMatch(session -> session.getStartedAt().atZone(zoneId).toLocalDate().equals(today));
        return new CommunityAccessResponse(canEnter);
    }
}
