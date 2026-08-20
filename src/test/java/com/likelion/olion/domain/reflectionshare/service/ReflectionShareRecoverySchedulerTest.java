package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.repository.ReflectionShareRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReflectionShareRecoverySchedulerTest {
    @Test
    void reprocessesStaleQueuedAndProcessingShares() {
        ReflectionShareRepository repository = mock(ReflectionShareRepository.class);
        ReflectionShareImageWorker worker = mock(ReflectionShareImageWorker.class);
        ReflectionShare share = new ReflectionShare(mock(Reflection.class), 1L, 2L);
        ReflectionTestUtils.setField(share, "shareId", 30L);
        given(repository.findTop50ByStatusInAndUpdatedAtBeforeOrderByUpdatedAtAsc(
                anyCollection(), any())).willReturn(List.of(share));

        new ReflectionShareRecoveryScheduler(repository, worker, 120)
                .recoverStaleShares();

        verify(worker).process(30L);
    }
}
