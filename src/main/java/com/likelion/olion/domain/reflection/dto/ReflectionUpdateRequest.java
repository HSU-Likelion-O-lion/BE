package com.likelion.olion.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사유 수정 요청")
public record ReflectionUpdateRequest(
        @NotBlank
        @Schema(description = "수정할 본문", example = "오늘 읽은 부분에서... (수정)", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
