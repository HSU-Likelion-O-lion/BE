package com.likelion.olion.domain.reading.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingSessionTestPropertiesTest {
    @Test
    void allowsOnlyConfiguredEmails() {
        ReadingSessionTestProperties properties = new ReadingSessionTestProperties();
        properties.setTimerSkipEmails(List.of("test1234@gmail.com"));

        assertThat(properties.isSkipAllowed("test1234@gmail.com")).isTrue();
        assertThat(properties.isSkipAllowed("someone-else@example.com")).isFalse();
        assertThat(properties.isSkipAllowed(null)).isFalse();
    }

    @Test
    void defaultsToNoAllowedEmails() {
        ReadingSessionTestProperties properties = new ReadingSessionTestProperties();

        assertThat(properties.isSkipAllowed("test1234@gmail.com")).isFalse();
    }
}
