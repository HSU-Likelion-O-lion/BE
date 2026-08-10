package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityShareCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityShareCreateResponse;
import com.likelion.olion.domain.community.service.CommunityShareService;
import com.likelion.olion.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/community/posts/{postId}/share")
@Tag(name = "쉼터 공유", description = "쉼터 게시글 외부 공유 이미지 API")
public class CommunityShareController {
    private final CommunityShareService communityShareService;

    public CommunityShareController(CommunityShareService communityShareService) {
        this.communityShareService = communityShareService;
    }

    @PostMapping
    @Operation(
            summary = "공유 이미지 생성 요청",
            description = "선택한 테마로 게시글의 외부 공유 이미지를 생성하는 비동기 작업을 접수합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202", description = "공유 이미지 생성 작업 접수 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "테마 ID가 누락되었거나 올바르지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "게시글 또는 테마를 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CommunityShareCreateResponse>> createShare(
            Principal principal,
            @Parameter(description = "공유할 게시글 ID", example = "200") @PathVariable Long postId,
            @Valid @RequestBody CommunityShareCreateRequest request
    ) {
        CommunityShareCreateResponse response = communityShareService.createShare(
                Long.valueOf(principal.getName()), postId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(
                "SUCCESS",
                HttpStatus.ACCEPTED,
                "공유 이미지 생성이 접수되었습니다.",
                response));
    }
}
