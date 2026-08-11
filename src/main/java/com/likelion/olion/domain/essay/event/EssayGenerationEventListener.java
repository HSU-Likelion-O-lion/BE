package com.likelion.olion.domain.essay.event;

import com.likelion.olion.domain.essay.service.EssayGenerationWorker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EssayGenerationEventListener {
    private final EssayGenerationWorker essayGenerationWorker;

    public EssayGenerationEventListener(EssayGenerationWorker essayGenerationWorker) {
        this.essayGenerationWorker = essayGenerationWorker;
    }

    @Async("essayTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(EssayGenerationRequestedEvent event) {
        essayGenerationWorker.process(event.essayId());
    }
}
