package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisDetailResponse;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosisRecommendation;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRecommendationRepository;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmotionDiagnosisDetailServiceTest {
    @Mock
    private EmotionDiagnosisRepository diagnosisRepository;

    @Mock
    private EmotionDiagnosisRecommendationRepository recommendationRepository;

    @Test
    void returnsOwnedDiagnosisWithRecommendationSnapshots() {
        EmotionDiagnosisDetailService service = createService();
        EmotionDiagnosis diagnosis = new EmotionDiagnosis(7L);
        ReflectionTestUtils.setField(diagnosis, "diagnosisId", 10L);
        EmotionDiagnosisRecommendation recommendation = new EmotionDiagnosisRecommendation(
                diagnosis,
                5L,
                "아몬드",
                "https://cdn.olion.com/book/5.png",
                "감정을 배우지 못한 소년의 이야기",
                0);
        given(diagnosisRepository.findById(10L)).willReturn(Optional.of(diagnosis));
        given(recommendationRepository
                .findByDiagnosisDiagnosisIdOrderByRecommendationOrderAsc(10L))
                .willReturn(List.of(recommendation));

        EmotionDiagnosisDetailResponse response = service.getDiagnosis(7L, 10L);

        assertThat(response.diagnosisId()).isEqualTo(10L);
        assertThat(response.createdAt()).isEqualTo(diagnosis.getCreatedAt());
        assertThat(response.recommendedBooks()).singleElement().satisfies(book -> {
            assertThat(book.bookId()).isEqualTo(5L);
            assertThat(book.title()).isEqualTo("아몬드");
            assertThat(book.coverImageUrl()).isEqualTo("https://cdn.olion.com/book/5.png");
            assertThat(book.shortDesc()).isEqualTo("감정을 배우지 못한 소년의 이야기");
        });
    }

    @Test
    void rejectsOtherUsersDiagnosis() {
        EmotionDiagnosisDetailService service = createService();
        EmotionDiagnosis diagnosis = new EmotionDiagnosis(8L);
        given(diagnosisRepository.findById(10L)).willReturn(Optional.of(diagnosis));

        assertThatThrownBy(() -> service.getDiagnosis(7L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(businessException.getMessage())
                            .isEqualTo("본인의 진단 결과만 조회할 수 있습니다.");
                });
        verify(recommendationRepository, never())
                .findByDiagnosisDiagnosisIdOrderByRecommendationOrderAsc(10L);
    }

    @Test
    void rejectsMissingDiagnosis() {
        EmotionDiagnosisDetailService service = createService();
        given(diagnosisRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDiagnosis(7L, 10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(businessException.getMessage())
                            .isEqualTo("진단 결과를 찾을 수 없습니다.");
                });
    }

    private EmotionDiagnosisDetailService createService() {
        return new EmotionDiagnosisDetailService(diagnosisRepository, recommendationRepository);
    }
}
