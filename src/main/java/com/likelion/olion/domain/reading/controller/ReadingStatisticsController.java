package com.likelion.olion.domain.reading.controller;

import com.likelion.olion.domain.reading.dto.ReadingStatisticsResponse;
import com.likelion.olion.domain.reading.service.ReadingSessionService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/reading-statistics")
@Tag(name = "독서 통계", description = "독서 세션 및 이탈 기록 기반 통계 API")
public class ReadingStatisticsController {
    private final ReadingSessionService readingSessionService;

    public ReadingStatisticsController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    @GetMapping
    @Operation(summary = "독서 통계 조회", description = "계속 읽기·전자책 전환 횟수와 완료된 세션의 요일별·시간대별 집중 시간을 조회합니다.")
    public ResponseEntity<ApiResponse<ReadingStatisticsResponse>> getStatistics(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "통계를 조회했습니다.",
                readingSessionService.getStatistics(Long.valueOf(principal.getName()))));
    }
}
