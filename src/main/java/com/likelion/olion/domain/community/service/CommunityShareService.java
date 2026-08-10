package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityShareCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityShareCreateResponse;
import com.likelion.olion.domain.community.dto.CommunityShareStatusResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.entity.CommunityShare;
import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import com.likelion.olion.domain.community.entity.CommunityShareTheme;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.domain.community.repository.CommunityShareRepository;
import com.likelion.olion.domain.community.repository.CommunityShareThemeRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityShareService {
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "게시글 또는 테마를 찾을 수 없습니다.";
    private static final String SHARE_NOT_FOUND_MESSAGE = "작업을 찾을 수 없습니다.";

    private final CommunityPostRepository communityPostRepository;
    private final CommunityShareThemeRepository communityShareThemeRepository;
    private final CommunityShareRepository communityShareRepository;

    public CommunityShareService(
            CommunityPostRepository communityPostRepository,
            CommunityShareThemeRepository communityShareThemeRepository,
            CommunityShareRepository communityShareRepository
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityShareThemeRepository = communityShareThemeRepository;
        this.communityShareRepository = communityShareRepository;
    }

    @Transactional
    public CommunityShareCreateResponse createShare(
            Long userId,
            Long postId,
            CommunityShareCreateRequest request
    ) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(this::resourceNotFoundException);
        CommunityShareTheme theme = communityShareThemeRepository.findById(request.themeId())
                .orElseThrow(this::resourceNotFoundException);

        CommunityShare share = communityShareRepository.saveAndFlush(
                new CommunityShare(post, theme, userId));
        return new CommunityShareCreateResponse(share.getShareId(), share.getStatus());
    }

    @Transactional(readOnly = true)
    public CommunityShareStatusResponse getShareStatus(Long userId, Long shareId) {
        CommunityShare share = communityShareRepository.findByShareIdAndUserId(shareId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, SHARE_NOT_FOUND_MESSAGE));
        String imageUrl = share.getStatus() == CommunityShareStatus.COMPLETED
                ? share.getImageUrl()
                : null;
        return new CommunityShareStatusResponse(share.getStatus(), imageUrl);
    }

    private BusinessException resourceNotFoundException() {
        return new BusinessException(ErrorCode.NOT_FOUND, RESOURCE_NOT_FOUND_MESSAGE);
    }
}
