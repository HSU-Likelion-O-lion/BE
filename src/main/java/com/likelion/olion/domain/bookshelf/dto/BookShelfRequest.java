package com.likelion.olion.domain.bookshelf.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "책장 도서 등록 요청")
public record BookShelfRequest(
        @Schema(description = "등록할 내부 도서 ID", example = "1") @NotNull Long bookId) {
}
