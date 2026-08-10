package com.likelion.olion.domain.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "독서 통계 응답")
public record ReadingStatisticsResponse(
        @Schema(description = "계속 읽기 선택 횟수", example = "12") int continueCount,
        @Schema(description = "전자책 전환 횟수", example = "4") int ebookSwitchCount,
        @Schema(description = "요일별 집중 시간 목록") List<WeekdayStat> byWeekday,
        @Schema(description = "시간대별 집중 시간 목록") List<HourStat> byHour
) {
    @Schema(description = "요일별 집중 시간")
    public record WeekdayStat(
            @Schema(description = "요일", example = "MONDAY") String weekday,
            @Schema(description = "집중 시간(분)", example = "60") int focusedMinutes
    ) {
    }

    @Schema(description = "시간대별 집중 시간")
    public record HourStat(
            @Schema(description = "시각(0~23)", example = "10") int hour,
            @Schema(description = "집중 시간(분)", example = "90") int focusedMinutes
    ) {
    }
}
