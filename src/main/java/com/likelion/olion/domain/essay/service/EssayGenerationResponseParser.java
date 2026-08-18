package com.likelion.olion.domain.essay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.olion.domain.reflection.entity.Reflection;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class EssayGenerationResponseParser {
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_CONTENT_LENGTH = 3;
    private static final int MAX_CONTENT_LENGTH = 500;
    private static final int MAX_ESSAY_LENGTH = 2_500;

    private final ObjectMapper objectMapper;

    @Autowired
    public EssayGenerationResponseParser() {
        this(new ObjectMapper());
    }

    public EssayGenerationResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<EssayEditor.EssayDraft> parse(String response, List<Reflection> reflections) {
        if (response == null || response.isBlank() || reflections.isEmpty()) {
            return Optional.empty();
        }

        Map<Long, Reflection> reflectionsById = new HashMap<>();
        reflections.forEach(reflection -> reflectionsById.put(reflection.getReflectionId(), reflection));
        Set<Long> usedReflectionIds = new HashSet<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            if (root == null || !root.isObject()) {
                return Optional.empty();
            }

            String title = text(root.get("title"));
            JsonNode chapterNodes = root.get("chapters");
            if (!isValidTitle(title) || chapterNodes == null || !chapterNodes.isArray()
                    || chapterNodes.isEmpty()) {
                return Optional.empty();
            }

            List<EssayEditor.ChapterDraft> chapters = new ArrayList<>();
            int totalContentLength = 0;
            for (JsonNode chapterNode : chapterNodes) {
                if (!chapterNode.isObject()) {
                    return Optional.empty();
                }
                String chapterTitle = text(chapterNode.get("title"));
                String content = text(chapterNode.get("content"));
                JsonNode reflectionIdNodes = chapterNode.get("reflectionIds");
                if (!isValidTitle(chapterTitle) || !isValidContent(content)
                        || reflectionIdNodes == null || !reflectionIdNodes.isArray()
                        || reflectionIdNodes.isEmpty()) {
                    return Optional.empty();
                }
                totalContentLength += content.length();
                if (totalContentLength > MAX_ESSAY_LENGTH) {
                    return Optional.empty();
                }

                List<Reflection> chapterReflections = new ArrayList<>();
                for (JsonNode reflectionIdNode : reflectionIdNodes) {
                    if (!reflectionIdNode.isIntegralNumber()) {
                        return Optional.empty();
                    }
                    long reflectionId = reflectionIdNode.asLong();
                    Reflection reflection = reflectionsById.get(reflectionId);
                    if (reflection == null || !usedReflectionIds.add(reflectionId)) {
                        return Optional.empty();
                    }
                    chapterReflections.add(reflection);
                }
                chapters.add(new EssayEditor.ChapterDraft(chapterTitle, content, chapterReflections));
            }

            if (usedReflectionIds.size() != reflectionsById.size()) {
                return Optional.empty();
            }
            return Optional.of(new EssayEditor.EssayDraft(title, chapters));
        } catch (JsonProcessingException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText().trim() : "";
    }

    private boolean isValidTitle(String value) {
        return !value.isBlank() && value.length() <= MAX_TITLE_LENGTH;
    }

    private boolean isValidContent(String value) {
        return value.length() >= MIN_CONTENT_LENGTH && value.length() <= MAX_CONTENT_LENGTH;
    }
}
