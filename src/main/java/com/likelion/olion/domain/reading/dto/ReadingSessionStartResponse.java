package com.likelion.olion.domain.reading.dto;

import com.likelion.olion.domain.reading.entity.ReadingSession;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 시작 결과")
public record ReadingSessionStartResponse(
        @Schema(description = "독서 세션 ID", example = "100")
        Long sessionId,
        @Schema(description = "세션 상태", example = "IN_PROGRESS")
        String status,
        @Schema(description = "세션 시작 시각", example = "2026-08-10T10:00:00Z")
        Instant startedAt
) {
    public static ReadingSessionStartResponse from(ReadingSession session) {
        return new ReadingSessionStartResponse(
                session.getSessionId(),
                session.getStatus().name(),
                session.getStartedAt());
    }
}
