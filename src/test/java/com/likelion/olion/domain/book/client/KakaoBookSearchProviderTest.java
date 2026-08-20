package com.likelion.olion.domain.book.client;

import com.likelion.olion.domain.book.config.BookApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoBookSearchProviderTest {
    @Test
    void ISBN_문자열에서_ISBN13을_추출한다() {
        assertThat(KakaoBookSearchProvider.extractIsbn13("8936434264 9788936434267"))
                .isEqualTo("9788936434267");
    }

    @Test
    void ISBN13이_없으면_제공자_식별자로_ISBN10을_사용한다() {
        assertThat(KakaoBookSearchProvider.extractIsbn13("89-364-3426-4")).isNull();
        assertThat(KakaoBookSearchProvider.extractProviderBookId("89-364-3426-4"))
                .isEqualTo("8936434264");
    }

    @Test
    void parsesRealKakaoSearchResponseShape() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "documents": [
                            {
                              "title": "어린 왕자",
                              "authors": ["앙투안 드 생텍쥐페리"],
                              "thumbnail": "https://example.com/cover.jpg",
                              "publisher": "열린책들",
                              "contents": "설명",
                              "url": "https://example.com/book",
                              "isbn": "8936434264 9788936434267"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        BookApiProperties properties = new BookApiProperties();
        properties.getKakao().setRestApiKey("test-key");
        KakaoBookSearchProvider provider = new KakaoBookSearchProvider(builder, properties);

        List<BookSearchResult> results = provider.search("어린 왕자");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("어린 왕자");
        assertThat(results.get(0).isbn13()).isEqualTo("9788936434267");
    }
}
