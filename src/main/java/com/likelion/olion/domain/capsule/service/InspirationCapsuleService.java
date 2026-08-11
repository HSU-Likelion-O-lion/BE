package com.likelion.olion.domain.capsule.service;

import com.likelion.olion.domain.capsule.dto.InspirationCapsuleTodayResponse;
import com.likelion.olion.domain.capsule.entity.InspirationCapsule;
import com.likelion.olion.domain.capsule.repository.InspirationCapsuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class InspirationCapsuleService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAY_CUTOFF_HOUR = 4;

    private final InspirationCapsuleRepository inspirationCapsuleRepository;

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

    LocalDate today() {
        return ZonedDateTime.now(KST).minusHours(DAY_CUTOFF_HOUR).toLocalDate();
    }
}
