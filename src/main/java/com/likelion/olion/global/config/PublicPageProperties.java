package com.likelion.olion.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.annotation.PostConstruct;

import java.net.URI;
import java.net.URISyntaxException;

@ConfigurationProperties(prefix = "app.public-pages")
public class PublicPageProperties {
    private String supportUrl;
    private String termsUrl;
    private String privacyPolicyUrl;

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }

    public String getTermsUrl() {
        return termsUrl;
    }

    public void setTermsUrl(String termsUrl) {
        this.termsUrl = termsUrl;
    }

    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }

    @PostConstruct
    void validate() {
        validateUrl("support-url", supportUrl);
        validateUrl("terms-url", termsUrl);
        validateUrl("privacy-policy-url", privacyPolicyUrl);
    }

    private void validateUrl(String propertyName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("app.public-pages." + propertyName + " must not be empty");
        }
        try {
            URI uri = new URI(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getFragment() != null) {
                throw new IllegalStateException("Invalid public page URL: " + value);
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid public page URL: " + value, exception);
        }
    }
}
