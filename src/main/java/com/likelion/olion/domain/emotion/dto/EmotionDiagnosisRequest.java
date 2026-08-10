package com.likelion.olion.domain.emotion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감정 진단 제출 요청")
public record EmotionDiagnosisRequest(
        @Schema(description = "감정 카드별 선택 결과 목록")
        @NotEmpty List<@Valid Swipe> swipes
) {
    @Schema(description = "감정 카드 선택 결과")
    public record Swipe(
            @Schema(description = "감정 카드 ID", example = "1") Integer cardId,
            @Schema(description = "카드에 공감했는지 여부", example = "true") Boolean liked) {
    }
}
