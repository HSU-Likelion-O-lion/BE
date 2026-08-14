package com.likelion.olion.domain.capsule.controller;

import com.likelion.olion.domain.capsule.dto.InspirationCapsuleHistoryResponse;
import com.likelion.olion.domain.capsule.dto.InspirationCapsuleOpenResponse;
import com.likelion.olion.domain.capsule.dto.InspirationCapsuleTodayResponse;
import com.likelion.olion.domain.capsule.service.InspirationCapsuleService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/inspiration-capsule")
@Tag(name = "영감 캡슐", description = "오늘의 영감 캡슐 API")
public class InspirationCapsuleController {
    private final InspirationCapsuleService inspirationCapsuleService;

    public InspirationCapsuleController(InspirationCapsuleService inspirationCapsuleService) {
        this.inspirationCapsuleService = inspirationCapsuleService;
    }

    @GetMapping("/today")
    @Operation(summary = "오늘의 캡슐 조회", description = "오늘 캡슐을 열었는지 여부와 내용을 조회합니다.")
    public ResponseEntity<ApiResponse<InspirationCapsuleTodayResponse>> getToday(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "오늘의 캡슐 상태를 조회했습니다.",
                inspirationCapsuleService.getToday(Long.valueOf(principal.getName()))));
    }

    @PostMapping("/today/open")
    @Operation(summary = "오늘의 캡슐 열기", description = "오늘의 캡슐을 엽니다. 하루 한 번만 새로 열리며, 이미 열었다면 같은 내용을 그대로 반환합니다.")
    public ResponseEntity<ApiResponse<InspirationCapsuleOpenResponse>> open(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "캡슐이 열렸습니다.",
                inspirationCapsuleService.open(Long.valueOf(principal.getName()))));
    }

    @GetMapping("/history")
    @Operation(summary = "캡슐 히스토리 조회", description = "지금까지 열었던 캡슐 이력을 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<InspirationCapsuleHistoryResponse>> getHistory(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "캡슐 이력을 조회했습니다.",
                inspirationCapsuleService.getHistory(Long.valueOf(principal.getName()))));
    }
}
