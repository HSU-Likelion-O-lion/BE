package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityPostPreviewResponse;
import com.likelion.olion.domain.community.dto.CommunityPostListResponse;
import com.likelion.olion.domain.community.service.CommunityPostService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/rooms/{roomId}/posts")
@Tag(name = "쉼터 게시글", description = "쉼터 게시글 조회 API")
public class CommunityPostController {
    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @GetMapping("/preview")
    @Operation(summary = "사유 첫 줄 미리보기", description = "쉼터 게시글의 첫 줄만 조회합니다. 게시글 본문 전체는 반환하지 않습니다.")
    public ResponseEntity<ApiResponse<CommunityPostPreviewResponse>> getPreviews(
            Principal principal,
            @Parameter(description = "소통방 ID", example = "12") @PathVariable Long roomId
    ) {
        CommunityPostPreviewResponse response = communityPostService
                .getPreviews(Long.valueOf(principal.getName()), roomId);
        String message = response.previews().isEmpty()
                ? "첫 번째 사유를 남겨주세요."
                : "조각 모음을 조회했습니다.";
        String code = response.previews().isEmpty() ? "SUCCESS_EMPTY" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(
                code, org.springframework.http.HttpStatus.OK, message, response));
    }

    @GetMapping
    @Operation(summary = "쉼터 게시글 목록 조회", description = "쉼터 내부 게시글과 익명 정보, 본인 글 여부를 조회합니다.")
    public ResponseEntity<ApiResponse<CommunityPostListResponse>> getPosts(
            Principal principal,
            @Parameter(description = "소통방 ID", example = "12") @PathVariable Long roomId
    ) {
        CommunityPostListResponse response = communityPostService
                .getPosts(Long.valueOf(principal.getName()), roomId);
        String message = response.posts().isEmpty() ? "게시글이 없습니다." : "게시글 목록을 조회했습니다.";
        String code = response.posts().isEmpty() ? "SUCCESS_EMPTY" : "SUCCESS";
        return ResponseEntity.ok(ApiResponse.success(
                code, org.springframework.http.HttpStatus.OK, message, response));
    }
}
