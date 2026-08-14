package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.reflection.entity.Reflection;

import java.util.List;

public interface EssayEditor {
    List<ChapterDraft> organize(List<Reflection> reflections);

    record ChapterDraft(String title, List<Reflection> reflections) {
    }
}
