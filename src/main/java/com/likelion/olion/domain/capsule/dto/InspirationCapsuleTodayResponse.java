package com.likelion.olion.domain.capsule.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 영감 캡슐 상태 응답")
public record InspirationCapsuleTodayResponse(
        @Schema(description = "오늘 열람 여부", example = "false") boolean opened,
        @Schema(description = "명언 문구 (미열람 시 null)", example = "삶이 그대를 속일지라도...") String quoteText,
        @Schema(description = "발췌 도서 제목 (미열람 시 null)", example = "안나 카레니나") String bookTitle
) {
}
