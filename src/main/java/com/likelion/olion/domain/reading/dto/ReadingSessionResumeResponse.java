package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 재개 결과")
public record ReadingSessionResumeResponse(
        @Schema(description = "세션 상태", example = "IN_PROGRESS")
        String status,
        @Schema(description = "서버 기준 남은 시간(초)", example = "900")
        int remainingSeconds
) {
}
