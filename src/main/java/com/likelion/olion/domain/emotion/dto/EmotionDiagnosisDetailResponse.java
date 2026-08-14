package com.likelion.olion.domain.emotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "특정 감정 진단과 추천 도서 조회 결과")
public record EmotionDiagnosisDetailResponse(
        @Schema(description = "감정 진단 ID", example = "10")
        Long diagnosisId,
        @Schema(description = "진단 생성 시각", example = "2026-05-23T22:59:15Z")
        Instant createdAt,
        @Schema(description = "진단 당시 추천된 도서 목록")
        List<RecommendedBook> recommendedBooks
) {
    @Schema(description = "진단 당시 추천 도서")
    public record RecommendedBook(
            @Schema(description = "도서 ID", example = "5")
            Long bookId,
            @Schema(description = "도서 제목", example = "아몬드")
            String title,
            @Schema(description = "도서 표지 이미지 URL", example = "https://cdn.olion.com/book/5.png")
            String coverImageUrl,
            @Schema(description = "도서 짧은 소개", example = "감정을 배우지 못한 소년의 이야기")
            String shortDesc
    ) {
    }
}
