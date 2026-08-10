package com.likelion.olion.domain.reading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReadingSessionHeartbeatRequest(
        @NotNull @Min(0) Integer elapsedSeconds
) {
}
