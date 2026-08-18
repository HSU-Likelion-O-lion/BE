package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.book.entity.Book;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmotionBookRecommendationService {
    private static final int RECOMMENDATION_SIZE = 3;

    private static final Map<Integer, Set<String>> CARD_KEYWORDS = Map.ofEntries(
            Map.entry(1, Set.of("무기력", "회복", "휴식", "위로", "감정")),
            Map.entry(2, Set.of("불안", "집중", "휴식", "마음", "디지털")),
            Map.entry(3, Set.of("비교", "우울", "관계", "자존", "위로")),
            Map.entry(4, Set.of("시작", "용기", "변화", "습관", "집중")),
            Map.entry(5, Set.of("기대", "희망", "미래", "삶", "회복")),
            Map.entry(6, Set.of("짜증", "감정", "관계", "평온", "마음")),
            Map.entry(7, Set.of("관계", "고독", "혼자", "사람", "마음")),
            Map.entry(8, Set.of("집중", "몰입", "독서", "습관", "시작")),
            Map.entry(9, Set.of("방향", "성장", "삶", "선택", "의미")),
            Map.entry(10, Set.of("공허", "의미", "삶", "마음", "관계")),
            Map.entry(11, Set.of("조언", "성장", "변화", "용기", "현실")),
            Map.entry(12, Set.of("휴식", "쉼", "회복", "마음", "위로"))
    );

    public List<Book> recommend(List<Integer> likedCardIds, List<Book> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        Set<String> keywords = likedCardIds.stream()
                .map(CARD_KEYWORDS::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Map<Book, Integer> scores = new HashMap<>();
        for (Book book : candidates) {
            scores.put(book, score(book, keywords));
        }

        return candidates.stream()
                .filter(book -> book.getBookId() != null)
                .distinct()
                .sorted(Comparator
                        .comparingInt((Book book) -> scores.getOrDefault(book, 0)).reversed()
                        .thenComparing(book -> normalize(book.getTitle()))
                        .thenComparing(Book::getBookId))
                .limit(RECOMMENDATION_SIZE)
                .toList();
    }

    private int score(Book book, Set<String> keywords) {
        String description = normalize(book.getDescription());
        String summary = normalize(book.getAiSummary());
        String title = normalize(book.getTitle());
        int score = 0;

        for (String keyword : keywords) {
            if (title.contains(keyword)) {
                score += 1;
            }
            if (description.contains(keyword)) {
                score += 10;
            }
            if (summary.contains(keyword)) {
                score += 8;
            }
        }
        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("\\s+", "");
    }
}
