package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.community.dto.CommunityPostPreviewResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CommunityPostService {
    private final CommunityAccessChecker communityAccessChecker;
    private final UserBookRepository userBookRepository;
    private final CommunityPostRepository communityPostRepository;

    public CommunityPostService(
            CommunityAccessChecker communityAccessChecker,
            UserBookRepository userBookRepository,
            CommunityPostRepository communityPostRepository
    ) {
        this.communityAccessChecker = communityAccessChecker;
        this.userBookRepository = userBookRepository;
        this.communityPostRepository = communityPostRepository;
    }

    public CommunityPostPreviewResponse getPreviews(Long userId, Long roomId) {
        if (!communityAccessChecker.canEnter(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        userBookRepository.findByUserBookIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return new CommunityPostPreviewResponse(communityPostRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(post -> new CommunityPostPreviewResponse.Preview(
                        post.getPostId(), firstLine(post.getContent())))
                .toList());
    }

    private String firstLine(String content) {
        return content == null ? "" : content.split("\\R", 2)[0].trim();
    }
}
