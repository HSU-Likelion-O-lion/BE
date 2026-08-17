package com.likelion.olion.domain.reading.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.reading")
public class ReadingSessionTestProperties {
    private List<String> timerSkipEmails = List.of();

    public List<String> getTimerSkipEmails() {
        return timerSkipEmails;
    }

    public void setTimerSkipEmails(List<String> timerSkipEmails) {
        this.timerSkipEmails = timerSkipEmails;
    }

    public boolean isSkipAllowed(String email) {
        return email != null && timerSkipEmails.contains(email);
    }
}
