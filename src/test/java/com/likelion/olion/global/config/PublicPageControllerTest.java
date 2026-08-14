package com.likelion.olion.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PublicPageControllerTest {
    @Test
    void 고객지원과_약관_URL을_공개한다() {
        PublicPageProperties properties = new PublicPageProperties();
        properties.setSupportUrl("https://example.com/support");
        properties.setTermsUrl("https://example.com/terms");
        properties.setPrivacyPolicyUrl("https://example.com/privacy");

        ResponseEntity<?> response = new PublicPageController(properties).getPublicPages();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(com.likelion.olion.global.common.response.ApiResponse.class);
        com.likelion.olion.global.common.response.ApiResponse<?> body =
                (com.likelion.olion.global.common.response.ApiResponse<?>) response.getBody();
        assertThat(body.data()).isEqualTo(new PublicPageResponse(
                "https://example.com/support",
                "https://example.com/terms",
                "https://example.com/privacy"));
    }
}
