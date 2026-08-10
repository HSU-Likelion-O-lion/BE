package com.likelion.olion.domain.reading.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.bookshelf.repository.UserBookRepository;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionStartResponse;
import com.likelion.olion.domain.reading.dto.ActiveReadingSessionResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatRequest;
import com.likelion.olion.domain.reading.dto.ReadingSessionHeartbeatResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionResumeResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionCompleteResponse;
import com.likelion.olion.domain.reading.dto.ReadingSessionAbandonResponse;
import com.likelion.olion.domain.reading.dto.ReadingInterruptionRequest;
import com.likelion.olion.domain.reading.dto.ReadingInterruptionResponse;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reading.entity.ReadingInterruption;
import com.likelion.olion.domain.reading.entity.ReadingInterruptionReason;
import com.likelion.olion.domain.reading.entity.ReadingSessionStatus;
import com.likelion.olion.domain.reading.repository.ReadingInterruptionRepository;
import com.likelion.olion.domain.reading.repository.ReadingSessionRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional(readOnly = true)
public class ReadingSessionService {
    private static final Set<Integer> ALLOWED_TARGET_MINUTES = Set.of(15, 30, 60);
    private static final long MAX_HEARTBEAT_DRIFT_SECONDS = 10;
    private static final String DEFAULT_AI_QUESTION = "오늘 읽은 부분에서 가장 마음에 남는 문장은?";

    private final ReadingSessionRepository readingSessionRepository;
    private final ReadingInterruptionRepository readingInterruptionRepository;
    private final UserBookRepository userBookRepository;

    @Autowired
    public ReadingSessionService(
            ReadingSessionRepository readingSessionRepository,
            ReadingInterruptionRepository readingInterruptionRepository,
            UserBookRepository userBookRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
        this.readingInterruptionRepository = readingInterruptionRepository;
        this.userBookRepository = userBookRepository;
    }

    public ReadingSessionService(
            ReadingSessionRepository readingSessionRepository,
            UserBookRepository userBookRepository
    ) {
        this(readingSessionRepository, null, userBookRepository);
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

    public ReadingSessionHeartbeatResponse heartbeat(
            Long userId,
            Long sessionId,
            ReadingSessionHeartbeatRequest request
    ) {
        ReadingSession session = readingSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (session.getStatus() != ReadingSessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        long serverElapsedSeconds = Math.max(0,
                session.getStartedAt().until(Instant.now(), ChronoUnit.SECONDS));
        int targetSeconds = session.getTargetMinutes() * 60;
        int remainingSeconds = (int) Math.max(0, targetSeconds - serverElapsedSeconds);
        boolean valid = Math.abs(serverElapsedSeconds - request.elapsedSeconds())
                <= MAX_HEARTBEAT_DRIFT_SECONDS;
        return new ReadingSessionHeartbeatResponse(remainingSeconds, valid);
    }

    public ReadingSessionResumeResponse resume(Long userId, Long sessionId) {
        ReadingSession session = readingSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (session.getStatus() != ReadingSessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        return new ReadingSessionResumeResponse(
                session.getStatus().name(),
                calculateRemainingSeconds(session));
    }

    @Transactional
    public ReadingSessionCompleteResponse complete(Long userId, Long sessionId) {
        ReadingSession session = readingSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (session.getStatus() != ReadingSessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        session.complete(DEFAULT_AI_QUESTION);
        return new ReadingSessionCompleteResponse(session.getStatus().name(), session.getAiQuestion());
    }

    @Transactional
    public ReadingSessionAbandonResponse abandon(Long userId, Long sessionId) {
        ReadingSession session = readingSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (session.getStatus() != ReadingSessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }

        session.abandon();
        return new ReadingSessionAbandonResponse(session.getStatus().name());
    }

    @Transactional
    public ReadingInterruptionResponse recordInterruption(
            Long userId,
            Long sessionId,
            ReadingInterruptionRequest request
    ) {
        ReadingSession session = readingSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        ReadingInterruptionReason reason = request.parsedReason();
        if (reason == null || (reason == ReadingInterruptionReason.OTHER
                && (request.customText() == null || request.customText().isBlank()))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        ReadingInterruption interruption = readingInterruptionRepository.save(
                new ReadingInterruption(session, reason, request.customText(), request.occurredAt()));
        return new ReadingInterruptionResponse(interruption.getInterruptionId());
    }

    @Transactional
    public void deleteRecoverySession(Long userId, Long sessionId) {
        ReadingSession session = readingSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        readingSessionRepository.delete(session);
    }

    private int calculateRemainingSeconds(ReadingSession session) {
        long elapsedSeconds = Math.max(0,
                session.getStartedAt().until(Instant.now(), ChronoUnit.SECONDS));
        int targetSeconds = session.getTargetMinutes() * 60;
        return (int) Math.max(0, targetSeconds - elapsedSeconds);
    }
}
