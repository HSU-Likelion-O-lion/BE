package com.likelion.olion.domain.reading.controller;

import com.likelion.olion.domain.reading.dto.StreakResponse;
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
@RequestMapping("/api/streaks")
@Tag(name = "주간 달성", description = "최근 7일 독서 목표 달성 현황 API")
public class StreakController {
    private final ReadingSessionService readingSessionService;

    public StreakController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    @GetMapping
    @Operation(summary = "주간 달성 현황 조회", description = "최근 7일 동안 완료된 독서 세션이 있는 날짜를 달성 여부로 반환합니다.")
    public ResponseEntity<ApiResponse<StreakResponse>> getStreaks(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "주간 현황을 조회했습니다.",
                readingSessionService.getStreaks(Long.valueOf(principal.getName()))));
    }
}
