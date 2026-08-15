package com.likelion.olion.domain.book.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AladinBookSearchProviderTest {
    @Test
    void 알라딘_XML_응답을_공통_도서_결과로_변환한다() {
        String response = """
                <?xml version="1.0" encoding="UTF-8"?>
                <object>
                    <item>
                        <title>아몬드</title>
                        <author>손원평</author>
                        <cover>https://image.example/almond.jpg</cover>
                        <publisher>창비</publisher>
                        <description>책 <b>소개</b></description>
                        <link>https://www.aladin.co.kr/shop/wproduct.aspx?ISBN=123</link>
                        <isbn13>9788936434267</isbn13>
                        <itemId>123456789</itemId>
                        <categoryName>국내도서>소설/시/희곡>한국소설</categoryName>
                    </item>
                </object>
                """;

        List<BookSearchResult> results = AladinBookSearchProvider.parse(response);

        assertThat(results).singleElement().satisfies(book -> {
            assertThat(book.title()).isEqualTo("아몬드");
            assertThat(book.author()).isEqualTo("손원평");
            assertThat(book.coverImageUrl()).isEqualTo("https://image.example/almond.jpg");
            assertThat(book.description()).isEqualTo("책 소개");
            assertThat(book.provider()).isEqualTo("ALADIN");
            assertThat(book.isbn13()).isEqualTo("9788936434267");
            assertThat(book.providerBookId()).isEqualTo("123456789");
            assertThat(book.category()).isEqualTo("국내도서>소설/시/희곡>한국소설");
        });
    }
}
