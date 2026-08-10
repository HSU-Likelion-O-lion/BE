package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityHeartResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.entity.CommunityPostHeart;
import com.likelion.olion.domain.community.repository.CommunityPostHeartRepository;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityHeartService {
    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostHeartRepository communityPostHeartRepository;

    public CommunityHeartService(
            CommunityPostRepository communityPostRepository,
            CommunityPostHeartRepository communityPostHeartRepository
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostHeartRepository = communityPostHeartRepository;
    }

    @Transactional
    public CommunityHeartResponse addHeart(Long userId, Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if (communityPostHeartRepository.existsByPostPostIdAndUserId(postId, userId)) {
            throw duplicateHeartException();
        }

        try {
            communityPostHeartRepository.saveAndFlush(new CommunityPostHeart(post, userId));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateHeartException();
        }
        return new CommunityHeartResponse(true);
    }

    private BusinessException duplicateHeartException() {
        return new BusinessException(ErrorCode.CONFLICT, "이미 공감한 게시글입니다.");
    }
}
