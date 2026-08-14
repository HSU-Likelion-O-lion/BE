package com.likelion.olion.domain.reading.dto;

import com.likelion.olion.domain.reading.entity.ReadingInterruptionReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(description = "독서 세션 이탈 사유 기록 요청")
public record ReadingInterruptionRequest(
        @Schema(description = "이탈 사유 코드", allowableValues = {
                "TASTE_MISMATCH", "NOTIFICATION", "EBOOK_SWITCH", "UNAVOIDABLE", "OTHER", "CONTINUE"
        }, example = "OTHER")
        @NotBlank String reason,
        @Schema(description = "OTHER 선택 시 사용자가 직접 입력한 사유", example = "갑자기 졸려서요")
        String customText,
        @Schema(description = "이탈 발생 시각", example = "2026-08-10T10:05:00Z")
        @NotNull Instant occurredAt
) {
    public ReadingInterruptionReason parsedReason() {
        try {
            return ReadingInterruptionReason.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
