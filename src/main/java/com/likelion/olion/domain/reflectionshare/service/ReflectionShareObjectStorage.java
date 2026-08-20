package com.likelion.olion.domain.reflectionshare.service;

import java.util.Optional;

public interface ReflectionShareObjectStorage {
    Optional<byte[]> loadTheme(Long themeId);

    String storeShare(Long shareId, byte[] pngBytes);

    String resolveUrl(String objectKey);

    String resolveThemePreviewUrl(Long themeId);
}
