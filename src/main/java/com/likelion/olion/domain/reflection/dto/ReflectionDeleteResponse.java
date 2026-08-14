package com.likelion.olion.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사유 삭제 응답")
public record ReflectionDeleteResponse(
        @Schema(description = "삭제 후 표지 진행도 (0~30)", example = "11") int coverProgress
) {
}
