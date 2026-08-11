package com.likelion.olion.domain.capsule.dto;

import com.likelion.olion.domain.capsule.entity.InspirationCapsule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "영감 캡슐 히스토리 응답")
public record InspirationCapsuleHistoryResponse(
        @Schema(description = "과거 캡슐 목록 (최신순)") List<Capsule> capsules
) {
    public static InspirationCapsuleHistoryResponse from(List<InspirationCapsule> capsules) {
        return new InspirationCapsuleHistoryResponse(capsules.stream().map(Capsule::from).toList());
    }

    @Schema(description = "과거 캡슐")
    public record Capsule(
            @Schema(description = "열람 날짜", example = "2026-08-10") LocalDate openedDate,
            @Schema(description = "명언 문구", example = "삶이 그대를 속일지라도 슬퍼하거나 노여워하지 말라") String quoteText,
            @Schema(description = "발췌 도서 제목", example = "삶이 그대를 속일지라도") String bookTitle
    ) {
        private static Capsule from(InspirationCapsule capsule) {
            return new Capsule(capsule.getOpenedDate(), capsule.getQuoteText(), capsule.getBookTitle());
        }
    }
}
