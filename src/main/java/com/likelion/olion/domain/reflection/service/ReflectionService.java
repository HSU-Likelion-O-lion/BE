package com.likelion.olion.domain.reflection.service;

import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateRequest;
import com.likelion.olion.domain.reflection.dto.ReflectionCreateResponse;
import com.likelion.olion.domain.reflection.dto.ReflectionListResponse;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReflectionService {
    private static final int MAX_COVER_PROGRESS = 30;

    private final ReflectionRepository reflectionRepository;
    private final ReadingSessionRepository readingSessionRepository;

    public ReflectionService(
            ReflectionRepository reflectionRepository,
            ReadingSessionRepository readingSessionRepository
    ) {
        this.reflectionRepository = reflectionRepository;
        this.readingSessionRepository = readingSessionRepository;
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

    private int coverProgress(Long userId) {
        return (int) Math.min(reflectionRepository.countByUserId(userId), MAX_COVER_PROGRESS);
    }
}
