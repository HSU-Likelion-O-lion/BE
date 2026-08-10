package com.likelion.olion.domain.community.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PngCommunityShareImageRendererTest {
    @TempDir
    Path tempDir;

    @Test
    void rendersFallbackBackgroundAsSocialPng() throws Exception {
        PngCommunityShareImageRenderer renderer = new PngCommunityShareImageRenderer(
                tempDir.toString(), "");

        byte[] result = renderer.render(new CommunityShareRenderRequest(
                30L,
                "오늘의 마음을 천천히 들여다봅니다.",
                "밤하늘",
                "https://untrusted.example.com/theme.png"));

        assertThat(result).startsWith(0x89, 0x50, 0x4e, 0x47);
        var image = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(image.getWidth()).isEqualTo(1080);
        assertThat(image.getHeight()).isEqualTo(1350);
    }
}
