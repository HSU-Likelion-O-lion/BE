package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.dto.EssayDetailResponse;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EssayPdfGeneratorTest {
    private final EssayPdfGenerator generator = new EssayPdfGenerator();

    @Test
    void generatesNonEmptyPdfBytes() {
        EssayDetailResponse detail = new EssayDetailResponse(
                7L, "흔들려도 걷는 마음", EssayStatus.COMPLETED, null, null,
                List.of(new EssayDetailResponse.Chapter(1, "1장", List.of("사유 1", "사유 2"))));

        byte[] pdf = generator.generate(detail);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
