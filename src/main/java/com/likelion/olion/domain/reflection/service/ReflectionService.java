package com.likelion.olion.domain.reflection.service;

import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionDeleteResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionListResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionPublishableResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionUpdateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionUpdateResponse;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReflectionService {
    private static final int MAX_COVER_PROGRESS = 30;
    private static final int PUBLISH_THRESHOLD = 30;

    private final ReflectionRepository reflectionRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final CommunityPostRepository communityPostRepository;

    @Autowired
    public ReflectionService(
            ReflectionRepository reflectionRepository,
            ReadingSessionRepository readingSessionRepository,
            CommunityPostRepository communityPostRepository
    ) {
        this.reflectionRepository = reflectionRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.communityPostRepository = communityPostRepository;
    }

    public ReflectionService(
            ReflectionRepository reflectionRepository,
            ReadingSessionRepository readingSessionRepository
    ) {
        this(reflectionRepository, readingSessionRepository, null);
    }

    @Transactional
    public ReflectionCreateResponse create(Long userId, ReflectionCreateRequest request) {
        ReadingSession session = readingSessionRepository
                .findBySessionIdAndUserId(request.sessionId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "세션을 찾을 수 없습니다."));

        Reflection saved = reflectionRepository.save(new Reflection(userId, session, request.content()));
        int coverProgress = coverProgress(userId);
        return new ReflectionCreateResponse(saved.getReflectionId(), coverProgress);
    }

    @Transactional(readOnly = true)
    public ReflectionListResponse getList(Long userId) {
        List<Reflection> reflections = reflectionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ReflectionListResponse.of(coverProgress(userId), reflections);
    }

    @Transactional
    public ReflectionUpdateResponse update(Long userId, Long reflectionId, ReflectionUpdateRequest request) {
        Reflection reflection = reflectionRepository.findByReflectionIdAndUserId(reflectionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사유를 찾을 수 없습니다."));
        String content = request.content().trim();
        reflection.edit(content);
        syncCommunityPosts(reflection, content);
        return new ReflectionUpdateResponse(reflection.getReflectionId());
    }

    @Transactional
    public ReflectionDeleteResponse delete(Long userId, Long reflectionId) {
        Reflection reflection = reflectionRepository.findByReflectionIdAndUserId(reflectionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사유를 찾을 수 없습니다."));
        if (communityPostRepository != null) {
            communityPostRepository.deleteAll(communityPostRepository.findByReflectionId(reflectionId));
        }
        reflectionRepository.delete(reflection);
        return new ReflectionDeleteResponse(coverProgress(userId));
    }

    @Transactional(readOnly = true)
    public ReflectionPublishableResponse getPublishable(Long userId) {
        long count = reflectionRepository.countByUserId(userId);
        if (count < PUBLISH_THRESHOLD) {
            return ReflectionPublishableResponse.notEnough((int) (PUBLISH_THRESHOLD - count));
        }
        return ReflectionPublishableResponse.ready(reflectionRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    private int coverProgress(Long userId) {
        return (int) Math.min(reflectionRepository.countByUserId(userId), MAX_COVER_PROGRESS);
    }

    private void syncCommunityPosts(Reflection reflection, String content) {
        if (communityPostRepository == null) {
            return;
        }
        communityPostRepository.findByReflectionId(reflection.getReflectionId())
                .forEach(post -> post.updateContent(content));
    }
}
