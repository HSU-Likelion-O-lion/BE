package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.global.ai.AiTextGenerator;
import com.likelion.olion.global.ai.PromptTemplateLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AiEssayEditor {
    private static final int MAX_ATTEMPTS = 2;
    private static final String PROMPT_VERSION = "v1";

    private final AiTextGenerator aiTextGenerator;
    private final PromptTemplateLoader promptTemplateLoader;
    private final EssayGenerationResponseParser responseParser;

    @Autowired
    public AiEssayEditor(
            AiTextGenerator aiTextGenerator,
            PromptTemplateLoader promptTemplateLoader,
            EssayGenerationResponseParser responseParser
    ) {
        this.aiTextGenerator = aiTextGenerator;
        this.promptTemplateLoader = promptTemplateLoader;
        this.responseParser = responseParser;
    }

    public AiEssayEditor(AiTextGenerator aiTextGenerator) {
        this(aiTextGenerator, new PromptTemplateLoader(),
                new EssayGenerationResponseParser(new com.fasterxml.jackson.databind.ObjectMapper()));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public EssayEditor.EssayDraft generate(Long userId, List<Reflection> reflections) {
        List<Reflection> ordered = reflections.stream()
                .sorted(Comparator.comparing(Reflection::getCreatedAt))
                .toList();
        if (ordered.isEmpty()) {
            throw new EssayGenerationException("에세이 생성에 사용할 사유가 없습니다.");
        }

        String prompt = promptTemplateLoader.load("essay", PROMPT_VERSION, Map.of(
                "reflections", formatReflections(ordered)));
        Optional<EssayEditor.EssayDraft> parsed = Optional.empty();
        for (int attempt = 0; attempt < MAX_ATTEMPTS && parsed.isEmpty(); attempt++) {
            String response = aiTextGenerator.generate(userId, "essay-generation", prompt, "");
            parsed = responseParser.parse(response, ordered);
        }
        return parsed.orElseThrow(() -> new EssayGenerationException(
                "AI 에세이 응답이 형식 검증을 통과하지 못했습니다."));
    }

    private String formatReflections(List<Reflection> reflections) {
        StringBuilder result = new StringBuilder();
        for (Reflection reflection : reflections) {
            result.append("ID=").append(reflection.getReflectionId())
                    .append("\n내용=").append(limit(reflection.getContent(), 2_000))
                    .append("\n\n");
        }
        return result.toString().trim();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public static class EssayGenerationException extends RuntimeException {
        public EssayGenerationException(String message) {
            super(message);
        }
    }
}
