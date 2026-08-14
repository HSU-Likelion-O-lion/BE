package com.likelion.olion.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "익명 게시글 작성 요청")
public record CommunityPostCreateRequest(
        @NotNull
        @Schema(description = "게시글을 작성할 소통방 ID", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        Long roomId,

        @NotBlank
        @Schema(description = "게시글 본문", example = "오늘은 책 속 문장이 오래 마음에 남았습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "게시글과 연결할 회고 ID. 연결하지 않으면 생략합니다.", example = "31", nullable = true)
        Long reflectionId
) {
}
