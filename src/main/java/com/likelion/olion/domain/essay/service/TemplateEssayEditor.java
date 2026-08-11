package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TemplateEssayEditor implements EssayEditor {
    private static final int CHAPTER_SIZE = 5;

    @Override
    public List<ChapterDraft> organize(List<Reflection> reflections) {
        List<Reflection> sorted = reflections.stream()
                .sorted(Comparator.comparing(Reflection::getCreatedAt))
                .toList();

        List<ChapterDraft> chapters = new ArrayList<>();
        int chapterNo = 1;
        for (int i = 0; i < sorted.size(); i += CHAPTER_SIZE) {
            List<Reflection> chunk = sorted.subList(i, Math.min(i + CHAPTER_SIZE, sorted.size()));
            chapters.add(new ChapterDraft(chapterNo + "장", chunk));
            chapterNo++;
        }
        return chapters;
    }
}
