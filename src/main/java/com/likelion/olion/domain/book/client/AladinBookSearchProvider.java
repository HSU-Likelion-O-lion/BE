package com.likelion.olion.domain.book.client;

import com.likelion.olion.domain.book.config.BookApiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.InputSource;

@Component
@Order(2)
@ConditionalOnProperty(prefix = "book.api.aladin", name = "ttb-key")
public class AladinBookSearchProvider implements BookSearchProvider {
    private final RestClient restClient;
    private final BookApiProperties.Aladin properties;

    public AladinBookSearchProvider(RestClient.Builder builder, BookApiProperties properties) {
        this.restClient = builder.baseUrl(properties.getAladin().getBaseUrl()).build();
        this.properties = properties.getAladin();
    }

    @Override
    public List<BookSearchResult> search(String query) {
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/ttb/api/ItemSearch.aspx")
                        .queryParam("ttbkey", properties.getTtbKey())
                        .queryParam("Query", query)
                        .queryParam("QueryType", "Keyword")
                        .queryParam("MaxResults", 20)
                        .queryParam("start", 1)
                        .queryParam("SearchTarget", "Book")
                        .queryParam("output", "xml")
                        .queryParam("Version", "20131101")
                        .build())
                .retrieve()
                .body(String.class);

        return parse(response);
    }

    static List<BookSearchResult> parse(String response) {
        if (response == null || response.isBlank()) {
            return List.of();
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(response)));
            NodeList items = document.getElementsByTagName("item");
            List<BookSearchResult> results = new ArrayList<>();

            for (int index = 0; index < items.getLength(); index++) {
                Node item = items.item(index);
                results.add(new BookSearchResult(
                        text(item, "title"),
                        text(item, "author"),
                        text(item, "cover"),
                        text(item, "publisher"),
                        stripHtml(text(item, "description")),
                        text(item, "link"),
                        "ALADIN",
                        normalizeIsbn13(text(item, "isbn13")),
                        normalize(text(item, "itemId"))
                ));
            }
            return results;
        } catch (Exception exception) {
            throw new IllegalStateException("알라딘 도서 검색 응답을 파싱할 수 없습니다.", exception);
        }
    }

    private static String text(Node parent, String tagName) {
        NodeList children = ((org.w3c.dom.Element) parent).getElementsByTagName(tagName);
        return children.getLength() == 0 ? "" : children.item(0).getTextContent().trim();
    }

    private static String stripHtml(String value) {
        return value.replaceAll("<[^>]*>", "");
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String normalizeIsbn13(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replaceAll("[^0-9]", "");
        return normalized.length() == 13 ? normalized : null;
    }
}
