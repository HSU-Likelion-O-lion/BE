package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reflection.entity.Reflection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TemplateEssayEditorTest {
    @Test
    void groupsReflectionsIntoChaptersOfFive() {
        TemplateEssayEditor editor = new TemplateEssayEditor();
        List<Reflection> reflections = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            reflections.add(new Reflection(1L, mockSession(), "사유 " + i));
        }

        List<EssayEditor.ChapterDraft> chapters = editor.organize(reflections);

        assertThat(chapters).hasSize(7);
        assertThat(chapters.get(0).title()).isEqualTo("1장");
        assertThat(chapters.get(0).reflections()).hasSize(5);
        assertThat(chapters.get(6).reflections()).hasSize(2);
    }

    private ReadingSession mockSession() {
        return new ReadingSession(1L, mock(UserBook.class), 30);
    }
}
