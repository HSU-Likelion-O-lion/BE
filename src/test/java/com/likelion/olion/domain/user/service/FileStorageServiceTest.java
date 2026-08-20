package com.likelion.olion.domain.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FileStorageServiceTest {
    @SuppressWarnings("unchecked")
    private final ObjectProvider<S3Client> s3ClientProvider = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<S3Presigner> s3PresignerProvider = mock(ObjectProvider.class);

    private FileStorageService localService() {
        return new FileStorageService(
                "/tmp/olion-test-uploads", "local", "", 10, s3ClientProvider, s3PresignerProvider);
    }

    @Test
    void returnsDefaultAvatarWhenNoImageStored() {
        FileStorageService service = localService();

        assertThat(service.resolveProfileImageUrl(null)).isEqualTo("/mascot.png");
        assertThat(service.resolveProfileImageUrl("")).isEqualTo("/mascot.png");
    }

    @Test
    void resolvesLocalStoredReferenceToImagesPath() {
        FileStorageService service = localService();

        assertThat(service.resolveProfileImageUrl("profile/1_uuid.png"))
                .isEqualTo("/images/profile/1_uuid.png");
    }

    @Test
    void returnsAlreadyResolvedUrlUnchanged() {
        FileStorageService service = localService();

        assertThat(service.resolveProfileImageUrl("/images/profile/1_uuid.png"))
                .isEqualTo("/images/profile/1_uuid.png");
    }
}
