package com.likelion.olion.domain.reading.dto;

import com.likelion.olion.domain.reading.entity.ReadingSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ActiveReadingSessionResponse(ActiveSession session) {
    public static ActiveReadingSessionResponse from(ReadingSession readingSession) {
        if (readingSession == null) {
            return new ActiveReadingSessionResponse(null);
        }

        long elapsedSeconds = Math.max(0,
                readingSession.getStartedAt().until(Instant.now(), ChronoUnit.SECONDS));
        int targetSeconds = readingSession.getTargetMinutes() * 60;
        int remainingSeconds = (int) Math.max(0, targetSeconds - elapsedSeconds);
        return new ActiveReadingSessionResponse(new ActiveSession(
                readingSession.getSessionId(),
                readingSession.getStatus().name(),
                remainingSeconds));
    }

    public record ActiveSession(Long sessionId, String status, int remainingSeconds) {
    }
}
