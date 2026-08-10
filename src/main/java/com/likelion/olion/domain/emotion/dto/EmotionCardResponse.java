package com.likelion.olion.domain.emotion.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감정 카드 목록 응답")
public record EmotionCardResponse(
        @Schema(description = "감정 카드 목록") List<Card> cards) {
    @Schema(description = "감정 카드")
    public record Card(
            @Schema(description = "감정 카드 ID", example = "1") int cardId,
            @Schema(description = "감정 카드 문구", example = "오늘 마음이 편안한가요?") String content) {
    }
}
