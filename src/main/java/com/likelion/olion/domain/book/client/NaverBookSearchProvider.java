package com.likelion.olion.domain.book.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.likelion.olion.domain.book.config.BookApiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "book.api.naver", name = {"client-id", "client-secret"})
public class NaverBookSearchProvider implements BookSearchProvider {
    private final RestClient restClient;
    private final BookApiProperties.Naver properties;

    public NaverBookSearchProvider(RestClient.Builder builder, BookApiProperties properties) {
        this.restClient = builder.baseUrl(properties.getNaver().getBaseUrl()).build();
        this.properties = properties.getNaver();
    }

    @Override
    public List<BookSearchResult> search(String query) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/search/book.json")
                        .queryParam("query", query)
                        .queryParam("display", 20)
                        .queryParam("sort", "sim")
                        .build())
                .header("X-Naver-Client-Id", properties.getClientId())
                .header("X-Naver-Client-Secret", properties.getClientSecret())
                .retrieve()
                .body(JsonNode.class);

        List<BookSearchResult> results = new ArrayList<>();
        for (JsonNode item : response.path("items")) {
            results.add(new BookSearchResult(
                    stripHtml(item.path("title").asText()),
                    item.path("author").asText(),
                    item.path("image").asText(),
                    item.path("publisher").asText(),
                    stripHtml(item.path("description").asText()),
                    item.path("link").asText(),
                    "NAVER"
            ));
        }
        return results;
    }

    private String stripHtml(String value) {
        return value == null ? "" : value.replaceAll("<[^>]*>", "");
    }
}
