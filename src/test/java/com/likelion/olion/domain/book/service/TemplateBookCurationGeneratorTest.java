package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.entity.Book;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateBookCurationGeneratorTest {
    private final TemplateBookCurationGenerator generator = new TemplateBookCurationGenerator();

    @Test
    void generatesCurationFromBookAndLikedEmotionCard() {
        Book book = mock(Book.class);
        when(book.getTitle()).thenReturn("아몬드");

        String result = generator.generate(book, List.of(1, 4));

        assertThat(result).contains("지치고 쉬어가고 싶은 마음", "『아몬드』");
    }

    @Test
    void usesNeutralPhraseWhenNoCardWasLiked() {
        Book book = mock(Book.class);
        when(book.getTitle()).thenReturn("아몬드");

        String result = generator.generate(book, List.of());

        assertThat(result).contains("지금의 마음", "『아몬드』");
    }
}
