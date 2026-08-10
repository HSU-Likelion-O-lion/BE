package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityHeartResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.entity.CommunityPostHeart;
import com.likelion.olion.domain.community.repository.CommunityPostHeartRepository;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityHeartServiceTest {
    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityPostHeartRepository communityPostHeartRepository;

    @Test
    void addsHeartToPost() {
        CommunityHeartService service = new CommunityHeartService(
                communityPostRepository, communityPostHeartRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "공감할 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));
        given(communityPostHeartRepository.existsByPostPostIdAndUserId(200L, 1L)).willReturn(false);

        CommunityHeartResponse response = service.addHeart(1L, 200L);

        assertThat(response.isHearted()).isTrue();
        verify(communityPostHeartRepository).saveAndFlush(any(CommunityPostHeart.class));
    }

    @Test
    void rejectsHeartWhenPostDoesNotExist() {
        CommunityHeartService service = new CommunityHeartService(
                communityPostRepository, communityPostHeartRepository);
        given(communityPostRepository.findById(200L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.addHeart(1L, 200L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
        verify(communityPostHeartRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateHeart() {
        CommunityHeartService service = new CommunityHeartService(
                communityPostRepository, communityPostHeartRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "공감할 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));
        given(communityPostHeartRepository.existsByPostPostIdAndUserId(200L, 1L)).willReturn(true);

        assertThatThrownBy(() -> service.addHeart(1L, 200L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(communityPostHeartRepository, never()).saveAndFlush(any());
    }

    @Test
    void mapsConcurrentDuplicateToConflict() {
        CommunityHeartService service = new CommunityHeartService(
                communityPostRepository, communityPostHeartRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "공감할 내용");
        given(communityPostRepository.findById(200L)).willReturn(Optional.of(post));
        given(communityPostHeartRepository.existsByPostPostIdAndUserId(200L, 1L)).willReturn(false);
        given(communityPostHeartRepository.saveAndFlush(any(CommunityPostHeart.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.addHeart(1L, 200L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void removesOwnHeart() {
        CommunityHeartService service = new CommunityHeartService(
                communityPostRepository, communityPostHeartRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "공감한 내용");
        CommunityPostHeart heart = new CommunityPostHeart(post, 1L);
        given(communityPostHeartRepository.findByPostPostIdAndUserId(200L, 1L))
                .willReturn(Optional.of(heart));

        CommunityHeartResponse response = service.removeHeart(1L, 200L);

        assertThat(response.isHearted()).isFalse();
        verify(communityPostHeartRepository).delete(heart);
    }

    @Test
    void rejectsCancelWhenHeartDoesNotExist() {
        CommunityHeartService service = new CommunityHeartService(
                communityPostRepository, communityPostHeartRepository);
        given(communityPostHeartRepository.findByPostPostIdAndUserId(200L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeHeart(1L, 200L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
        verify(communityPostHeartRepository, never()).delete(any());
    }
}
