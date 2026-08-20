package com.likelion.olion.domain.user.service;

import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES = List.of("image/png", "image/jpeg", "image/webp");

    private final Path uploadDir;
    private final String storageType;
    private final String bucket;
    private final Duration presignedUrlDuration;
    private final ObjectProvider<S3Client> s3ClientProvider;
    private final ObjectProvider<S3Presigner> s3PresignerProvider;

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${reflection-share.storage:local}") String storageType,
            @Value("${aws.s3.bucket:}") String bucket,
            @Value("${reflection-share.presigned-url-minutes:10}") long presignedUrlMinutes,
            ObjectProvider<S3Client> s3ClientProvider,
            ObjectProvider<S3Presigner> s3PresignerProvider
    ) {
        this.uploadDir = Path.of(uploadDir, "profile");
        this.storageType = storageType;
        this.bucket = bucket;
        this.presignedUrlDuration = Duration.ofMinutes(Math.max(1, presignedUrlMinutes));
        this.s3ClientProvider = s3ClientProvider;
        this.s3PresignerProvider = s3PresignerProvider;
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
        String extension = extensionFor(image.getContentType(), image.getOriginalFilename());
        String filename = userId + "_" + UUID.randomUUID() + "." + extension;
        if ("s3".equalsIgnoreCase(storageType)) {
            return storeProfileImageToS3("profile/" + filename, image);
        }
        try {
            Files.copy(image.getInputStream(), uploadDir.resolve(filename));
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장에 실패했습니다.", e);
        }
        return "/images/profile/" + filename;
    }

    public String resolveProfileImageUrl(String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            return null;
        }
        if (!storedReference.startsWith("profile/")) {
            return storedReference;
        }
        if (!"s3".equalsIgnoreCase(storageType)) {
            return "/images/" + storedReference;
        }
        S3Presigner presigner = s3PresignerProvider.getIfAvailable();
        if (presigner == null) {
            throw new IllegalStateException("S3 Presigner가 설정되지 않았습니다.");
        }
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(requireBucket())
                .key(storedReference)
                .build();
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(presignedUrlDuration)
                        .getObjectRequest(request)
                        .build())
                .url()
                .toString();
    }

    public Optional<byte[]> loadProfileImage(String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            return Optional.empty();
        }
        if (storedReference.startsWith("profile/") && "s3".equalsIgnoreCase(storageType)) {
            S3Client s3Client = s3ClientProvider.getIfAvailable();
            if (s3Client == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(requireBucket())
                        .key(storedReference)
                        .build()).asByteArray());
            } catch (RuntimeException exception) {
                return Optional.empty();
            }
        }

        String filename;
        if (storedReference.startsWith("/images/profile/")) {
            filename = storedReference.substring("/images/profile/".length());
        } else if (storedReference.startsWith("profile/")) {
            filename = storedReference.substring("profile/".length());
        } else {
            return Optional.empty();
        }
        Path path = uploadDir.resolve(filename).normalize();
        if (!path.startsWith(uploadDir) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private String storeProfileImageToS3(String key, MultipartFile image) {
        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (s3Client == null) {
            throw new IllegalStateException("S3 클라이언트가 설정되지 않았습니다.");
        }
        try {
            byte[] bytes = image.getBytes();
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(requireBucket())
                            .key(key)
                            .contentType(image.getContentType())
                            .cacheControl("private, max-age=31536000, immutable")
                            .build(),
                    RequestBody.fromBytes(bytes));
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("프로필 이미지를 읽을 수 없습니다.", exception);
        }
    }

    private String extensionFor(String contentType, String originalFilename) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default -> {
                String extension = StringUtils.getFilenameExtension(originalFilename);
                yield extension == null || extension.isBlank() ? "bin" : extension.toLowerCase();
            }
        };
    }

    private String requireBucket() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET 환경변수가 필요합니다.");
        }
        return bucket.trim();
    }
}
