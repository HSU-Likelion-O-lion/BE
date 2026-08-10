package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "독서 세션 강제 종료 결과")
public record ReadingSessionAbandonResponse(
        @Schema(description = "종료된 세션 상태", example = "ABANDONED") String status
) {
}
