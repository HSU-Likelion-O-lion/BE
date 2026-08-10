package com.likelion.olion.domain.bookshelf.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "책장 도서 상태 변경 요청")
public record BookStatusChangeRequest(
        @Schema(description = "변경할 독서 상태", allowableValues = {"BEFORE_READING", "READING", "DONE"}, example = "READING")
        @NotBlank String status) {
}
