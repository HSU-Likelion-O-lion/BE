package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EssayGenerationResponseParserTest {
    private final EssayGenerationResponseParser parser = new EssayGenerationResponseParser();

    @Test
    void 모든_사유를_한번씩_사용한_응답을_파싱한다() {
        List<Reflection> reflections = List.of(reflection(1L), reflection(2L));
        String response = """
                {"title":"제목","chapters":[
                  {"title":"첫 장","content":"따뜻한 본문입니다.","reflectionIds":[1]},
                  {"title":"둘째 장","content":"이어지는 본문입니다.","reflectionIds":[2]}
                ]}
                """;

        var result = parser.parse(response, reflections);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().chapters()).hasSize(2);
    }

    @Test
    void 중복되거나_누락된_사유_ID는_거부한다() {
        List<Reflection> reflections = List.of(reflection(1L), reflection(2L));
        String response = "{\"title\":\"제목\",\"chapters\":[{\"title\":\"장\",\"content\":\"본문입니다\",\"reflectionIds\":[1,1]}]}";

        assertThat(parser.parse(response, reflections)).isEmpty();
    }

    @Test
    void 챕터_본문이_500자를_초과하면_거부한다() {
        Reflection reflection = reflection(1L);
        String content = "가".repeat(501);
        String response = "{\"title\":\"제목\",\"chapters\":[{\"title\":\"장\",\"content\":\""
                + content + "\",\"reflectionIds\":[1]}]}";

        assertThat(parser.parse(response, List.of(reflection))).isEmpty();
    }

    @Test
    void 전체_본문이_2500자를_초과하면_거부한다() {
        String content = "가".repeat(500);
        String response = "{\"title\":\"제목\",\"chapters\":["
                + chapter("1장", content, 1) + ","
                + chapter("2장", content, 2) + ","
                + chapter("3장", content, 3) + ","
                + chapter("4장", content, 4) + ","
                + chapter("5장", content, 5) + ","
                + chapter("6장", content, 6) + "]}";

        assertThat(parser.parse(response, reflections(1, 2, 3, 4, 5, 6))).isEmpty();
    }

    private String chapter(String title, String content, long reflectionId) {
        return "{\"title\":\"" + title + "\",\"content\":\"" + content
                + "\",\"reflectionIds\":[" + reflectionId + "]}";
    }

    private List<Reflection> reflections(long... ids) {
        return java.util.Arrays.stream(ids).mapToObj(this::reflection).toList();
    }

    private Reflection reflection(Long id) {
        Reflection reflection = org.mockito.Mockito.mock(Reflection.class);
        org.mockito.Mockito.when(reflection.getReflectionId()).thenReturn(id);
        org.mockito.Mockito.when(reflection.getCreatedAt())
                .thenReturn(Instant.parse("2026-08-18T00:00:00Z"));
        return reflection;
    }
}
