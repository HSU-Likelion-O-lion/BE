package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "누적 독서 배지 응답")
public record BadgeResponse(
        @Schema(description = "누적 배지 개수", example = "3") int badgeCount,
        @Schema(description = "배지 획득 기록 목록") List<Badge> badges
) {
    @Schema(description = "배지 획득 기록")
    public record Badge(
            @Schema(description = "배지 획득 시각", example = "2026-08-10T09:00:00Z") Instant earnedAt
    ) {
    }
}
