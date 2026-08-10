package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "익명 게시글 작성 결과")
public record CommunityPostCreateResponse(
        @Schema(description = "생성된 게시글 ID", example = "101")
        Long postId,

        @Schema(description = "소통방에서 사용할 익명 닉네임", example = "고요한 파도")
        String anonymousNickname
) {
}
