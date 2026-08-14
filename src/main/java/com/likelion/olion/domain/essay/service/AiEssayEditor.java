package com.likelion.olion.domain.essay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.global.ai.AiTextGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AiEssayEditor {
    private final AiTextGenerator aiTextGenerator;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiEssayEditor(AiTextGenerator aiTextGenerator) {
        this(aiTextGenerator, new ObjectMapper());
    }

    public AiEssayEditor(AiTextGenerator aiTextGenerator, ObjectMapper objectMapper) {
        this.aiTextGenerator = aiTextGenerator;
        this.objectMapper = objectMapper;
    }

    public List<EssayEditor.ChapterDraft> organize(
            List<Reflection> reflections,
            EssayEditor fallbackEditor
    ) {
        return organize(null, reflections, fallbackEditor);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<EssayEditor.ChapterDraft> organize(
            Long userId,
            List<Reflection> reflections,
            EssayEditor fallbackEditor
    ) {
        List<Reflection> ordered = reflections.stream()
                .sorted(Comparator.comparing(Reflection::getCreatedAt))
                .toList();
        if (ordered.isEmpty()) {
            return fallbackEditor.organize(ordered);
        }

        StringBuilder prompt = new StringBuilder("""
                당신은 독자의 사유를 따뜻한 에세이로 편집하는 편집자입니다.
                사용자의 문장을 새로 창작하거나 삭제하지 말고, 의미가 이어지는 순서와 장으로만 묶으세요.
                반드시 JSON 객체만 반환하고 마크다운 코드 블록이나 설명을 붙이지 마세요.
                JSON 스키마: {"chapters":[{"title":"장 제목","reflectionIds":[사유 ID]}]}
                모든 사유 ID는 정확히 한 번씩 사용해야 합니다.
                """);
        for (Reflection reflection : ordered) {
            prompt.append("\nID=").append(reflection.getReflectionId())
                    .append(" | 내용=").append(limit(reflection.getContent(), 600));
        }

        String response = aiTextGenerator.generate(userId, "essay-editing", prompt.toString(), "");
        List<EssayEditor.ChapterDraft> parsed = parseJson(response, ordered);
        return parsed.isEmpty() ? fallbackEditor.organize(ordered) : parsed;
    }

    private List<EssayEditor.ChapterDraft> parseJson(String response, List<Reflection> reflections) {
        if (response == null || response.isBlank()) {
            return List.of();
        }

        Map<Long, Reflection> byId = new HashMap<>();
        reflections.forEach(reflection -> byId.put(reflection.getReflectionId(), reflection));
        Set<Long> usedIds = new HashSet<>();
        List<EssayEditor.ChapterDraft> chapters = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode chapterNodes = root == null || !root.isObject() ? null : root.get("chapters");
            if (chapterNodes == null || !chapterNodes.isArray() || chapterNodes.isEmpty()) {
                return List.of();
            }
            for (JsonNode chapterNode : chapterNodes) {
                JsonNode titleNode = chapterNode.get("title");
                JsonNode idNodes = chapterNode.get("reflectionIds");
                if (!chapterNode.isObject() || titleNode == null || !titleNode.isTextual()
                        || titleNode.asText().isBlank() || idNodes == null || !idNodes.isArray()
                        || idNodes.isEmpty()) {
                    return List.of();
                }

                List<Reflection> chapterReflections = new ArrayList<>();
                for (JsonNode idNode : idNodes) {
                    if (!idNode.isIntegralNumber()) {
                        return List.of();
                    }
                    long reflectionId = idNode.asLong();
                    Reflection reflection = byId.get(reflectionId);
                    if (reflection == null || !usedIds.add(reflectionId)) {
                        return List.of();
                    }
                    chapterReflections.add(reflection);
                }
                chapters.add(new EssayEditor.ChapterDraft(titleNode.asText().trim(), chapterReflections));
            }
        } catch (JsonProcessingException | RuntimeException ignored) {
            return List.of();
        }

        return usedIds.size() == byId.size() ? chapters : List.of();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
