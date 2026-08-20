package com.likelion.olion.domain.reflectionshare.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(name = "reflection-share.storage", havingValue = "s3")
public class ReflectionShareS3Config {
    @Bean
    public S3Client reflectionShareS3Client(AwsS3Properties properties) {
        return S3Client.builder()
                .region(Region.of(require(properties.region(), "AWS_S3_REGION")))
                .credentialsProvider(credentials(properties))
                .build();
    }

    @Bean
    public S3Presigner reflectionShareS3Presigner(AwsS3Properties properties) {
        return S3Presigner.builder()
                .region(Region.of(require(properties.region(), "AWS_S3_REGION")))
                .credentialsProvider(credentials(properties))
                .build();
    }

    private StaticCredentialsProvider credentials(AwsS3Properties properties) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(
                require(properties.accessKey(), "AWS_S3_ACCESS_KEY"),
                require(properties.secretKey(), "AWS_S3_SECRET_KEY")));
    }

    private String require(String value, String environmentName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(environmentName + " 환경변수가 필요합니다.");
        }
        return value.trim();
    }
}
