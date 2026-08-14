package com.likelion.olion.domain.reading.dto;

import com.likelion.olion.domain.reading.entity.ReadingSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진행 중 독서 세션 조회 결과")
public record ActiveReadingSessionResponse(
        @Schema(description = "진행 중 세션 정보. 없으면 null") ActiveSession session) {
    public static ActiveReadingSessionResponse from(ReadingSession readingSession) {
        return from(readingSession, Instant.now());
    }

    public static ActiveReadingSessionResponse from(ReadingSession readingSession, Instant now) {
        if (readingSession == null) {
            return new ActiveReadingSessionResponse(null);
        }

        int targetSeconds = readingSession.getTargetMinutes() * 60;
        int remainingSeconds = (int) Math.max(0,
                targetSeconds - readingSession.calculateFocusedSeconds(now));
        return new ActiveReadingSessionResponse(new ActiveSession(
                readingSession.getSessionId(),
                readingSession.getStatus().name(),
                remainingSeconds));
    }

    @Schema(description = "진행 중 세션 정보")
    public record ActiveSession(
            @Schema(description = "독서 세션 ID", example = "100") Long sessionId,
            @Schema(description = "세션 상태", example = "IN_PROGRESS") String status,
            @Schema(description = "서버 기준 남은 시간(초)", example = "900") int remainingSeconds) {
    }
}
