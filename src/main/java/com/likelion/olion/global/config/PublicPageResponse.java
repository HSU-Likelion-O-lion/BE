package com.likelion.olion.global.config;

public record PublicPageResponse(
        String supportUrl,
        String termsUrl,
        String privacyPolicyUrl
) {
    public static PublicPageResponse from(PublicPageProperties properties) {
        return new PublicPageResponse(
                properties.getSupportUrl(),
                properties.getTermsUrl(),
                properties.getPrivacyPolicyUrl());
    }
}
