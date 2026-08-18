package com.likelion.olion.domain.essay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "에세이 생성 요청")
public record EssayCreateRequest(
        @NotNull
        @Size(min = 30, max = 30, message = "에세이 생성에는 사유 30개가 필요합니다.")
        @Schema(description = "에세이 생성에 사용할 사유 ID 목록 (정확히 30개)", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Long> reflectionIds
) {
}
