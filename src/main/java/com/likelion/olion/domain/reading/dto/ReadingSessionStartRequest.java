package com.likelion.olion.domain.reading.dto;

import jakarta.validation.constraints.NotNull;

public record ReadingSessionStartRequest(
        @NotNull Long userBookId,
        @NotNull Integer targetMinutes
) {
}
