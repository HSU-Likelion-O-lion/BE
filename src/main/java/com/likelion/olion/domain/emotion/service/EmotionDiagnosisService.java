package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisRequest;
import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisResponse;
import com.likelion.olion.domain.emotion.entity.DiagnosisSwipe;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosisRecommendation;
import com.likelion.olion.domain.emotion.repository.DiagnosisSwipeRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRecommendationRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class EmotionDiagnosisService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final EmotionDiagnosisRepository diagnosisRepository;
    private final DiagnosisSwipeRepository swipeRepository;
    private final BookRepository bookRepository;
    private final EmotionDiagnosisRecommendationRepository recommendationRepository;
    private final EmotionBookRecommendationService recommendationService;
    private final UserRepository userRepository;

    @Autowired
    public EmotionDiagnosisService(
            EmotionDiagnosisRepository diagnosisRepository,
            DiagnosisSwipeRepository swipeRepository,
            BookRepository bookRepository,
            EmotionDiagnosisRecommendationRepository recommendationRepository,
            EmotionBookRecommendationService recommendationService,
            UserRepository userRepository
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.swipeRepository = swipeRepository;
        this.bookRepository = bookRepository;
        this.recommendationRepository = recommendationRepository;
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    public EmotionDiagnosisService(
            EmotionDiagnosisRepository diagnosisRepository,
            DiagnosisSwipeRepository swipeRepository,
            BookRepository bookRepository,
            EmotionDiagnosisRecommendationRepository recommendationRepository,
            UserRepository userRepository
    ) {
        this(diagnosisRepository, swipeRepository, bookRepository,
                recommendationRepository, new EmotionBookRecommendationService(), userRepository);
    }

    @Transactional
    public Submission submit(Long userId, EmotionDiagnosisRequest request) {
        List<EmotionDiagnosisRequest.Swipe> swipes = request.swipes();
        if (swipes.size() != 5 || swipes.stream().anyMatch(s -> s.cardId() == null || s.liked() == null)
                || swipes.stream().map(EmotionDiagnosisRequest.Swipe::cardId).distinct().count() != 5
                || swipes.stream().anyMatch(s -> !isKnownCard(s.cardId()))) {
            return Submission.invalid();
        }

        if (isOverDailyLimit(userId)) {
            return Submission.limitExceeded();
        }

        EmotionDiagnosis diagnosis = diagnosisRepository.save(new EmotionDiagnosis(userId));
        swipeRepository.saveAll(swipes.stream()
                .map(s -> new DiagnosisSwipe(diagnosis.getDiagnosisId(), s.cardId(), s.liked()))
                .toList());

        boolean hasLikedCard = swipes.stream().anyMatch(EmotionDiagnosisRequest.Swipe::liked);
        if (!hasLikedCard) {
            return Submission.empty(new EmotionDiagnosisResponse(diagnosis.getDiagnosisId(), List.of()));
        }

        List<Integer> likedCardIds = swipes.stream()
                .filter(EmotionDiagnosisRequest.Swipe::liked)
                .map(EmotionDiagnosisRequest.Swipe::cardId)
                .toList();
        List<EmotionDiagnosisResponse.RecommendedBook> books = recommendationService
                .recommend(likedCardIds, bookRepository.findAll()).stream()
                .map(this::toRecommendedBook)
                .toList();
        if (books.isEmpty()) {
            EmotionDiagnosisResponse response = new EmotionDiagnosisResponse(
                    diagnosis.getDiagnosisId(),
                    List.of(new EmotionDiagnosisResponse.RecommendedBook(
                            1L, "기본 추천 도서", null, "지금 이 순간을 위한 책"
                    ))
            );
            saveRecommendations(diagnosis, response.recommendedBooks());
            return Submission.fallback(response);
        }
        EmotionDiagnosisResponse response = new EmotionDiagnosisResponse(
                diagnosis.getDiagnosisId(), books);
        saveRecommendations(diagnosis, books);
        return Submission.success(response);
    }

    private void saveRecommendations(
            EmotionDiagnosis diagnosis,
            List<EmotionDiagnosisResponse.RecommendedBook> books
    ) {
        recommendationRepository.saveAll(IntStream.range(0, books.size())
                .mapToObj(index -> {
                    EmotionDiagnosisResponse.RecommendedBook book = books.get(index);
                    return new EmotionDiagnosisRecommendation(
                            diagnosis,
                            book.bookId(),
                            book.title(),
                            book.coverImageUrl(),
                            book.shortDesc(),
                            index);
                })
                .toList());
    }

    private EmotionDiagnosisResponse.RecommendedBook toRecommendedBook(Book book) {
        return new EmotionDiagnosisResponse.RecommendedBook(
                book.getBookId(), book.getTitle(), book.getCoverImageUrl(), book.getDescription()
        );
    }

    private boolean isKnownCard(Integer cardId) {
        return cardId >= 1 && cardId <= 12;
    }

    private boolean isOverDailyLimit(Long userId) {
        SubscriptionPlan plan = userRepository.findById(userId)
                .map(User::getPlan)
                .orElse(SubscriptionPlan.BASIC);
        int limit = plan.dailyDiagnosisLimit();
        if (limit == Integer.MAX_VALUE) {
            return false;
        }

        ZonedDateTime startOfToday = ZonedDateTime.now(KST).toLocalDate().atStartOfDay(KST);
        Instant start = startOfToday.toInstant();
        Instant end = startOfToday.plusDays(1).toInstant();
        return diagnosisRepository.countByUserIdAndCreatedAtBetween(userId, start, end) >= limit;
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
        static Submission limitExceeded() {
            return new Submission(HttpStatus.TOO_MANY_REQUESTS, "PLAN_429_1",
                    "오늘의 감정 진단 횟수를 모두 사용했어요. 구독 등급을 업그레이드하면 더 많이 이용할 수 있어요.", null);
        }
    }
}
