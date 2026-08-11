package com.likelion.olion.domain.essay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "에세이 생성 요청")
public record EssayCreateRequest(
        @NotNull
        @Size(min = 30, message = "사유는 최소 30개 이상 선택해야 합니다.")
        @Schema(description = "선택한 사유 ID 목록 (30개 이상)", requiredMode = Schema.RequiredMode.REQUIRED)
        List<Long> reflectionIds
) {
}
