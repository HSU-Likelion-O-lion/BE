package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityShareCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityShareCreateResponse;
import com.likelion.olion.domain.community.dto.CommunityShareStatusResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.entity.CommunityShare;
import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import com.likelion.olion.domain.community.entity.CommunityShareTheme;
import com.likelion.olion.domain.community.event.CommunityShareCreatedEvent;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.domain.community.repository.CommunityShareRepository;
import com.likelion.olion.domain.community.repository.CommunityShareThemeRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityShareServiceTest {
    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityShareThemeRepository communityShareThemeRepository;

    @Mock
    private CommunityShareRepository communityShareRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void queuesShareImageGeneration() {
        CommunityShareService service = createService();
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "공유할 사유");
        CommunityShareTheme theme = new CommunityShareTheme("밤하늘", "/images/themes/night.png");
        CommunityShare savedShare = new CommunityShare(post, theme, 1L);
        ReflectionTestUtils.setField(savedShare, "shareId", 30L);
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));
        given(communityShareThemeRepository.findById(2L)).willReturn(Optional.of(theme));
        given(communityShareRepository.saveAndFlush(any(CommunityShare.class)))
                .willReturn(savedShare);

        CommunityShareCreateResponse response = service.createShare(
                1L, 200L, new CommunityShareCreateRequest(2L));

        assertThat(response.shareId()).isEqualTo(30L);
        assertThat(response.status()).isEqualTo(CommunityShareStatus.QUEUED);
        ArgumentCaptor<CommunityShare> shareCaptor = ArgumentCaptor.forClass(CommunityShare.class);
        verify(communityShareRepository).saveAndFlush(shareCaptor.capture());
        assertThat(shareCaptor.getValue().getPost()).isSameAs(post);
        assertThat(shareCaptor.getValue().getTheme()).isSameAs(theme);
        assertThat(shareCaptor.getValue().getUserId()).isEqualTo(1L);
        verify(eventPublisher).publishEvent(new CommunityShareCreatedEvent(30L));
    }

    @Test
    void rejectsRequestWhenPostDoesNotExist() {
        CommunityShareService service = createService();
        given(communityPostRepository.findById(200L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.createShare(
                1L, 200L, new CommunityShareCreateRequest(2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage())
                            .isEqualTo("게시글 또는 테마를 찾을 수 없습니다.");
                });
        verify(communityShareThemeRepository, never()).findById(any());
        verify(communityShareRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsRequestWhenThemeDoesNotExist() {
        CommunityShareService service = createService();
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "공유할 사유");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));
        given(communityShareThemeRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.createShare(
                1L, 200L, new CommunityShareCreateRequest(2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage())
                            .isEqualTo("게시글 또는 테마를 찾을 수 없습니다.");
                });
        verify(communityShareRepository, never()).saveAndFlush(any());
    }

    @Test
    void returnsCompletedShareWithImageUrl() {
        CommunityShareService service = createService();
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "공유할 사유");
        CommunityShareTheme theme = new CommunityShareTheme("밤하늘", "/images/themes/night.png");
        CommunityShare share = new CommunityShare(post, theme, 1L);
        share.startProcessing();
        share.complete("https://cdn.olion.com/share/200.png");
        given(communityShareRepository.findByShareIdAndUserId(30L, 1L))
                .willReturn(Optional.of(share));

        CommunityShareStatusResponse response = service.getShareStatus(1L, 30L);

        assertThat(response.status()).isEqualTo(CommunityShareStatus.COMPLETED);
        assertThat(response.imageUrl()).isEqualTo("https://cdn.olion.com/share/200.png");
    }

    @Test
    void hidesImageUrlWhileShareIsProcessing() {
        CommunityShareService service = createService();
        CommunityPost post = new CommunityPost(12L, 1L, "고요한 파도", "공유할 사유");
        CommunityShareTheme theme = new CommunityShareTheme("밤하늘", "/images/themes/night.png");
        CommunityShare share = new CommunityShare(post, theme, 1L);
        share.startProcessing();
        ReflectionTestUtils.setField(share, "imageUrl", "https://cdn.olion.com/share/partial.png");
        given(communityShareRepository.findByShareIdAndUserId(30L, 1L))
                .willReturn(Optional.of(share));

        CommunityShareStatusResponse response = service.getShareStatus(1L, 30L);

        assertThat(response.status()).isEqualTo(CommunityShareStatus.PROCESSING);
        assertThat(response.imageUrl()).isNull();
    }

    @Test
    void rejectsMissingOrOtherUsersShare() {
        CommunityShareService service = createService();
        given(communityShareRepository.findByShareIdAndUserId(30L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getShareStatus(1L, 30L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage()).isEqualTo("작업을 찾을 수 없습니다.");
                });
    }

    private CommunityShareService createService() {
        return new CommunityShareService(
                communityPostRepository,
                communityShareThemeRepository,
                communityShareRepository,
                eventPublisher);
    }
}
