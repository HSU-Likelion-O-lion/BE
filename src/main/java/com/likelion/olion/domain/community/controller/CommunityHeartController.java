package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityHeartResponse;
import com.likelion.olion.domain.community.service.CommunityHeartService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/posts/{postId}/hearts")
@Tag(name = "쉼터 게시글 공감", description = "쉼터 게시글 하트 등록·취소 API")
public class CommunityHeartController {
    private final CommunityHeartService communityHeartService;

    public CommunityHeartController(CommunityHeartService communityHeartService) {
        this.communityHeartService = communityHeartService;
    }

    @PostMapping
    @Operation(summary = "게시글 하트 등록", description = "쉼터 게시글에 공감 하트를 등록합니다. 한 게시글에는 한 번만 등록할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "하트 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 하트를 등록한 게시글")
    })
    public ResponseEntity<ApiResponse<CommunityHeartResponse>> addHeart(
            Principal principal,
            @Parameter(description = "게시글 ID", example = "200") @PathVariable Long postId
    ) {
        CommunityHeartResponse response = communityHeartService
                .addHeart(Long.valueOf(principal.getName()), postId);
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS", HttpStatus.OK, "공감을 남겼습니다.", response));
    }

    @DeleteMapping
    @Operation(summary = "게시글 하트 취소", description = "현재 사용자가 쉼터 게시글에 남긴 공감 하트를 취소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "하트 취소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "현재 사용자의 하트 기록을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CommunityHeartResponse>> removeHeart(
            Principal principal,
            @Parameter(description = "게시글 ID", example = "200") @PathVariable Long postId
    ) {
        CommunityHeartResponse response = communityHeartService
                .removeHeart(Long.valueOf(principal.getName()), postId);
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS", HttpStatus.OK, "공감을 취소했습니다.", response));
    }
}
