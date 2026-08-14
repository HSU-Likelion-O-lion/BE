package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.global.ai.AiTextGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AiEssayEditorTest {
    @Mock
    private AiTextGenerator aiTextGenerator;

    @Test
    void 유효한_JSON은_장과_사유를_반환한다() {
        Reflection first = reflection(1L, "첫 번째 사유");
        Reflection second = reflection(2L, "두 번째 사유");
        given(aiTextGenerator.generate(eq(7L), eq("essay-editing"), anyString(), eq("")))
                .willReturn("{\"chapters\":[{\"title\":\"시작\",\"reflectionIds\":[1,2]}]}");

        List<EssayEditor.ChapterDraft> result = new AiEssayEditor(aiTextGenerator)
                .organize(7L, List.of(first, second), reflections -> List.of());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("시작");
        assertThat(result.getFirst().reflections()).containsExactly(first, second);
    }

    @Test
    void 잘못된_JSON이나_중복_사유가_있으면_템플릿_편집기로_대체한다() {
        Reflection first = reflection(1L, "첫 번째 사유");
        Reflection second = reflection(2L, "두 번째 사유");
        given(aiTextGenerator.generate(eq(7L), eq("essay-editing"), anyString(), eq("")))
                .willReturn("{\"chapters\":[{\"title\":\"중복\",\"reflectionIds\":[1,1]}]}");
        EssayEditor.ChapterDraft fallback = new EssayEditor.ChapterDraft("기본 장", List.of(first, second));

        List<EssayEditor.ChapterDraft> result = new AiEssayEditor(aiTextGenerator)
                .organize(7L, List.of(first, second), reflections -> List.of(fallback));

        assertThat(result).containsExactly(fallback);
    }

    private Reflection reflection(Long id, String content) {
        Reflection reflection = org.mockito.Mockito.mock(Reflection.class);
        given(reflection.getReflectionId()).willReturn(id);
        given(reflection.getCreatedAt()).willReturn(Instant.parse("2026-08-14T00:00:00Z"));
        given(reflection.getContent()).willReturn(content);
        return reflection;
    }
}
