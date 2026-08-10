package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "쉼터 게시글 목록 응답")
public record CommunityPostListResponse(
        @Schema(description = "게시글 목록") List<Post> posts
) {
    @Schema(description = "쉼터 게시글")
    public record Post(
            @Schema(description = "게시글 ID", example = "200") Long postId,
            @Schema(description = "익명 닉네임", example = "조용한 새벽") String anonymousNickname,
            @Schema(description = "게시글 본문", example = "오늘따라 유독 마음이...") String content,
            @Schema(description = "본인이 작성한 글인지 여부", example = "false") boolean isMine,
            @Schema(description = "본인이 하트를 눌렀는지 여부", example = "false") boolean isHearted,
            @Schema(description = "하트 수. 본인 글이 아니면 null", example = "4", nullable = true) Integer heartCount
    ) {
    }
}
