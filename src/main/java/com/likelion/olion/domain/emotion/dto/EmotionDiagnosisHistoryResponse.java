package com.likelion.olion.domain.emotion.dto;

import java.time.Instant;
import java.util.List;

public record EmotionDiagnosisHistoryResponse(List<Diagnosis> diagnoses) {
    public record Diagnosis(Long diagnosisId, Instant createdAt) {
    }
}
