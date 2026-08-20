package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.repository.ReflectionShareRepository;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ReflectionShareImageWorkerTest {
    @Test
    void resumesAStaleProcessingShare() {
        ReflectionShareRepository shareRepository = mock(ReflectionShareRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReflectionShareImageRenderer renderer = mock(ReflectionShareImageRenderer.class);
        ReflectionShareObjectStorage storage = mock(ReflectionShareObjectStorage.class);
        Reflection reflection = mock(Reflection.class);
        User user = mock(User.class);
        ReflectionShare share = new ReflectionShare(reflection, 1L, 2L);
        ReflectionTestUtils.setField(share, "shareId", 30L);
        share.startProcessing();

        given(shareRepository.findByIdForUpdate(30L)).willReturn(Optional.of(share));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reflection.getContent()).willReturn("사유 내용");
        given(reflection.getCreatedAt()).willReturn(Instant.parse("2026-06-25T00:00:00Z"));
        given(user.getNickname()).willReturn("지훈");
        given(renderer.render(any())).willReturn(new byte[]{1, 2, 3});
        given(storage.storeShare(org.mockito.ArgumentMatchers.eq(30L), any(byte[].class)))
                .willReturn("share/30_uuid.png");

        new ReflectionShareImageWorker(
                shareRepository, userRepository, renderer, storage).process(30L);

        assertThat(share.getStatus()).isEqualTo(ReflectionShareStatus.COMPLETED);
        assertThat(share.getImageKey()).isEqualTo("share/30_uuid.png");
    }
}
