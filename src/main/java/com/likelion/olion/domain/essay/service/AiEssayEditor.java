package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.global.ai.AiTextGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiEssayEditor {
    private final AiTextGenerator aiTextGenerator;

    public AiEssayEditor(AiTextGenerator aiTextGenerator) {
        this.aiTextGenerator = aiTextGenerator;
    }

    public List<EssayEditor.ChapterDraft> organize(
            List<Reflection> reflections,
            EssayEditor fallbackEditor
    ) {
        List<Reflection> ordered = reflections.stream()
                .sorted(Comparator.comparing(Reflection::getCreatedAt))
                .toList();
        StringBuilder prompt = new StringBuilder("""
                당신은 독자의 사유를 따뜻한 에세이로 편집하는 편집자입니다.
                사용자의 문장을 새로 창작하거나 삭제하지 말고, 의미가 이어지는 순서와 장으로만 묶으세요.
                반드시 아래 형식만 반환하세요.
                CHAPTER|장 제목|사유 ID를 쉼표로 구분
                """);
        for (Reflection reflection : ordered) {
            prompt.append("\nID=").append(reflection.getReflectionId())
                    .append(" | 내용=").append(limit(reflection.getContent(), 600));
        }

        String response = aiTextGenerator.generate(prompt.toString(), "");
        List<EssayEditor.ChapterDraft> parsed = parse(response, ordered);
        return parsed.isEmpty() ? fallbackEditor.organize(ordered) : parsed;
    }

    private List<EssayEditor.ChapterDraft> parse(String response, List<Reflection> reflections) {
        Map<Long, Reflection> byId = new HashMap<>();
        reflections.forEach(reflection -> byId.put(reflection.getReflectionId(), reflection));
        List<EssayEditor.ChapterDraft> chapters = new ArrayList<>();

        for (String line : response.split("\\R")) {
            String[] parts = line.trim().split("\\|", 3);
            if (parts.length != 3 || !parts[0].equalsIgnoreCase("CHAPTER")) {
                continue;
            }
            List<Reflection> chapterReflections = new ArrayList<>();
            for (String token : parts[2].split(",")) {
                try {
                    Reflection reflection = byId.get(Long.parseLong(token.trim()));
                    if (reflection != null && !chapterReflections.contains(reflection)) {
                        chapterReflections.add(reflection);
                    }
                } catch (NumberFormatException ignored) {
                    // AI가 형식을 일부 어겨도 유효한 ID만 반영하고 fallback 판단은 아래에서 합니다.
                }
            }
            if (!chapterReflections.isEmpty() && !parts[1].isBlank()) {
                chapters.add(new EssayEditor.ChapterDraft(parts[1].trim(), chapterReflections));
            }
        }
        long parsedReflectionCount = chapters.stream()
                .flatMap(chapter -> chapter.reflections().stream())
                .distinct()
                .count();
        return parsedReflectionCount == reflections.size() ? chapters : List.of();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
