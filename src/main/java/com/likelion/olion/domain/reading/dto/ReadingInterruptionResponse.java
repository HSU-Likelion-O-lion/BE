package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 이탈 사유 기록 결과")
public record ReadingInterruptionResponse(
        @Schema(description = "이탈 기록 ID", example = "55") Long interruptionId
) {
}
