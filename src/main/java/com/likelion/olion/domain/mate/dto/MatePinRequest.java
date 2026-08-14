package com.likelion.olion.domain.mate.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메이트 핀 고정 요청")
public record MatePinRequest(
        @Schema(description = "핀 고정할 책장 도서 항목 ID", example = "30") @NotNull Long userBookId) {
}
