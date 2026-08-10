package com.likelion.olion.domain.community.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityPostPolicyTest {
    private final CommunityPostPolicy communityPostPolicy = new CommunityPostPolicy();

    @Test
    void detectsProhibitedWordEvenWithWhitespace() {
        assertThat(communityPostPolicy.containsProhibitedWord("씨 발이라는 표현"))
                .isTrue();
    }

    @Test
    void createsSameNicknameForSameUserAndRoom() {
        String firstNickname = communityPostPolicy.createAnonymousNickname(1L, 12L);
        String secondNickname = communityPostPolicy.createAnonymousNickname(1L, 12L);

        assertThat(firstNickname).isEqualTo(secondNickname);
    }
}
