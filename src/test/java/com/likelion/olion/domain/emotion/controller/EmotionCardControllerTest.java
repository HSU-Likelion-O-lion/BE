package com.likelion.olion.domain.emotion.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.olion.domain.emotion.service.EmotionCardService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmotionCardControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new EmotionCardController(new EmotionCardService()))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 무작위_감정카드_5개를_조회한다() throws Exception {
        String body = mockMvc.perform(get("/api/emotion-cards/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.data.cards").isArray())
                .andExpect(jsonPath("$.data.cards.length()").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode cards = objectMapper.readTree(body).path("data").path("cards");
        assertThat(cards).hasSize(5);
        assertThat(cards.findValuesAsText("cardId")).doesNotHaveDuplicates();
    }
}
