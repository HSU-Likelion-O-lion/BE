package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;

import java.util.List;

public interface EssayEditor {
    List<ChapterDraft> organize(List<Reflection> reflections);

    record EssayDraft(String title, List<ChapterDraft> chapters) {
    }

    record ChapterDraft(String title, String content, List<Reflection> reflections) {
        public ChapterDraft(String title, List<Reflection> reflections) {
            this(title, joinContents(reflections), reflections);
        }

        private static String joinContents(List<Reflection> reflections) {
            return reflections.stream()
                    .map(Reflection::getContent)
                    .filter(content -> content != null && !content.isBlank())
                    .reduce((left, right) -> left + "\n\n" + right)
                    .orElse("");
        }
    }
}
