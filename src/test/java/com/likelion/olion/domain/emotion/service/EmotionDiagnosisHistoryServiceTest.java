package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.emotion.dto.EmotionDiagnosisHistoryResponse;
import com.likelion.olion.domain.emotion.entity.EmotionDiagnosis;
import com.likelion.olion.domain.emotion.repository.EmotionDiagnosisRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmotionDiagnosisHistoryServiceTest {
    private final EmotionDiagnosisRepository repository = mock(EmotionDiagnosisRepository.class);
    private final EmotionDiagnosisHistoryService service = new EmotionDiagnosisHistoryService(repository);

    @Test
    void 사용자별_진단_이력을_최신순으로_반환한다() {
        when(repository.findByUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(
                new EmotionDiagnosis(7L), new EmotionDiagnosis(7L)
        ));

        EmotionDiagnosisHistoryResponse response = service.getHistory(7L);

        assertThat(response.diagnoses()).hasSize(2);
        verify(repository).findByUserIdOrderByCreatedAtDesc(7L);
    }

    @Test
    void 진단_이력이_없으면_빈_배열을_반환한다() {
        when(repository.findByUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of());

        EmotionDiagnosisHistoryResponse response = service.getHistory(7L);

        assertThat(response.diagnoses()).isEmpty();
    }
}
