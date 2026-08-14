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
            "조용한", "사색하는", "춤추는", "밤을 걷는", "미소 짓는",
            "위로하는", "다정한", "깊어지는", "머뭇거리는", "반짝이는"
    };
    private static final String[] NOUNS = {
            "고양이", "나무", "여행자", "별빛", "바람", "서재", "연필", "조약돌", "바다", "시계"
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
