package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityPostCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostCreateResponse;
import com.likelion.olion.domain.community.dto.CommunityPostUpdateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostUpdateResponse;
import com.likelion.olion.domain.community.service.CommunityPostService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/posts")
@Tag(name = "쉼터 게시글", description = "쉼터 게시글 작성·수정 API")
public class CommunityPostCommandController {
    private final CommunityPostService communityPostService;

    public CommunityPostCommandController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @PostMapping
    @Operation(
            summary = "익명 게시글 작성",
            description = "오늘의 독서 목표를 달성한 사용자가 소통방에 익명으로 사유를 작성합니다. "
                    + "3분 동안 최대 5개까지 작성할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "사유 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "게시글 내용이 비어 있음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "오늘의 독서 목표 미달성"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "금칙어가 포함된 게시글"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429", description = "3분 내 게시글 5개 작성 제한 초과")
    })
    public ResponseEntity<ApiResponse<CommunityPostCreateResponse>> createPost(
            Principal principal,
            @Valid @RequestBody CommunityPostCreateRequest request
    ) {
        CommunityPostCreateResponse response = communityPostService
                .createPost(Long.valueOf(principal.getName()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "SUCCESS", HttpStatus.CREATED, "사유가 등록되었습니다.", response));
    }

    @PatchMapping("/{postId}")
    @Operation(
            summary = "본인 게시글 수정",
            description = "본인이 작성한 쉼터 게시글의 본문을 수정합니다. 빈 내용이나 금칙어가 포함된 내용은 수정할 수 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "게시글 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "수정할 게시글 내용이 비어 있음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "게시글 작성자가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422", description = "금칙어가 포함된 게시글")
    })
    public ResponseEntity<ApiResponse<CommunityPostUpdateResponse>> updatePost(
            Principal principal,
            @PathVariable Long postId,
            @Valid @RequestBody CommunityPostUpdateRequest request
    ) {
        CommunityPostUpdateResponse response = communityPostService.updatePost(
                Long.valueOf(principal.getName()), postId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "SUCCESS", HttpStatus.OK, "게시글이 수정되었습니다.", response));
    }
}
