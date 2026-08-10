package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.community.dto.CommunityPostCreateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostCreateResponse;
import com.likelion.olion.domain.community.dto.CommunityPostPreviewResponse;
import com.likelion.olion.domain.community.dto.CommunityPostListResponse;
import com.likelion.olion.domain.community.dto.CommunityPostUpdateRequest;
import com.likelion.olion.domain.community.dto.CommunityPostUpdateResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.repository.CommunityPostHeartRepository;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CommunityPostService {
    private final CommunityAccessChecker communityAccessChecker;
    private final UserBookRepository userBookRepository;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostHeartRepository communityPostHeartRepository;
    private final CommunityPostPolicy communityPostPolicy;

    public CommunityPostService(
            CommunityAccessChecker communityAccessChecker,
            UserBookRepository userBookRepository,
            CommunityPostRepository communityPostRepository,
            CommunityPostHeartRepository communityPostHeartRepository,
            CommunityPostPolicy communityPostPolicy
    ) {
        this.communityAccessChecker = communityAccessChecker;
        this.userBookRepository = userBookRepository;
        this.communityPostRepository = communityPostRepository;
        this.communityPostHeartRepository = communityPostHeartRepository;
        this.communityPostPolicy = communityPostPolicy;
    }

    @Transactional
    public CommunityPostCreateResponse createPost(Long userId, CommunityPostCreateRequest request) {
        validateContent(request.content());
        checkAccess(userId, request.roomId());
        validatePostingRate(userId);

        String anonymousNickname = communityPostPolicy
                .createAnonymousNickname(userId, request.roomId());
        CommunityPost savedPost = communityPostRepository.save(new CommunityPost(
                request.roomId(), userId, anonymousNickname, request.content().trim(), request.reflectionId()));

        return new CommunityPostCreateResponse(savedPost.getPostId(), savedPost.getAnonymousNickname());
    }

    @Transactional
    public CommunityPostUpdateResponse updatePost(
            Long userId,
            Long postId,
            CommunityPostUpdateRequest request
    ) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        validateContent(request.content());
        post.updateContent(request.content().trim());
        return new CommunityPostUpdateResponse(post.getPostId());
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 작성한 게시글만 삭제할 수 있습니다.");
        }
        communityPostRepository.delete(post);
    }

    public CommunityPostPreviewResponse getPreviews(Long userId, Long roomId) {
        checkAccess(userId, roomId);

        return new CommunityPostPreviewResponse(communityPostRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(post -> new CommunityPostPreviewResponse.Preview(
                        post.getPostId(), firstLine(post.getContent())))
                .toList());
    }

    public CommunityPostListResponse getPosts(Long userId, Long roomId) {
        checkAccess(userId, roomId);
        return new CommunityPostListResponse(communityPostRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(post -> {
                    boolean isMine = post.getUserId().equals(userId);
                    boolean isHearted = communityPostHeartRepository
                            .existsByPostPostIdAndUserId(post.getPostId(), userId);
                    Integer heartCount = isMine
                            ? Math.toIntExact(communityPostHeartRepository.countByPostPostId(post.getPostId()))
                            : null;
                    return new CommunityPostListResponse.Post(
                            post.getPostId(), post.getAnonymousNickname(), post.getContent(),
                            isMine, isHearted, heartCount);
                })
                .toList());
    }

    private void checkAccess(Long userId, Long roomId) {
        if (!communityAccessChecker.canEnter(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        userBookRepository.findByUserBookIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "게시글 내용을 입력해 주세요.");
        }
        if (communityPostPolicy.containsProhibitedWord(content)) {
            throw new BusinessException(ErrorCode.UNPROCESSABLE_ENTITY, "부적절한 내용이 포함되어 있습니다.");
        }
    }

    private void validatePostingRate(Long userId) {
        Instant threeMinutesAgo = Instant.now().minus(3, ChronoUnit.MINUTES);
        if (communityPostRepository.countByUserIdAndCreatedAtAfter(userId, threeMinutesAgo) >= 5) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "잠시 후 다시 작성해 주세요.");
        }
    }

    private String firstLine(String content) {
        return content == null ? "" : content.split("\\R", 2)[0].trim();
    }
}
