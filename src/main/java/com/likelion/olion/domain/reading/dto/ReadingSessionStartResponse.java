package com.likelion.olion.domain.reading.dto;

import com.likelion.olion.domain.reading.entity.ReadingSession;

import java.time.Instant;

public record ReadingSessionStartResponse(
        Long sessionId,
        String status,
        Instant startedAt
) {
    public static ReadingSessionStartResponse from(ReadingSession session) {
        return new ReadingSessionStartResponse(
                session.getSessionId(),
                session.getStatus().name(),
                session.getStartedAt());
    }
}
