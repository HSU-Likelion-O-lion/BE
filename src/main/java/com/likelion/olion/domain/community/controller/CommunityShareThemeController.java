package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityShareThemeResponse;
import com.likelion.olion.domain.community.service.CommunityShareThemeService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/community/share-themes")
@Tag(name = "쉼터 공유", description = "외부 공유용 배경 테마 API")
public class CommunityShareThemeController {
    private final CommunityShareThemeService communityShareThemeService;

    public CommunityShareThemeController(CommunityShareThemeService communityShareThemeService) {
        this.communityShareThemeService = communityShareThemeService;
    }

    @GetMapping
    @Operation(summary = "공유 배경 테마 목록 조회", description = "외부 공유 이미지에 사용할 감성 배경 테마 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "공유 배경 테마 목록 조회 성공")
    public ResponseEntity<ApiResponse<CommunityShareThemeResponse>> getThemes() {
        return ResponseEntity.ok(ApiResponse.success(
                "공유 테마 목록을 조회했습니다.", communityShareThemeService.getThemes()));
    }
}
