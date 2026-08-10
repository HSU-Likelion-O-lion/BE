package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.entity.CommunityShare;
import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import com.likelion.olion.domain.community.entity.CommunityShareTheme;
import com.likelion.olion.domain.community.repository.CommunityShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityShareImageWorkerTest {
    @Mock
    private CommunityShareRepository communityShareRepository;
    @Mock
    private CommunityShareImageRenderer imageRenderer;
    @Mock
    private CommunityShareImageStorage imageStorage;

    private CommunityShareImageWorker worker;
    private CommunityShare share;

    @BeforeEach
    void setUp() {
        worker = new CommunityShareImageWorker(
                communityShareRepository, imageRenderer, imageStorage);
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "오늘의 사유");
        CommunityShareTheme theme = new CommunityShareTheme(
                "밤하늘", "/images/themes/night.png");
        share = new CommunityShare(post, theme, 1L);
        ReflectionTestUtils.setField(share, "shareId", 30L);
    }

    @Test
    void rendersStoresAndCompletesQueuedShare() {
        byte[] png = new byte[]{1, 2, 3};
        given(communityShareRepository.findByIdForUpdate(30L)).willReturn(Optional.of(share));
        given(imageRenderer.render(any(CommunityShareRenderRequest.class))).willReturn(png);
        given(imageStorage.store(30L, png)).willReturn("/images/share/30.png");

        worker.process(30L);

        assertThat(share.getStatus()).isEqualTo(CommunityShareStatus.COMPLETED);
        assertThat(share.getImageUrl()).isEqualTo("/images/share/30.png");
    }

    @Test
    void skipsAlreadyCompletedShare() {
        share.startProcessing();
        share.complete("/images/share/30.png");
        given(communityShareRepository.findByIdForUpdate(30L)).willReturn(Optional.of(share));

        worker.process(30L);

        verify(imageRenderer, never()).render(any());
        verify(imageStorage, never()).store(any(), any());
    }

    @Test
    void requeuesShareWhenGenerationFails() {
        given(communityShareRepository.findByIdForUpdate(30L)).willReturn(Optional.of(share));
        given(imageRenderer.render(any(CommunityShareRenderRequest.class)))
                .willThrow(new IllegalStateException("render failed"));

        worker.process(30L);

        assertThat(share.getStatus()).isEqualTo(CommunityShareStatus.QUEUED);
        assertThat(share.getImageUrl()).isNull();
        verify(imageStorage, never()).store(any(), any());
    }
}
