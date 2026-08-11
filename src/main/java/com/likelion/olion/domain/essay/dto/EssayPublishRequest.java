package com.likelion.olion.domain.essay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "에세이 발행 요청")
public record EssayPublishRequest(
        @NotBlank
        @Schema(description = "에세이 제목", example = "흔들려도 걷는 마음", requiredMode = Schema.RequiredMode.REQUIRED)
        String title
) {
}
