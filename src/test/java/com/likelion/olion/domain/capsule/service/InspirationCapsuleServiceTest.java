package com.likelion.olion.domain.capsule.service;

import com.likelion.olion.domain.capsule.dto.InspirationCapsuleTodayResponse;
import com.likelion.olion.domain.capsule.entity.InspirationCapsule;
import com.likelion.olion.domain.capsule.repository.InspirationCapsuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InspirationCapsuleServiceTest {
    @Mock
    private InspirationCapsuleRepository inspirationCapsuleRepository;

    @Test
    void returnsNotOpenedWhenNoCapsuleForToday() {
        InspirationCapsuleService service = new InspirationCapsuleService(inspirationCapsuleRepository);
        given(inspirationCapsuleRepository.findByUserIdAndOpenedDate(eq(1L), any(LocalDate.class)))
                .willReturn(Optional.empty());

        InspirationCapsuleTodayResponse response = service.getToday(1L);

        assertThat(response.opened()).isFalse();
        assertThat(response.quoteText()).isNull();
        assertThat(response.bookTitle()).isNull();
    }

    @Test
    void returnsOpenedCapsuleWhenAlreadyOpenedToday() {
        InspirationCapsuleService service = new InspirationCapsuleService(inspirationCapsuleRepository);
        InspirationCapsule capsule = new InspirationCapsule(1L, "삶이 그대를 속일지라도...", "안나 카레니나", LocalDate.now());
        given(inspirationCapsuleRepository.findByUserIdAndOpenedDate(eq(1L), any(LocalDate.class)))
                .willReturn(Optional.of(capsule));

        InspirationCapsuleTodayResponse response = service.getToday(1L);

        assertThat(response.opened()).isTrue();
        assertThat(response.quoteText()).isEqualTo("삶이 그대를 속일지라도...");
        assertThat(response.bookTitle()).isEqualTo("안나 카레니나");
    }
}
