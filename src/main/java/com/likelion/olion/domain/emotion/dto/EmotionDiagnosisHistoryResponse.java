package com.likelion.olion.domain.emotion.dto;

import java.time.Instant;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "감정 진단 이력 응답")
public record EmotionDiagnosisHistoryResponse(
        @Schema(description = "감정 진단 이력 목록") List<Diagnosis> diagnoses) {
    @Schema(description = "감정 진단 이력 항목")
    public record Diagnosis(
            @Schema(description = "진단 ID", example = "100") Long diagnosisId,
            @Schema(description = "진단 생성 시각", example = "2026-08-10T10:00:00Z") Instant createdAt) {
    }
}
