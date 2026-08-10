package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisRequest;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosisRecommendation;
import com.likelion.olion.domain.emotion.repository.DiagnosisSwipeRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRecommendationRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmotionDiagnosisServiceTest {
    private final EmotionDiagnosisRepository diagnosisRepository = mock(EmotionDiagnosisRepository.class);
    private final DiagnosisSwipeRepository swipeRepository = mock(DiagnosisSwipeRepository.class);
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final EmotionDiagnosisRecommendationRepository recommendationRepository =
            mock(EmotionDiagnosisRecommendationRepository.class);
    private final EmotionDiagnosisService service = new EmotionDiagnosisService(
            diagnosisRepository, swipeRepository, bookRepository, recommendationRepository
    );

    @Test
    void 스와이프_결과가_5개가_아니면_400_결과를_반환한다() {
        EmotionDiagnosisRequest request = new EmotionDiagnosisRequest(List.of(
                new EmotionDiagnosisRequest.Swipe(1, true)
        ));

        EmotionDiagnosisService.Submission result = service.submit(7L, request);

        assertThat(result.status().value()).isEqualTo(400);
        assertThat(result.code()).isEqualTo("BOOK_400_1");
        verify(diagnosisRepository, never()).save(any());
    }

    @Test
    void 전체_비공감이면_추천목록이_빈_성공응답이다() {
        when(diagnosisRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        EmotionDiagnosisRequest request = new EmotionDiagnosisRequest(List.of(
                new EmotionDiagnosisRequest.Swipe(1, false),
                new EmotionDiagnosisRequest.Swipe(2, false),
                new EmotionDiagnosisRequest.Swipe(3, false),
                new EmotionDiagnosisRequest.Swipe(4, false),
                new EmotionDiagnosisRequest.Swipe(5, false)
        ));

        EmotionDiagnosisService.Submission result = service.submit(7L, request);

        assertThat(result.status().value()).isEqualTo(200);
        assertThat(result.code()).isEqualTo("SUCCESS_EMPTY");
        assertThat(result.data().recommendedBooks()).isEmpty();
        verify(recommendationRepository, never()).saveAll(any());
    }

    @Test
    void 추천_도서를_진단_결과_스냅샷으로_저장한다() {
        EmotionDiagnosis diagnosis = new EmotionDiagnosis(7L);
        Book book = mock(Book.class);
        when(diagnosisRepository.save(any())).thenReturn(diagnosis);
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(book.getBookId()).thenReturn(5L);
        when(book.getTitle()).thenReturn("아몬드");
        when(book.getCoverImageUrl()).thenReturn("https://cdn.olion.com/book/5.png");
        when(book.getDescription()).thenReturn("감정을 배우지 못한 소년의 이야기");
        EmotionDiagnosisRequest request = new EmotionDiagnosisRequest(List.of(
                new EmotionDiagnosisRequest.Swipe(1, true),
                new EmotionDiagnosisRequest.Swipe(2, false),
                new EmotionDiagnosisRequest.Swipe(3, false),
                new EmotionDiagnosisRequest.Swipe(4, false),
                new EmotionDiagnosisRequest.Swipe(5, false)
        ));

        EmotionDiagnosisService.Submission result = service.submit(7L, request);

        assertThat(result.status().value()).isEqualTo(201);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<EmotionDiagnosisRecommendation>> captor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(recommendationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(recommendation -> {
            assertThat(recommendation.getDiagnosis()).isSameAs(diagnosis);
            assertThat(recommendation.getBookId()).isEqualTo(5L);
            assertThat(recommendation.getTitle()).isEqualTo("아몬드");
            assertThat(recommendation.getRecommendationOrder()).isZero();
        });
    }
}
