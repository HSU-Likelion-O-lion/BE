package com.likelion.olion.domain.emotion.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감정 진단 결과 응답")
public record EmotionDiagnosisResponse(
        @Schema(description = "감정 진단 ID", example = "100") Long diagnosisId,
        @Schema(description = "진단 결과 기반 추천 도서 목록") List<RecommendedBook> recommendedBooks) {
    @Schema(description = "추천 도서")
    public record RecommendedBook(
            @Schema(description = "도서 ID", example = "1")
            Long bookId,
            @Schema(description = "도서 제목", example = "어린 왕자")
            String title,
            @Schema(description = "표지 이미지 URL")
            String coverImageUrl,
            @Schema(description = "도서 한 줄 설명")
            String shortDesc
    ) {
    }
}
