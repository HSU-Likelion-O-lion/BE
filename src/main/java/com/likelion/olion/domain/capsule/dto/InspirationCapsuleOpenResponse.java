package com.likelion.olion.domain.capsule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영감 캡슐 열기 응답")
public record InspirationCapsuleOpenResponse(
        @Schema(description = "명언 문구", example = "삶이 그대를 속일지라도 슬퍼하거나 노여워하지 말라") String quoteText,
        @Schema(description = "발췌 도서 제목", example = "삶이 그대를 속일지라도") String bookTitle
) {
}
