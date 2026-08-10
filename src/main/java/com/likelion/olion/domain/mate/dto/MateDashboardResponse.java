package com.likelion.olion.domain.mate.dto;

import com.likelion.olion.domain.reading.dto.StreakResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "메이트 대시보드 응답")
public record MateDashboardResponse(
        @Schema(description = "최근 7일 독서 달성 현황") List<StreakResponse.Day> week,
        @Schema(description = "메이트 화면에 핀 고정된 도서 목록") List<MatePinResponse.Pin> pins,
        @Schema(description = "누적 배지 개수", example = "3") int badgeCount
) {
}
