package com.likelion.olion.domain.community.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class CommunityPostPolicy {
    private static final List<String> PROHIBITED_WORDS = List.of(
            "시발", "씨발", "병신", "개새끼"
    );
    private static final String[] ADJECTIVES = {
            "고요한", "따뜻한", "푸른", "느긋한", "다정한", "포근한", "맑은", "잔잔한"
    };
    private static final String[] NOUNS = {
            "파도", "새벽", "구름", "별빛", "숲", "바람", "달빛", "여울"
    };

    public boolean containsProhibitedWord(String content) {
        if (content == null) {
            return false;
        }
        String normalizedContent = content.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        return PROHIBITED_WORDS.stream().anyMatch(normalizedContent::contains);
    }

    public String createAnonymousNickname(Long userId, Long roomId) {
        int adjectiveIndex = Math.floorMod(Objects.hash(userId, roomId), ADJECTIVES.length);
        int nounIndex = Math.floorMod(Objects.hash(roomId, userId), NOUNS.length);
        return ADJECTIVES[adjectiveIndex] + " " + NOUNS[nounIndex];
    }
}
