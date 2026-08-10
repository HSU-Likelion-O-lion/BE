package com.likelion.olion.domain.emotion.dto;

import java.util.List;

public record EmotionDiagnosisResponse(Long diagnosisId, List<RecommendedBook> recommendedBooks) {
    public record RecommendedBook(
            Long bookId,
            String title,
            String coverImageUrl,
            String shortDesc
    ) {
    }
}
