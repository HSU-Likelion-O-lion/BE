package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "쉼터 게시글 첫 줄 미리보기 응답")
public record CommunityPostPreviewResponse(
        @Schema(description = "게시글 첫 줄 미리보기 목록") List<Preview> previews
) {
    @Schema(description = "게시글 미리보기")
    public record Preview(
            @Schema(description = "게시글 ID", example = "200") Long postId,
            @Schema(description = "게시글 첫 줄", example = "오늘따라 유독 마음이...") String firstLine
    ) {
    }
}
