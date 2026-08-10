package com.likelion.olion.domain.reading.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.dto.ActiveReadingSessionResponse;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ReadingSessionService {
    private static final Set<Integer> ALLOWED_TARGET_MINUTES = Set.of(15, 30, 60);

    private final ReadingSessionRepository readingSessionRepository;
    private final UserBookRepository userBookRepository;

    public ReadingSessionService(
            ReadingSessionRepository readingSessionRepository,
            UserBookRepository userBookRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
        this.userBookRepository = userBookRepository;
    }

    @Transactional
    public ReadingSessionStartResponse start(Long userId, ReadingSessionStartRequest request) {
        UserBook userBook = userBookRepository.findByUserBookIdAndUserId(request.userBookId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        if (!ALLOWED_TARGET_MINUTES.contains(request.targetMinutes())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (readingSessionRepository.existsByUserIdAndStatus(userId, ReadingSessionStatus.IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        ReadingSession session = readingSessionRepository.save(
                new ReadingSession(userId, userBook, request.targetMinutes()));
        return ReadingSessionStartResponse.from(session);
    }

    public ActiveReadingSessionResponse getActive(Long userId) {
        return ActiveReadingSessionResponse.from(readingSessionRepository
                .findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, ReadingSessionStatus.IN_PROGRESS)
                .orElse(null));
    }
}
