package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.dto.BookCurationResponse;
import com.likelion.olion.domain.book.entity.Book;
import com.likelion.olion.domain.book.entity.BookCuration;
import com.likelion.olion.domain.book.repository.BookCurationRepository;
import com.likelion.olion.domain.book.repository.BookRepository;
import com.likelion.olion.domain.emotion.entity.DiagnosisSwipe;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.repository.DiagnosisSwipeRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookCurationServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private EmotionDiagnosisRepository diagnosisRepository;
    @Mock
    private DiagnosisSwipeRepository swipeRepository;
    @Mock
    private BookCurationRepository curationRepository;
    @Mock
    private BookCurationGenerator curationGenerator;

    private BookCurationService service;
    private Book book;
    private EmotionDiagnosis diagnosis;

    @BeforeEach
    void setUp() {
        service = new BookCurationService(
                bookRepository,
                diagnosisRepository,
                swipeRepository,
                curationRepository,
                curationGenerator);
        book = org.mockito.Mockito.mock(Book.class);
        diagnosis = new EmotionDiagnosis(7L);
        ReflectionTestUtils.setField(diagnosis, "diagnosisId", 10L);
    }

    @Test
    void returnsCachedCurationWithoutGeneratingAgain() {
        BookCuration curation = new BookCuration(diagnosis, book, "저장된 소개 문구");
        given(book.getBookId()).willReturn(5L);
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));
        given(diagnosisRepository.findById(10L)).willReturn(Optional.of(diagnosis));
        given(curationRepository.findByDiagnosisDiagnosisIdAndBookBookId(10L, 5L))
                .willReturn(Optional.of(curation));

        BookCurationResponse response = service.getCuration(7L, 5L, 10L);

        assertThat(response.bookId()).isEqualTo(5L);
        assertThat(response.curationText()).isEqualTo("저장된 소개 문구");
        verify(curationGenerator, never()).generate(any(), any());
        verify(curationRepository, never()).save(any());
    }

    @Test
    void generatesAndCachesCurationWhenCacheIsMissing() {
        DiagnosisSwipe likedSwipe = new DiagnosisSwipe(10L, 1, true);
        given(book.getBookId()).willReturn(5L);
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));
        given(diagnosisRepository.findById(10L)).willReturn(Optional.of(diagnosis));
        given(curationRepository.findByDiagnosisDiagnosisIdAndBookBookId(10L, 5L))
                .willReturn(Optional.empty());
        given(swipeRepository.findByDiagnosisIdAndLikedTrueOrderBySwipeIdAsc(10L))
                .willReturn(List.of(likedSwipe));
        given(curationGenerator.generate(book, List.of(1))).willReturn("맞춤 소개 문구");
        given(curationRepository.save(any(BookCuration.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BookCurationResponse response = service.getCuration(7L, 5L, 10L);

        assertThat(response.curationText()).isEqualTo("맞춤 소개 문구");
        verify(curationRepository).save(any(BookCuration.class));
    }

    @Test
    void rejectsMissingBook() {
        given(bookRepository.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCuration(7L, 5L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage()).isEqualTo("도서를 찾을 수 없습니다.");
                });
        verify(diagnosisRepository, never()).findById(any());
    }

    @Test
    void rejectsMissingDiagnosis() {
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));
        given(diagnosisRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCuration(7L, 5L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage()).isEqualTo("진단 결과를 찾을 수 없습니다.");
                });
    }

    @Test
    void rejectsOtherUsersDiagnosis() {
        EmotionDiagnosis otherUsersDiagnosis = new EmotionDiagnosis(8L);
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));
        given(diagnosisRepository.findById(10L)).willReturn(Optional.of(otherUsersDiagnosis));

        assertThatThrownBy(() -> service.getCuration(7L, 5L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(businessException.getMessage())
                            .isEqualTo("본인의 진단 결과만 사용할 수 있습니다.");
                });
        verify(curationRepository, never())
                .findByDiagnosisDiagnosisIdAndBookBookId(any(), any());
    }
}
