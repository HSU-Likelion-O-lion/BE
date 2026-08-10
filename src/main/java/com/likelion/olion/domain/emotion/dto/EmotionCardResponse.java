package com.likelion.olion.domain.emotion.dto;

import java.util.List;

public record EmotionCardResponse(List<Card> cards) {
    public record Card(int cardId, String content) {
    }
}
