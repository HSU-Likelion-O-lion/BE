package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareCreateRequest;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.event.ReflectionShareCreatedEvent;
import com.likelion.olion.domain.reflectionshare.repository.ReflectionShareRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReflectionShareServiceTest {
    private ReflectionRepository reflectionRepository;
    private ReflectionShareRepository shareRepository;
    private ReflectionShareObjectStorage objectStorage;
    private ApplicationEventPublisher eventPublisher;
    private ReflectionShareService service;

    @BeforeEach
    void setUp() {
        reflectionRepository = mock(ReflectionRepository.class);
        shareRepository = mock(ReflectionShareRepository.class);
        objectStorage = mock(ReflectionShareObjectStorage.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new ReflectionShareService(
                reflectionRepository, shareRepository, objectStorage, eventPublisher);
    }

    @Test
    void createsShareForOwnedReflection() {
        Reflection reflection = mock(Reflection.class);
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L))
                .willReturn(Optional.of(reflection));
        given(shareRepository.saveAndFlush(any(ReflectionShare.class)))
                .willAnswer(invocation -> {
                    ReflectionShare share = invocation.getArgument(0);
                    ReflectionTestUtils.setField(share, "shareId", 30L);
                    return share;
                });

        var response = service.create(1L, 88L, new ReflectionShareCreateRequest(2L));

        assertThat(response.shareId()).isEqualTo(30L);
        assertThat(response.status()).isEqualTo(ReflectionShareStatus.QUEUED);
        verify(eventPublisher).publishEvent(new ReflectionShareCreatedEvent(30L));
    }

    @Test
    void rejectsUnknownTheme() {
        given(reflectionRepository.findByReflectionIdAndUserId(88L, 1L))
                .willReturn(Optional.of(mock(Reflection.class)));

        assertThatThrownBy(() -> service.create(
                1L, 88L, new ReflectionShareCreateRequest(99L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("지원하지 않는 공유 테마입니다.");
    }

    @Test
    void returnsFreshResolvedUrlOnlyWhenCompleted() {
        ReflectionShare share = new ReflectionShare(mock(Reflection.class), 1L, 2L);
        ReflectionTestUtils.setField(share, "shareId", 30L);
        share.startProcessing();
        share.complete("share/30_uuid.png");
        given(shareRepository.findByShareIdAndUserId(30L, 1L))
                .willReturn(Optional.of(share));
        given(objectStorage.resolveUrl("share/30_uuid.png"))
                .willReturn("https://example.com/presigned");

        var response = service.getStatus(1L, 30L);

        assertThat(response.status()).isEqualTo(ReflectionShareStatus.COMPLETED);
        assertThat(response.imageUrl()).isEqualTo("https://example.com/presigned");
    }
}
