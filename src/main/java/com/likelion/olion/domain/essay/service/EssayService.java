package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.dto.EssayCreateRequest;
import com.likelion.olion.domain.essay.dto.EssayCreateResponse;
import com.likelion.olion.domain.essay.dto.EssayDetailResponse;
import com.likelion.olion.domain.essay.dto.EssayDraftResponse;
import com.likelion.olion.domain.essay.dto.EssayJobStatusResponse;
import com.likelion.olion.domain.essay.dto.EssayListResponse;
import com.likelion.olion.domain.essay.dto.EssayPublishRequest;
import com.likelion.olion.domain.essay.dto.EssayPublishResponse;
import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayChapter;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import com.likelion.olion.domain.essay.event.EssayGenerationRequestedEvent;
import com.likelion.olion.domain.essay.repository.EssayChapterRepository;
import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EssayService {
    private final EssayRepository essayRepository;
    private final EssayChapterRepository essayChapterRepository;
    private final ReflectionRepository reflectionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EssayPdfGenerator essayPdfGenerator;

    public EssayService(
            EssayRepository essayRepository,
            EssayChapterRepository essayChapterRepository,
            ReflectionRepository reflectionRepository,
            ApplicationEventPublisher eventPublisher,
            EssayPdfGenerator essayPdfGenerator
    ) {
        this.essayRepository = essayRepository;
        this.essayChapterRepository = essayChapterRepository;
        this.reflectionRepository = reflectionRepository;
        this.eventPublisher = eventPublisher;
        this.essayPdfGenerator = essayPdfGenerator;
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

    @Transactional(readOnly = true)
    public EssayListResponse getList(Long userId) {
        return EssayListResponse.of(essayRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Transactional(readOnly = true)
    public EssayJobStatusResponse getJobStatus(Long userId, Long essayId) {
        Essay essay = essayRepository.findByEssayIdAndUserId(essayId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "에세이를 찾을 수 없습니다."));
        return new EssayJobStatusResponse(essay.getStatus());
    }

    @Transactional
    public EssayJobStatusResponse retry(Long userId, Long essayId) {
        Essay essay = essayRepository.findByEssayIdAndUserId(essayId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "에세이를 찾을 수 없습니다."));
        if (essay.getStatus() != EssayStatus.FAILED) {
            throw new BusinessException(ErrorCode.CONFLICT, "실패 상태의 작업만 재시도할 수 있습니다.");
        }

        essay.retry();
        eventPublisher.publishEvent(new EssayGenerationRequestedEvent(essay.getEssayId()));
        return new EssayJobStatusResponse(essay.getStatus());
    }

    @Transactional(readOnly = true)
    public EssayDraftResponse getDraft(Long userId, Long essayId) {
        Essay essay = essayRepository.findByEssayIdAndUserId(essayId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "에세이를 찾을 수 없습니다."));
        if (essay.getStatus() != EssayStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CONFLICT, "아직 편집이 완료되지 않았습니다.");
        }

        List<EssayChapter> chapters = essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(essayId);
        Map<Long, List<Long>> reflectionIdsByChapter = reflectionRepository.findByEssay_EssayId(essayId).stream()
                .filter(reflection -> reflection.getChapter() != null)
                .collect(Collectors.groupingBy(
                        reflection -> reflection.getChapter().getChapterId(),
                        Collectors.mapping(Reflection::getReflectionId, Collectors.toList())));

        List<EssayDraftResponse.Chapter> chapterResponses = chapters.stream()
                .map(chapter -> new EssayDraftResponse.Chapter(
                        chapter.getChapterNo(),
                        chapter.getTitle(),
                        reflectionIdsByChapter.getOrDefault(chapter.getChapterId(), List.of())))
                .toList();
        return new EssayDraftResponse(chapterResponses);
    }

    @Transactional(readOnly = true)
    public EssayDetailResponse getDetail(Long userId, Long essayId) {
        Essay essay = essayRepository.findByEssayIdAndUserId(essayId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "에세이를 찾을 수 없습니다."));
        if (essay.getStatus() != EssayStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CONFLICT, "아직 편집이 완료되지 않았습니다.");
        }

        List<EssayChapter> chapters = essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(essayId);
        Map<Long, List<String>> contentsByChapter = reflectionRepository.findByEssay_EssayId(essayId).stream()
                .filter(reflection -> reflection.getChapter() != null)
                .sorted(Comparator.comparing(Reflection::getCreatedAt))
                .collect(Collectors.groupingBy(
                        reflection -> reflection.getChapter().getChapterId(),
                        Collectors.mapping(Reflection::getContent, Collectors.toList())));

        List<EssayDetailResponse.Chapter> chapterResponses = chapters.stream()
                .map(chapter -> new EssayDetailResponse.Chapter(
                        chapter.getChapterNo(),
                        chapter.getTitle(),
                        contentsByChapter.getOrDefault(chapter.getChapterId(), List.of())))
                .toList();
        return EssayDetailResponse.of(essay, chapterResponses);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPdf(Long userId, Long essayId) {
        return essayPdfGenerator.generate(getDetail(userId, essayId));
    }

    @Transactional
    public EssayPublishResponse publish(Long userId, Long essayId, EssayPublishRequest request) {
        Essay essay = essayRepository.findByEssayIdAndUserId(essayId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "에세이를 찾을 수 없습니다."));
        if (essay.getStatus() != EssayStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CONFLICT, "아직 편집이 완료되지 않았습니다.");
        }

        essay.publish(request.title());
        return new EssayPublishResponse(essay.getEssayId(), essay.getTitle(), essay.getPublishedAt());
    }
}
