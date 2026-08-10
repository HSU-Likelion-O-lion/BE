package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "본인 게시글 수정 결과")
public record CommunityPostUpdateResponse(
        @Schema(description = "수정된 게시글 ID", example = "200")
        Long postId
) {
}
