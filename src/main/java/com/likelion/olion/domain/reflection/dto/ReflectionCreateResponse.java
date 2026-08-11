package com.likelion.olion.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사유 작성 응답")
public record ReflectionCreateResponse(
        @Schema(description = "생성된 사유 ID", example = "88") Long reflectionId,
        @Schema(description = "표지 진행도 (0~30)", example = "12") int coverProgress
) {
}
