package com.likelion.olion.domain.reflectionshare.dto;

import java.util.List;

public record ReflectionShareThemeResponse(List<Theme> themes) {
    public record Theme(
            Long themeId,
            String name,
            String swatch,
            String previewUrl
    ) {
    }
}
