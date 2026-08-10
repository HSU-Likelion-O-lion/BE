package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class CommunityAccessChecker {
    private final ReadingSessionRepository readingSessionRepository;

    public CommunityAccessChecker(ReadingSessionRepository readingSessionRepository) {
        this.readingSessionRepository = readingSessionRepository;
    }

    public boolean canEnter(Long userId) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zoneId);
        return readingSessionRepository.findByUserIdAndStatus(userId, ReadingSessionStatus.COMPLETED).stream()
                .anyMatch(session -> session.getStartedAt().atZone(zoneId).toLocalDate().equals(today));
    }
}
