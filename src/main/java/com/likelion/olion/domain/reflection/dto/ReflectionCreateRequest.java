package com.likelion.olion.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사유 작성 요청")
public record ReflectionCreateRequest(
        @NotNull
        @Schema(description = "관련 독서 세션 ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        Long sessionId,

        @NotBlank
        @Schema(description = "사유 본문", example = "오늘 읽은 부분에서...", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
