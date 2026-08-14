package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 heartbeat 결과")
public record ReadingSessionHeartbeatResponse(
        @Schema(description = "서버 기준 남은 시간(초)", example = "1500")
        int remainingSeconds,
        @Schema(description = "클라이언트·서버 시간 차이가 허용 범위인지 여부", example = "true")
        boolean valid
) {
}
