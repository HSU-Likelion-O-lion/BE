package com.likelion.olion.domain.reading.dto;

public record ReadingSessionHeartbeatResponse(
        int remainingSeconds,
        boolean valid
) {
}
