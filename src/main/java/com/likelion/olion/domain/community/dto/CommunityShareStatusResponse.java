package com.likelion.olion.domain.community.dto;

import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공유 이미지 생성 작업 상태와 결과")
public record CommunityShareStatusResponse(
        @Schema(description = "공유 이미지 생성 작업 상태", example = "COMPLETED")
        CommunityShareStatus status,
        @Schema(
                description = "생성된 공유 이미지 URL. 작업이 완료된 경우에만 제공됩니다.",
                example = "https://cdn.olion.com/share/200.png",
                nullable = true
        )
        String imageUrl
) {
}
