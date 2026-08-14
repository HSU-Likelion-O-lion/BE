package com.likelion.olion.domain.user.service;

import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/png", "image/jpeg", "image/webp");

    private final Path uploadDir;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir, "profile");
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리를 생성할 수 없습니다.", e);
        }
    }

    public String storeProfileImage(Long userId, MultipartFile image) {
        if (image.isEmpty() || !ALLOWED_TYPES.contains(image.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 파일 형식입니다.");
        }
        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String filename = userId + "_" + UUID.randomUUID() + (extension != null ? "." + extension : "");
        try {
            Files.copy(image.getInputStream(), uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
        }
        return "/images/profile/" + filename;
    }
}
