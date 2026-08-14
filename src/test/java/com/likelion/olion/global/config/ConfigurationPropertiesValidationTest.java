package com.likelion.olion.global.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationPropertiesValidationTest {
    @Test
    void CORS_와일드카드를_허용하지_않는다() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("*"));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("wildcard");
    }

    @Test
    void 공개_페이지_URL이_없으면_실패한다() {
        PublicPageProperties properties = new PublicPageProperties();
        properties.setSupportUrl("https://example.com/support");
        properties.setTermsUrl("https://example.com/terms");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("privacy-policy-url");
    }
}
