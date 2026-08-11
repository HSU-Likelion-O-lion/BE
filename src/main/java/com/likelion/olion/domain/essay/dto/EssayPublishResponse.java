package com.likelion.olion.domain.essay.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "에세이 발행 응답")
public record EssayPublishResponse(
        @Schema(description = "에세이 ID", example = "7") Long essayId,
        @Schema(description = "제목", example = "흔들려도 걷는 마음") String title,
        @Schema(description = "발행 시각", example = "2026-08-11T09:00:00Z") Instant publishedAt
) {
}
