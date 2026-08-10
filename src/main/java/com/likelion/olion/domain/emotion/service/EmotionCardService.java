package com.likelion.olion.domain.emotion.service;

import com.likelion.olion.domain.emotion.dto.EmotionCardResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class EmotionCardService {
    private static final int CARD_COUNT = 5;
    private static final List<EmotionCardResponse.Card> CARDS = List.of(
            new EmotionCardResponse.Card(1, "오늘따라 아무것도 하기 싫고 무기력하다."),
            new EmotionCardResponse.Card(2, "스마트폰을 손에 쥐고 있지 않으면 왠지 모르게 불안하다."),
            new EmotionCardResponse.Card(3, "남들의 일상을 보며 내 현실과 비교하고 우울해진 적이 있다."),
            new EmotionCardResponse.Card(4, "머릿속에 생각만 많고 막상 첫 시작을 내딛기가 어렵다."),
            new EmotionCardResponse.Card(5, "잠자리에 누워도 내일 하루가 전혀 기대되지 않는다."),
            new EmotionCardResponse.Card(6, "최근 들어 사소한 일에도 쉽게 짜증이 나고 날이 서 있다."),
            new EmotionCardResponse.Card(7, "인간관계가 피곤해서 지금은 그저 완벽히 혼자 있고 싶다."),
            new EmotionCardResponse.Card(8, "어떤 일이나 글에 10분 이상 깊게 집중해 본 지 꽤 오래되었다."),
            new EmotionCardResponse.Card(9, "내가 지금 올바른 방향으로 잘 살고 있는 건지 확신이 안 선다."),
            new EmotionCardResponse.Card(10, "누구보다 바쁘게 살고 있는데, 문득 묘한 공허함이 밀려온다."),
            new EmotionCardResponse.Card(11, "지금 나에게는 따뜻한 위로보다 차갑고 뼈 때리는 조언이 필요하다."),
            new EmotionCardResponse.Card(12, "이유 불문하고 그저 아무 생각 없이 뇌를 끄고 푹 쉬고 싶다.")
    );

    public EmotionCardResponse getRandomCards() {
        List<EmotionCardResponse.Card> shuffledCards = new ArrayList<>(CARDS);
        Collections.shuffle(shuffledCards);
        return new EmotionCardResponse(
                shuffledCards.stream()
                        .limit(CARD_COUNT)
                        .toList()
        );
    }
}
