package com.likelion.olion.domain.book.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "book.api")
public class BookApiProperties {
    private final Kakao kakao = new Kakao();
    private final Aladin aladin = new Aladin();

    public Kakao getKakao() {
        return kakao;
    }

    public Aladin getAladin() {
        return aladin;
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

    public static class Aladin {
        private String baseUrl = "https://www.aladin.co.kr";
        private String ttbKey = "";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getTtbKey() { return ttbKey; }
        public void setTtbKey(String ttbKey) { this.ttbKey = ttbKey; }
        public boolean isEnabled() {
            return !ttbKey.isBlank();
        }
    }
}
