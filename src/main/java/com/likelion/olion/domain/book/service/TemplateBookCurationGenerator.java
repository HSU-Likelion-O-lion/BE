package com.likelion.olion.domain.book.service;

import com.likelion.olion.domain.book.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TemplateBookCurationGenerator implements BookCurationGenerator {
    private static final Set<Integer> EXHAUSTION_CARDS = Set.of(1, 5, 10, 12);
    private static final Set<Integer> ANXIETY_CARDS = Set.of(2, 3, 9);
    private static final Set<Integer> FOCUS_CARDS = Set.of(4, 8);
    private static final Set<Integer> RELATIONSHIP_CARDS = Set.of(6, 7, 11);

    @Override
    public String generate(Book book, List<Integer> likedCardIds) {
        String emotionPhrase = resolveEmotionPhrase(likedCardIds);
        String title = book.getTitle() == null || book.getTitle().isBlank()
                ? "이 책"
                : "『" + book.getTitle().trim() + "』";
        return emotionPhrase + "을 위한 " + title + "의 이야기를 천천히 만나보세요.";
    }

    private String resolveEmotionPhrase(List<Integer> likedCardIds) {
        if (likedCardIds.stream().anyMatch(EXHAUSTION_CARDS::contains)) {
            return "지치고 쉬어가고 싶은 마음";
        }
        if (likedCardIds.stream().anyMatch(ANXIETY_CARDS::contains)) {
            return "불안하고 흔들리는 마음";
        }
        if (likedCardIds.stream().anyMatch(FOCUS_CARDS::contains)) {
            return "다시 집중하고 싶은 마음";
        }
        if (likedCardIds.stream().anyMatch(RELATIONSHIP_CARDS::contains)) {
            return "복잡한 관계와 감정";
        }
        return "지금의 마음";
    }
}
