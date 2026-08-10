package com.likelion.olion.domain.mate.controller;

import com.likelion.olion.domain.mate.dto.MateDashboardResponse;
import com.likelion.olion.domain.mate.service.MateDashboardService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/mate/dashboard")
@Tag(name = "메이트 대시보드", description = "메이트 메인 화면 통합 조회 API")
public class MateDashboardController {
    private final MateDashboardService mateDashboardService;

    public MateDashboardController(MateDashboardService mateDashboardService) {
        this.mateDashboardService = mateDashboardService;
    }

    @GetMapping
    @Operation(summary = "메이트 대시보드 조회", description = "주간 달성 현황, 핀 고정 도서, 누적 배지 개수를 한 번에 조회합니다.")
    public ResponseEntity<ApiResponse<MateDashboardResponse>> getDashboard(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "대시보드를 조회했습니다.",
                mateDashboardService.getDashboard(Long.valueOf(principal.getName()))));
    }
}
