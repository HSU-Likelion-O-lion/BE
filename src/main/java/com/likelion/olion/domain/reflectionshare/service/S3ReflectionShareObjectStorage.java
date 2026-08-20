package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflectionshare.config.AwsS3Properties;
import com.likelion.olion.domain.reflectionshare.config.ReflectionShareProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "reflection-share.storage", havingValue = "s3")
public class S3ReflectionShareObjectStorage implements ReflectionShareObjectStorage {
    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration urlDuration;

    public S3ReflectionShareObjectStorage(
            S3Client s3Client,
            S3Presigner presigner,
            AwsS3Properties s3Properties,
            ReflectionShareProperties shareProperties
    ) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = requireBucket(s3Properties.bucket());
        this.urlDuration = Duration.ofMinutes(shareProperties.presignedUrlMinutes());
    }

    @Override
    public Optional<byte[]> loadTheme(Long themeId) {
        try {
            return Optional.of(s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(themeKey(themeId))
                    .build()).asByteArray());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new IllegalStateException(
                        themeKey(themeId) + " 테마 이미지가 S3에 없습니다.", exception);
            }
            throw exception;
        }
    }

    @Override
    public String storeShare(Long shareId, byte[] pngBytes) {
        String key = "share/" + shareId + "_" + UUID.randomUUID() + ".png";
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("image/png")
                        .cacheControl("private, max-age=31536000, immutable")
                        .build(),
                RequestBody.fromBytes(pngBytes));
        return key;
    }

    @Override
    public String resolveUrl(String objectKey) {
        GetObjectRequest getObject = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(urlDuration)
                        .getObjectRequest(getObject)
                        .build())
                .url()
                .toString();
    }

    @Override
    public String resolveThemePreviewUrl(Long themeId) {
        return resolveUrl(themeKey(themeId));
    }

    private String themeKey(Long themeId) {
        return "theme/" + themeId + ".png";
    }

    private String requireBucket(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("AWS_S3_BUCKET 환경변수가 필요합니다.");
        }
        return value.trim();
    }
}
