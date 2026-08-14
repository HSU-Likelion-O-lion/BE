package com.likelion.olion.domain.book.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.likelion.olion.domain.book.config.BookApiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(1)
@ConditionalOnProperty(prefix = "book.api.kakao", name = "rest-api-key")
public class KakaoBookSearchProvider implements BookSearchProvider {
    private final RestClient restClient;
    private final BookApiProperties.Kakao properties;

    public KakaoBookSearchProvider(RestClient.Builder builder, BookApiProperties properties) {
        this.restClient = builder.baseUrl(properties.getKakao().getBaseUrl()).build();
        this.properties = properties.getKakao();
    }

    @Override
    public List<BookSearchResult> search(String query) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v3/search/book")
                        .queryParam("query", query)
                        .queryParam("size", 20)
                        .queryParam("target", "title")
                        .build())
                .header("Authorization", "KakaoAK " + properties.getRestApiKey())
                .retrieve()
                .body(JsonNode.class);

        List<BookSearchResult> results = new ArrayList<>();
        for (JsonNode item : response.path("documents")) {
            String authors = java.util.stream.StreamSupport.stream(
                            item.path("authors").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.joining(", "));
            results.add(new BookSearchResult(
                    item.path("title").asText(),
                    authors,
                    item.path("thumbnail").asText(),
                    item.path("publisher").asText(),
                    item.path("contents").asText(),
                    item.path("url").asText(),
                    "KAKAO",
                    extractIsbn13(item.path("isbn").asText()),
                    extractProviderBookId(item.path("isbn").asText())
            ));
        }
        return results;
    }

    static String extractIsbn13(String value) {
        return isbnTokens(value).stream()
                .filter(token -> token.length() == 13)
                .findFirst()
                .orElse(null);
    }

    static String extractProviderBookId(String value) {
        return isbnTokens(value).stream()
                .findFirst()
                .orElse(null);
    }

    private static List<String> isbnTokens(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.trim().split("\\s+"))
                .map(token -> token.replaceAll("[^0-9Xx]", "").toUpperCase())
                .filter(token -> token.length() == 10 || token.length() == 13)
                .toList();
    }
}
