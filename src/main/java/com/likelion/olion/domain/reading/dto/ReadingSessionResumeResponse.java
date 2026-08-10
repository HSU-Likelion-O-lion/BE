package com.likelion.olion.domain.reading.dto;

public record ReadingSessionResumeResponse(
        String status,
        int remainingSeconds
) {
}
