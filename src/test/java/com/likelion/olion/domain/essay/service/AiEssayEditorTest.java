package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.global.ai.AiTextGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AiEssayEditorTest {
    @Mock
    private AiTextGenerator aiTextGenerator;

    @Test
    void 유효한_JSON으로_제목과_본문을_생성한다() {
        Reflection first = reflection(1L, "첫 번째 사유");
        Reflection second = reflection(2L, "두 번째 사유");
        given(aiTextGenerator.generate(eq(7L), eq("essay-generation"), anyString(), eq("")))
                .willReturn("""
                        {"title":"흔들려도 걷는 마음","chapters":[
                          {"title":"시작","content":"두 사유를 잇는 따뜻한 본문입니다.","reflectionIds":[1,2]}
                        ]}
                        """);

        EssayEditor.EssayDraft result = new AiEssayEditor(aiTextGenerator)
                .generate(7L, List.of(first, second));

        assertThat(result.title()).isEqualTo("흔들려도 걷는 마음");
        assertThat(result.chapters()).hasSize(1);
        assertThat(result.chapters().getFirst().content()).isEqualTo("두 사유를 잇는 따뜻한 본문입니다.");
        assertThat(result.chapters().getFirst().reflections()).containsExactly(first, second);
    }

    @Test
    void 잘못된_JSON은_한번_재시도하고_실패한다() {
        Reflection first = reflection(1L, "첫 번째 사유");
        given(aiTextGenerator.generate(eq(7L), eq("essay-generation"), anyString(), eq("")))
                .willReturn("잘못된 응답");

        assertThatThrownBy(() -> new AiEssayEditor(aiTextGenerator).generate(7L, List.of(first)))
                .isInstanceOf(AiEssayEditor.EssayGenerationException.class);
        org.mockito.Mockito.verify(aiTextGenerator, org.mockito.Mockito.times(2))
                .generate(eq(7L), eq("essay-generation"), anyString(), eq(""));
    }

    @Test
    void 누락된_사유_ID가_있으면_응답을_거부한다() {
        Reflection first = reflection(1L, "첫 번째 사유");
        Reflection second = reflection(2L, "두 번째 사유");
        given(aiTextGenerator.generate(eq(7L), eq("essay-generation"), anyString(), eq("")))
                .willReturn("{\"title\":\"제목\",\"chapters\":[{\"title\":\"장\",\"content\":\"본문입니다\",\"reflectionIds\":[1]}]}");

        assertThatThrownBy(() -> new AiEssayEditor(aiTextGenerator).generate(7L, List.of(first, second)))
                .isInstanceOf(AiEssayEditor.EssayGenerationException.class);
    }

    private Reflection reflection(Long id, String content) {
        Reflection reflection = org.mockito.Mockito.mock(Reflection.class);
        given(reflection.getReflectionId()).willReturn(id);
        org.mockito.Mockito.lenient().when(reflection.getCreatedAt())
                .thenReturn(Instant.parse("2026-08-14T00:00:00Z"));
        given(reflection.getContent()).willReturn(content);
        return reflection;
    }
}
