package com.likelion.olion.domain.emotion.controller;

import com.likelion.olion.domain.emotion.dto.EmotionCardResponse;
import com.likelion.olion.domain.emotion.service.EmotionCardService;
import com.likelion.olion.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/emotion-cards")
@Tag(name = "감정 카드", description = "감정 진단에 사용하는 카드 API")
public class EmotionCardController {
    private final EmotionCardService emotionCardService;

    public EmotionCardController(EmotionCardService emotionCardService) {
        this.emotionCardService = emotionCardService;
    }

    @GetMapping("/random")
    @Operation(summary = "랜덤 감정 카드 조회", description = "감정 진단에 사용할 랜덤 감정 카드 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<EmotionCardResponse>> getRandomCards() {
        return ResponseEntity.ok(ApiResponse.success(
                "카드를 조회했습니다.",
                emotionCardService.getRandomCards()
        ));
    }
}
