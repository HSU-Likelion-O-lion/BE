package com.likelion.olion.domain.community.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalCommunityShareImageStorage implements CommunityShareImageStorage {
    private final Path shareUploadDir;

    public LocalCommunityShareImageStorage(@Value("${file.upload-dir}") String uploadDir) {
        this.shareUploadDir = Path.of(uploadDir, "share").toAbsolutePath().normalize();
        try {
            Files.createDirectories(shareUploadDir);
        } catch (IOException exception) {
            throw new IllegalStateException("공유 이미지 디렉터리를 생성할 수 없습니다.", exception);
        }
    }

    @Override
    public String store(Long shareId, byte[] pngBytes) {
        String filename = shareId + "_" + UUID.randomUUID() + ".png";
        try {
            Files.write(shareUploadDir.resolve(filename), pngBytes);
        } catch (IOException exception) {
            throw new IllegalStateException("공유 이미지 저장에 실패했습니다.", exception);
        }
        return "/images/share/" + filename;
    }
}
