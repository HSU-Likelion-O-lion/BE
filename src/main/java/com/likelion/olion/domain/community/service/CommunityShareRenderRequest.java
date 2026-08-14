package com.likelion.olion.domain.community.service;

public record CommunityShareRenderRequest(
        Long shareId,
        String content,
        String themeName,
        String themePreviewUrl
) {
}
