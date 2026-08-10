package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.emotion.dto.EmotionCardResponse;
import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisRequest;
import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisResponse;
import com.likelion.olion.domain.emotion.entity.DiagnosisSwipe;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.repository.DiagnosisSwipeRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class EmotionDiagnosisService {
    private final EmotionDiagnosisRepository diagnosisRepository;
    private final DiagnosisSwipeRepository swipeRepository;
    private final BookRepository bookRepository;

    public EmotionDiagnosisService(
            EmotionDiagnosisRepository diagnosisRepository,
            DiagnosisSwipeRepository swipeRepository,
            BookRepository bookRepository
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.swipeRepository = swipeRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Submission submit(Long userId, EmotionDiagnosisRequest request) {
        List<EmotionDiagnosisRequest.Swipe> swipes = request.swipes();
        if (swipes.size() != 5 || swipes.stream().anyMatch(s -> s.cardId() == null || s.liked() == null)
                || swipes.stream().map(EmotionDiagnosisRequest.Swipe::cardId).distinct().count() != 5
                || swipes.stream().anyMatch(s -> !isKnownCard(s.cardId()))) {
            return Submission.invalid();
        }

        EmotionDiagnosis diagnosis = diagnosisRepository.save(new EmotionDiagnosis(userId));
        swipeRepository.saveAll(swipes.stream()
                .map(s -> new DiagnosisSwipe(diagnosis.getDiagnosisId(), s.cardId(), s.liked()))
                .toList());

        boolean hasLikedCard = swipes.stream().anyMatch(EmotionDiagnosisRequest.Swipe::liked);
        if (!hasLikedCard) {
            return Submission.empty(new EmotionDiagnosisResponse(diagnosis.getDiagnosisId(), List.of()));
        }

        List<EmotionDiagnosisResponse.RecommendedBook> books = bookRepository.findAll().stream()
                .limit(3)
                .map(this::toRecommendedBook)
                .toList();
        if (books.isEmpty()) {
            return Submission.fallback(new EmotionDiagnosisResponse(
                    diagnosis.getDiagnosisId(),
                    List.of(new EmotionDiagnosisResponse.RecommendedBook(
                            1L, "기본 추천 도서", "https://cdn.olion.com/book/1.png", "지금 이 순간을 위한 책"
                    ))
            ));
        }
        return Submission.success(new EmotionDiagnosisResponse(diagnosis.getDiagnosisId(), books));
    }

    private EmotionDiagnosisResponse.RecommendedBook toRecommendedBook(Book book) {
        return new EmotionDiagnosisResponse.RecommendedBook(
                book.getBookId(), book.getTitle(), book.getCoverImageUrl(), book.getDescription()
        );
    }

    private boolean isKnownCard(Integer cardId) {
        return cardId >= 1 && cardId <= 12;
    }

    public record Submission(HttpStatus status, String code, String message, EmotionDiagnosisResponse data) {
        static Submission success(EmotionDiagnosisResponse data) {
            return new Submission(HttpStatus.CREATED, "SUCCESS", "감정 진단이 완료되었습니다.", data);
        }
        static Submission empty(EmotionDiagnosisResponse data) {
            return new Submission(HttpStatus.OK, "SUCCESS_EMPTY", "마음에 와닿는 문장이 없으셨군요!", data);
        }
        static Submission fallback(EmotionDiagnosisResponse data) {
            return new Submission(HttpStatus.BAD_GATEWAY, "BOOK_502_1", "추천 도서를 불러오지 못해 기본 추천 도서로 대체합니다.", data);
        }
        static Submission invalid() {
            return new Submission(HttpStatus.BAD_REQUEST, "BOOK_400_1", "스와이프 결과가 5개가 아닙니다.", null);
        }
    }
}
