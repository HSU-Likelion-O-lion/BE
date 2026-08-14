package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookCurationResponse;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.entity.BookCuration;
import com.likelion.olion.domain.book.repository.BookCurationRepository;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.repository.DiagnosisSwipeRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import com.likelion.olion.global.ai.AiTextGenerator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookCurationService {
    private final BookRepository bookRepository;
    private final EmotionDiagnosisRepository diagnosisRepository;
    private final DiagnosisSwipeRepository swipeRepository;
    private final BookCurationRepository curationRepository;
    private final BookCurationGenerator curationGenerator;
    private final AiTextGenerator aiTextGenerator;

    @Autowired
    public BookCurationService(
            BookRepository bookRepository,
            EmotionDiagnosisRepository diagnosisRepository,
            DiagnosisSwipeRepository swipeRepository,
            BookCurationRepository curationRepository,
            BookCurationGenerator curationGenerator
    ) {
        this(bookRepository, diagnosisRepository, swipeRepository, curationRepository,
                curationGenerator, AiTextGenerator.disabled());
    }

    public BookCurationService(
            BookRepository bookRepository,
            EmotionDiagnosisRepository diagnosisRepository,
            DiagnosisSwipeRepository swipeRepository,
            BookCurationRepository curationRepository,
            BookCurationGenerator curationGenerator,
            AiTextGenerator aiTextGenerator
    ) {
        this.bookRepository = bookRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.swipeRepository = swipeRepository;
        this.curationRepository = curationRepository;
        this.curationGenerator = curationGenerator;
        this.aiTextGenerator = aiTextGenerator;
    }

    @Transactional
    public BookCurationResponse getCuration(Long userId, Long bookId, Long diagnosisId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));
        EmotionDiagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "진단 결과를 찾을 수 없습니다."));
        if (!diagnosis.getUserId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN, "본인의 진단 결과만 사용할 수 있습니다.");
        }

        return curationRepository.findByDiagnosisDiagnosisIdAndBookBookId(diagnosisId, bookId)
                .map(this::toResponse)
                .orElseGet(() -> generateAndSave(diagnosis, book));
    }

    private BookCurationResponse generateAndSave(EmotionDiagnosis diagnosis, Book book) {
        List<Integer> likedCardIds = swipeRepository
                .findByDiagnosisIdAndLikedTrueOrderBySwipeIdAsc(diagnosis.getDiagnosisId())
                .stream()
                .map(swipe -> swipe.getCardId())
                .toList();
        String fallback = curationGenerator.generate(book, likedCardIds);
        String prompt = """
                당신은 독서 서비스 ReadMate의 따뜻한 큐레이터입니다.
                책 본문을 인용하지 말고, 아래 공개 서지 정보와 사용자의 감정 카드 번호만 바탕으로
                2~3문장의 맞춤 소개글을 한국어로 작성하세요. 과장된 치료·의학적 조언은 하지 마세요.
                책 제목: %s
                저자: %s
                책 설명: %s
                공감한 감정 카드 번호: %s
                """.formatted(book.getTitle(), book.getAuthor(), limit(book.getDescription(), 800), likedCardIds);
        String curationText = aiTextGenerator.generate(prompt, fallback);
        BookCuration curation = curationRepository.save(
                new BookCuration(diagnosis, book, curationText));
        return toResponse(curation);
    }

    private String limit(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private BookCurationResponse toResponse(BookCuration curation) {
        return new BookCurationResponse(
                curation.getBook().getBookId(), curation.getCurationText());
    }
}
