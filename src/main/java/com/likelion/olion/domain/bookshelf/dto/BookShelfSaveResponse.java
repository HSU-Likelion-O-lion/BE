package com.likelion.olion.domain.bookshelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "책장 변경 결과")
public record BookShelfSaveResponse(
        @Schema(description = "책장 도서 항목 ID", example = "30") Long userBookId,
        @Schema(description = "현재 독서 상태", example = "BEFORE_READING") String status) {
}
