package com.likelion.olion.domain.capsule.controller;

import com.likelion.olion.domain.capsule.dto.InspirationCapsuleTodayResponse;
import com.likelion.olion.domain.capsule.service.InspirationCapsuleService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
