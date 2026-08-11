package com.likelion.olion.domain.capsule.service;

import com.likelion.olion.domain.capsule.dto.InspirationCapsuleHistoryResponse;
import com.likelion.olion.domain.capsule.dto.InspirationCapsuleOpenResponse;
import com.likelion.olion.domain.capsule.dto.InspirationCapsuleTodayResponse;
import com.likelion.olion.domain.capsule.entity.InspirationCapsule;
import com.likelion.olion.domain.capsule.repository.InspirationCapsuleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InspirationCapsuleService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAY_CUTOFF_HOUR = 4;

    private static final List<Quote> QUOTES = List.of(
            new Quote("삶이 그대를 속일지라도 슬퍼하거나 노여워하지 말라", "삶이 그대를 속일지라도"),
            new Quote("행복한 가정은 모두 엇비슷하고, 불행한 가정은 저마다의 이유로 불행하다", "안나 카레니나"),
            new Quote("나는 삶의 본질만을 마주하며, 진지하게 살아보고 싶었다", "월든"),
            new Quote("최고의 시절이자, 동시에 최악의 시절이었다", "두 도시 이야기")
    );

    private final InspirationCapsuleRepository inspirationCapsuleRepository;
    private final SecureRandom random = new SecureRandom();

    public InspirationCapsuleService(InspirationCapsuleRepository inspirationCapsuleRepository) {
        this.inspirationCapsuleRepository = inspirationCapsuleRepository;
    }

    @Transactional(readOnly = true)
    public InspirationCapsuleTodayResponse getToday(Long userId) {
        Optional<InspirationCapsule> capsule = inspirationCapsuleRepository
                .findByUserIdAndOpenedDate(userId, today());
        return capsule
                .map(c -> new InspirationCapsuleTodayResponse(true, c.getQuoteText(), c.getBookTitle()))
                .orElseGet(() -> new InspirationCapsuleTodayResponse(false, null, null));
    }

    @Transactional
    public InspirationCapsuleOpenResponse open(Long userId) {
        LocalDate today = today();
        Optional<InspirationCapsule> existing = inspirationCapsuleRepository
                .findByUserIdAndOpenedDate(userId, today);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        Quote quote = QUOTES.get(random.nextInt(QUOTES.size()));
        InspirationCapsule capsule = new InspirationCapsule(userId, quote.text(), quote.bookTitle(), today);
        try {
            inspirationCapsuleRepository.saveAndFlush(capsule);
        } catch (DataIntegrityViolationException exception) {
            return inspirationCapsuleRepository.findByUserIdAndOpenedDate(userId, today)
                    .map(this::toResponse)
                    .orElseThrow(() -> exception);
        }
        return toResponse(capsule);
    }

    @Transactional(readOnly = true)
    public InspirationCapsuleHistoryResponse getHistory(Long userId) {
        return InspirationCapsuleHistoryResponse.from(
                inspirationCapsuleRepository.findByUserIdOrderByOpenedDateDesc(userId));
    }

    LocalDate today() {
        return ZonedDateTime.now(KST).minusHours(DAY_CUTOFF_HOUR).toLocalDate();
    }

    private InspirationCapsuleOpenResponse toResponse(InspirationCapsule capsule) {
        return new InspirationCapsuleOpenResponse(capsule.getQuoteText(), capsule.getBookTitle());
    }

    private record Quote(String text, String bookTitle) {
    }
}
