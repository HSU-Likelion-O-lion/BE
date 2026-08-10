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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookCurationService {
    private final BookRepository bookRepository;
    private final EmotionDiagnosisRepository diagnosisRepository;
    private final DiagnosisSwipeRepository swipeRepository;
    private final BookCurationRepository curationRepository;
    private final BookCurationGenerator curationGenerator;

    public BookCurationService(
            BookRepository bookRepository,
            EmotionDiagnosisRepository diagnosisRepository,
            DiagnosisSwipeRepository swipeRepository,
            BookCurationRepository curationRepository,
            BookCurationGenerator curationGenerator
    ) {
        this.bookRepository = bookRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.swipeRepository = swipeRepository;
        this.curationRepository = curationRepository;
        this.curationGenerator = curationGenerator;
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
        String curationText = curationGenerator.generate(
                book,
                swipeRepository
                        .findByDiagnosisIdAndLikedTrueOrderBySwipeIdAsc(diagnosis.getDiagnosisId())
                        .stream()
                        .map(swipe -> swipe.getCardId())
                        .toList());
        BookCuration curation = curationRepository.save(
                new BookCuration(diagnosis, book, curationText));
        return toResponse(curation);
    }

    private BookCurationResponse toResponse(BookCuration curation) {
        return new BookCurationResponse(
                curation.getBook().getBookId(), curation.getCurationText());
    }
}
