package com.likelion.olion.domain.user.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoUserInfoClientTest {

    @Test
    void parsesProviderIdFromRealKakaoResponseShape() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://kapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(
                        "{\"id\": 1234567890, \"connected_at\": \"2026-01-01T00:00:00Z\"}",
                        MediaType.APPLICATION_JSON));
        KakaoUserInfoClient client = new KakaoUserInfoClient(builder);

        KakaoUserInfoClient.KakaoUserInfo result = client.getUserInfo("test-token");

        assertThat(result.providerId()).isEqualTo("1234567890");
        server.verify();
    }

    @Test
    void throwsWhenResponseHasNoId() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://kapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        KakaoUserInfoClient client = new KakaoUserInfoClient(builder);

        assertThatThrownBy(() -> client.getUserInfo("test-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("카카오 사용자 정보를 확인할 수 없습니다.");
    }
}
