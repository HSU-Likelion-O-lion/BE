package com.likelion.olion.domain.reflectionshare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "reflection-share.storage", havingValue = "local", matchIfMissing = true)
public class LocalReflectionShareObjectStorage implements ReflectionShareObjectStorage {
    private final Path uploadDir;

    public LocalReflectionShareObjectStorage(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir.resolve("theme"));
            Files.createDirectories(this.uploadDir.resolve("share"));
        } catch (IOException exception) {
            throw new IllegalStateException("사유록 공유 디렉터리를 생성할 수 없습니다.", exception);
        }
    }

    @Override
    public Optional<byte[]> loadTheme(Long themeId) {
        Path path = safePath("theme/" + themeId + ".png");
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException exception) {
            throw new IllegalStateException("테마 이미지를 읽을 수 없습니다.", exception);
        }
    }

    @Override
    public String storeShare(Long shareId, byte[] pngBytes) {
        String key = "share/" + shareId + "_" + UUID.randomUUID() + ".png";
        try {
            Files.write(safePath(key), pngBytes);
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("공유 이미지를 저장할 수 없습니다.", exception);
        }
    }

    @Override
    public String resolveUrl(String objectKey) {
        return "/images/" + objectKey;
    }

    @Override
    public String resolveThemePreviewUrl(Long themeId) {
        return "/images/theme/" + themeId + ".png";
    }

    private Path safePath(String objectKey) {
        Path path = uploadDir.resolve(objectKey).normalize();
        if (!path.startsWith(uploadDir)) {
            throw new IllegalArgumentException("올바르지 않은 이미지 경로입니다.");
        }
        return path;
    }
}
