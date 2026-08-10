package com.likelion.olion.domain.community.dto;

import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공유 이미지 생성 작업 접수 결과")
public record CommunityShareCreateResponse(
        @Schema(description = "공유 이미지 생성 작업 ID", example = "30")
        Long shareId,
        @Schema(description = "공유 이미지 생성 작업 상태", example = "QUEUED")
        CommunityShareStatus status
) {
}
