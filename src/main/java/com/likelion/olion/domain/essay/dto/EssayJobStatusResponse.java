package com.likelion.olion.domain.essay.dto;

import com.likelion.olion.domain.essay.entity.EssayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에세이 작업 상태 응답")
public record EssayJobStatusResponse(
        @Schema(description = "작업 상태", example = "PROCESSING") EssayStatus status
) {
}
