package com.likelion.olion.domain.reading.controller;

import com.likelion.olion.domain.reading.dto.BadgeResponse;
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
@RequestMapping("/api/badges")
@Tag(name = "독서 배지", description = "누적 독서 배지 조회 API")
public class BadgeController {
    private final ReadingSessionService readingSessionService;

    public BadgeController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    @GetMapping
    @Operation(summary = "누적 배지 조회", description = "완료된 독서 세션을 기반으로 누적 배지 개수와 획득 시각을 조회합니다.")
    public ResponseEntity<ApiResponse<BadgeResponse>> getBadges(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "훈장을 조회했습니다.",
                readingSessionService.getBadges(Long.valueOf(principal.getName()))));
    }
}
