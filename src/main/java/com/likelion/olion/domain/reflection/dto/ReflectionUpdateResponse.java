package com.likelion.olion.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사유 수정 응답")
public record ReflectionUpdateResponse(
        @Schema(description = "사유 ID", example = "88") Long reflectionId
) {
}
