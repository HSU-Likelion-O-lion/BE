package com.likelion.olion.global.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * OpenAI 호출을 한 곳으로 모으고, 키가 없거나 외부 호출에 실패하면
 * 도메인별 fallback 결과를 사용하도록 하는 얇은 경계입니다.
 */
@Component
public class AiTextGenerator {
    private static final Logger log = Logger.getLogger(AiTextGenerator.class.getName());

    private final ChatClient chatClient;
    private final AiUsageService aiUsageService;

    @Autowired
    public AiTextGenerator(
            ObjectProvider<ChatClient.Builder> builderProvider,
            ObjectProvider<AiUsageService> aiUsageServiceProvider
    ) {
        ChatClient.Builder builder = builderProvider.getIfAvailable();
        this.chatClient = builder == null ? null : builder.build();
        this.aiUsageService = aiUsageServiceProvider.getIfAvailable();
    }

    private AiTextGenerator(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.aiUsageService = null;
    }

    public static AiTextGenerator disabled() {
        return new AiTextGenerator((ChatClient) null);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String generate(String prompt, String fallback) {
        return generate(null, "unknown", prompt, fallback);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String generate(Long userId, String feature, String prompt, String fallback) {
        if (chatClient == null || aiUsageService == null) {
            return fallback;
        }

        Optional<Long> usageId = aiUsageService.start(userId, feature);
        if (usageId.isEmpty()) {
            return fallback;
        }
        long startedAt = System.nanoTime();
        try {
            String result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            boolean fallbackUsed = result == null || result.isBlank();
            aiUsageService.complete(usageId.get(),
                    fallbackUsed ? AiUsageStatus.FALLBACK : AiUsageStatus.SUCCESS,
                    Instant.now(), elapsedMillis(startedAt));
            return fallbackUsed ? fallback : result.trim();
        } catch (RuntimeException exception) {
            aiUsageService.complete(usageId.get(), AiUsageStatus.FALLBACK,
                    Instant.now(), elapsedMillis(startedAt));
            log.warning("AI 생성에 실패하여 fallback을 사용합니다: " + exception.getMessage());
            return fallback;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
