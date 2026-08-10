package com.likelion.olion.domain.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "book.api")
public class BookApiProperties {
    private final Naver naver = new Naver();
    private final Kakao kakao = new Kakao();

    public Naver getNaver() {
        return naver;
    }

    public Kakao getKakao() {
        return kakao;
    }

    public static class Naver {
        private String baseUrl = "https://openapi.naver.com";
        private String clientId = "";
        private String clientSecret = "";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public boolean isEnabled() {
            return !clientId.isBlank() && !clientSecret.isBlank();
        }
    }

    public static class Kakao {
        private String baseUrl = "https://dapi.kakao.com";
        private String restApiKey = "";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getRestApiKey() { return restApiKey; }
        public void setRestApiKey(String restApiKey) { this.restApiKey = restApiKey; }
        public boolean isEnabled() {
            return !restApiKey.isBlank();
        }
    }
}
