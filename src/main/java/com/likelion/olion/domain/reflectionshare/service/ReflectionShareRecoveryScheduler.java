package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.repository.ReflectionShareRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReflectionShareRecoveryScheduler {
    private static final List<ReflectionShareStatus> RECOVERABLE_STATUSES = List.of(
            ReflectionShareStatus.QUEUED,
            ReflectionShareStatus.PROCESSING);

    private final ReflectionShareRepository reflectionShareRepository;
    private final ReflectionShareImageWorker imageWorker;
    private final long staleAfterSeconds;

    public ReflectionShareRecoveryScheduler(
            ReflectionShareRepository reflectionShareRepository,
            ReflectionShareImageWorker imageWorker,
            @Value("${reflection-share.recovery-stale-seconds:120}") long staleAfterSeconds
    ) {
        this.reflectionShareRepository = reflectionShareRepository;
        this.imageWorker = imageWorker;
        this.staleAfterSeconds = Math.max(30, staleAfterSeconds);
    }

    @Scheduled(
            initialDelayString = "${reflection-share.recovery-initial-delay-ms:30000}",
            fixedDelayString = "${reflection-share.recovery-interval-ms:60000}"
    )
    public void recoverStaleShares() {
        Instant staleBefore = Instant.now().minusSeconds(staleAfterSeconds);
        reflectionShareRepository
                .findTop50ByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                        RECOVERABLE_STATUSES, staleBefore)
                .stream()
                .map(ReflectionShare::getShareId)
                .forEach(imageWorker::process);
    }
}
