package com.likelion.olion.domain.reading.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 시작 요청")
public record ReadingSessionStartRequest(
        @Schema(description = "독서할 책장 도서 항목 ID", example = "30") @NotNull Long userBookId,
        @Schema(description = "목표 독서 시간(분). 15, 30, 60 중 하나", allowableValues = {"15", "30", "60"}, example = "30")
        @NotNull Integer targetMinutes
) {
}
