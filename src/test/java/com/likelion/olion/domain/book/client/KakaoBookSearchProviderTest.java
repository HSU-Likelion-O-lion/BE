package com.likelion.olion.domain.book.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
