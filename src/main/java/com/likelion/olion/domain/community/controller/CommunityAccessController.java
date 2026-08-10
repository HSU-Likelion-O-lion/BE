package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityAccessResponse;
import com.likelion.olion.domain.community.service.CommunityAccessService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/access")
@Tag(name = "쉼터 입장", description = "쉼터·커뮤니티 입장 가능 여부 API")
public class CommunityAccessController {
    private final CommunityAccessService communityAccessService;

    public CommunityAccessController(CommunityAccessService communityAccessService) {
        this.communityAccessService = communityAccessService;
    }

    @GetMapping
    @Operation(summary = "쉼터 입장 가능 여부 조회", description = "오늘 완료된 독서 세션이 있는지 확인해 쉼터 입장 가능 여부를 반환합니다.")
    public ResponseEntity<ApiResponse<CommunityAccessResponse>> getAccess(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "입장 가능 여부를 조회했습니다.",
                communityAccessService.getAccess(Long.valueOf(principal.getName()))));
    }
}
