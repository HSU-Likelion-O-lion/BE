package com.likelion.olion.domain.essay.dto;

import com.likelion.olion.domain.essay.entity.EssayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에세이 생성 요청 응답")
public record EssayCreateResponse(
        @Schema(description = "에세이 ID", example = "7") Long essayId,
        @Schema(description = "작업 상태", example = "QUEUED") EssayStatus jobStatus
) {
}
