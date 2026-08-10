package com.likelion.olion.domain.community.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalCommunityShareImageStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void storesPngUnderShareDirectory() throws Exception {
        LocalCommunityShareImageStorage storage = new LocalCommunityShareImageStorage(
                tempDir.toString());

        String imageUrl = storage.store(30L, new byte[]{1, 2, 3});

        assertThat(imageUrl).startsWith("/images/share/30_").endsWith(".png");
        Path storedFile = tempDir.resolve(imageUrl.substring("/images/".length()));
        assertThat(Files.readAllBytes(storedFile)).containsExactly(1, 2, 3);
    }
}
