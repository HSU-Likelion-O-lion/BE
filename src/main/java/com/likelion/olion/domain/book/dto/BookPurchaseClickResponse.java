package com.likelion.olion.domain.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도서 구매 링크 클릭 기록 결과")
public record BookPurchaseClickResponse(
        @Schema(
                description = "이동할 외부 서점 링크",
                example = "https://book.store.com/item/5?ref=olion"
        )
        String redirectUrl
) {
}
