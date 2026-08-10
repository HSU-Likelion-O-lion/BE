package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "주간 독서 달성 현황 응답")
public record StreakResponse(
        @Schema(description = "최근 7일 달성 현황") List<Day> week
) {
    @Schema(description = "일별 독서 달성 상태")
    public record Day(
            @Schema(description = "날짜", example = "2026-08-10") LocalDate date,
            @Schema(description = "해당 날짜의 독서 목표 달성 여부", example = "true") boolean achieved
    ) {
    }
}
