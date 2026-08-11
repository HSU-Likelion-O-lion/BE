package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.dto.EssayCreateRequest;
import com.likelion.olion.domain.essay.dto.EssayCreateResponse;
import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.event.EssayGenerationRequestedEvent;
import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EssayService {
    private final EssayRepository essayRepository;
    private final ReflectionRepository reflectionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EssayService(
            EssayRepository essayRepository,
            ReflectionRepository reflectionRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.essayRepository = essayRepository;
        this.reflectionRepository = reflectionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EssayCreateResponse create(Long userId, EssayCreateRequest request) {
        List<Reflection> reflections = reflectionRepository
                .findByReflectionIdInAndUserId(request.reflectionIds(), userId);
        if (reflections.size() != request.reflectionIds().size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "선택한 사유 중 존재하지 않거나 본인 소유가 아닌 항목이 있습니다.");
        }

        Essay essay = essayRepository.saveAndFlush(new Essay(userId));
        reflections.forEach(reflection -> reflection.assignToEssay(essay));

        eventPublisher.publishEvent(new EssayGenerationRequestedEvent(essay.getEssayId()));
        return new EssayCreateResponse(essay.getEssayId(), essay.getStatus());
    }
}
