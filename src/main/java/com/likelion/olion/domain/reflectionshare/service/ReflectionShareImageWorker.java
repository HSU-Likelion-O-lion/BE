package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.repository.ReflectionShareRepository;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ReflectionShareImageWorker {
    private static final Logger log = Logger.getLogger(ReflectionShareImageWorker.class.getName());

    private final ReflectionShareRepository reflectionShareRepository;
    private final UserRepository userRepository;
    private final ReflectionShareImageRenderer imageRenderer;
    private final ReflectionShareObjectStorage objectStorage;

    public ReflectionShareImageWorker(
            ReflectionShareRepository reflectionShareRepository,
            UserRepository userRepository,
            ReflectionShareImageRenderer imageRenderer,
            ReflectionShareObjectStorage objectStorage
    ) {
        this.reflectionShareRepository = reflectionShareRepository;
        this.userRepository = userRepository;
        this.imageRenderer = imageRenderer;
        this.objectStorage = objectStorage;
    }

    @Transactional
    public void process(Long shareId) {
        ReflectionShare share = reflectionShareRepository.findByIdForUpdate(shareId).orElse(null);
        if (share == null || !isProcessable(share.getStatus())) {
            return;
        }

        share.startProcessing();
        try {
            User user = userRepository.findById(share.getUserId())
                    .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));
            byte[] png = imageRenderer.render(new ReflectionShareRenderRequest(
                    share.getThemeId(),
                    share.getReflection().getContent(),
                    user.getNickname(),
                    user.getProfileImageUrl(),
                    share.getReflection().getCreatedAt()));
            share.complete(objectStorage.storeShare(share.getShareId(), png));
        } catch (RuntimeException exception) {
            share.fail(exception.getMessage());
            log.log(Level.WARNING,
                    "Reflection share image generation failed for shareId=" + shareId,
                    exception);
        }
    }

    private boolean isProcessable(ReflectionShareStatus status) {
        return status == ReflectionShareStatus.QUEUED
                || status == ReflectionShareStatus.PROCESSING;
    }
}
