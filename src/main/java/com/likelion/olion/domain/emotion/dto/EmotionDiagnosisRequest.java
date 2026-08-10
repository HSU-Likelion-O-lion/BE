package com.likelion.olion.domain.emotion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EmotionDiagnosisRequest(
        @NotEmpty List<@Valid Swipe> swipes
) {
    public record Swipe(Integer cardId, Boolean liked) {
    }
}
