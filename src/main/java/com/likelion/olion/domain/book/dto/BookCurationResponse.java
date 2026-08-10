package com.likelion.olion.domain.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감정 진단 기반 도서 맞춤 소개글")
public record BookCurationResponse(
        @Schema(description = "도서 ID", example = "5")
        Long bookId,
        @Schema(description = "진단 결과에 맞춘 도서 소개 문구", example = "지친 마음에 건네는 다정한 한마디...")
        String curationText
) {
}
