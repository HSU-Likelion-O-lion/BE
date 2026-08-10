package com.likelion.olion.domain.reading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 heartbeat 요청")
public record ReadingSessionHeartbeatRequest(
        @Schema(description = "클라이언트가 계산한 경과 시간(초)", example = "300")
        @NotNull @Min(0) Integer elapsedSeconds
) {
}
