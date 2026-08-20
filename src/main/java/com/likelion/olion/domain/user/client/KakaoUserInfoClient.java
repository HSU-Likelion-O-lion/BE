package com.likelion.olion.domain.user.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class KakaoUserInfoClient {
    private final RestClient restClient;

    public KakaoUserInfoClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://kapi.kakao.com").build();
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        JsonNode response = restClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.hasNonNull("id")) {
            throw new IllegalStateException("카카오 사용자 정보를 확인할 수 없습니다.");
        }

        String providerId = String.valueOf(response.get("id").asLong());
        return new KakaoUserInfo(providerId);
    }

    public record KakaoUserInfo(String providerId) {
    }
}
