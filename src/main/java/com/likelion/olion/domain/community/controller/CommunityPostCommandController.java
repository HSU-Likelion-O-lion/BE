package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityPostCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostCreateResponse;
import com.likelion.olion.domain.community.service.CommunityPostService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/posts")
@Tag(name = "쉼터 게시글", description = "쉼터 게시글 작성 API")
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
}
