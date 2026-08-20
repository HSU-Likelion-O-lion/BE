package com.likelion.olion.domain.reflectionshare.event;

import com.likelion.olion.domain.reflectionshare.service.ReflectionShareImageWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReflectionShareCreatedEventListener {
    private final ReflectionShareImageWorker imageWorker;

    public ReflectionShareCreatedEventListener(ReflectionShareImageWorker imageWorker) {
        this.imageWorker = imageWorker;
    }

    @Async("reflectionShareTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(ReflectionShareCreatedEvent event) {
        imageWorker.process(event.shareId());
    }
}
