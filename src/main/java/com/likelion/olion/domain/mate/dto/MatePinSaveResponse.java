package com.likelion.olion.domain.mate.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메이트 핀 고정 결과")
public record MatePinSaveResponse(
        @Schema(description = "지정된 핀 순서", example = "2") Integer pinnedOrder) {
}
