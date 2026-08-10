package com.likelion.olion.domain.emotion.controller;

import com.likelion.olion.domain.emotion.dto.EmotionCardResponse;
import com.likelion.olion.domain.emotion.service.EmotionCardService;
import com.likelion.olion.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emotion-cards")
public class EmotionCardController {
    private final EmotionCardService emotionCardService;

    public EmotionCardController(EmotionCardService emotionCardService) {
        this.emotionCardService = emotionCardService;
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<EmotionCardResponse>> getRandomCards() {
        return ResponseEntity.ok(ApiResponse.success(
                "카드를 조회했습니다.",
                emotionCardService.getRandomCards()
        ));
    }
}
