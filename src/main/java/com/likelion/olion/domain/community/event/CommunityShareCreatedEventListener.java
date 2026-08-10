package com.likelion.olion.domain.community.event;

import com.likelion.olion.domain.community.service.CommunityShareImageWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CommunityShareCreatedEventListener {
    private final CommunityShareImageWorker imageWorker;

    public CommunityShareCreatedEventListener(CommunityShareImageWorker imageWorker) {
        this.imageWorker = imageWorker;
    }

    @Async("communityShareTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(CommunityShareCreatedEvent event) {
        imageWorker.process(event.shareId());
    }
}
