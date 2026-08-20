package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.user.service.FileStorageService;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PngReflectionShareImageRendererTest {
    @Test
    void rendersFigmaRatioShareCardWithoutRemoteAssets() throws Exception {
        ReflectionShareObjectStorage storage = new ReflectionShareObjectStorage() {
            @Override
            public Optional<byte[]> loadTheme(Long themeId) {
                return Optional.empty();
            }

            @Override
            public String storeShare(Long shareId, byte[] pngBytes) {
                return "share/test.png";
            }

            @Override
            public String resolveUrl(String objectKey) {
                return objectKey;
            }

            @Override
            public String resolveThemePreviewUrl(Long themeId) {
                return "theme/" + themeId + ".png";
            }
        };
        FileStorageService profileStorage = mock(FileStorageService.class);
        given(profileStorage.loadProfileImage(null)).willReturn(Optional.empty());
        PngReflectionShareImageRenderer renderer =
                new PngReflectionShareImageRenderer(storage, profileStorage);

        byte[] png = renderer.render(new ReflectionShareRenderRequest(
                2L,
                "원래 모든 일을 완벽하게 해내야 한다는 생각이 강했지만, "
                        + "이 책을 읽으며 통제할 수 없는 일은 흘려보내도 괜찮다는 사실을 깨달았어요.",
                "지훈",
                null,
                Instant.parse("2026-06-25T00:00:00Z")));

        var image = ImageIO.read(new ByteArrayInputStream(png));
        Path previewPath = Path.of("build", "test-outputs", "reflection-share-preview.png");
        Files.createDirectories(previewPath.getParent());
        Files.write(previewPath, png);
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(PngReflectionShareImageRenderer.WIDTH);
        assertThat(image.getHeight()).isEqualTo(PngReflectionShareImageRenderer.HEIGHT);
        assertThat(png.length).isGreaterThan(10_000);
    }
}
