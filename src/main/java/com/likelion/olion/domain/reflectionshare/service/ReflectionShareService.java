package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareCreateRequest;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareCreateResponse;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareStatusResponse;
import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareThemeResponse;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShare;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareTheme;
import com.likelion.olion.domain.reflectionshare.event.ReflectionShareCreatedEvent;
import com.likelion.olion.domain.reflectionshare.repository.ReflectionShareRepository;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class ReflectionShareService {
    private static final Long DEFAULT_THEME_ID = 2L;

    private final ReflectionRepository reflectionRepository;
    private final ReflectionShareRepository reflectionShareRepository;
    private final ReflectionShareObjectStorage objectStorage;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public ReflectionShareService(
            ReflectionRepository reflectionRepository,
            ReflectionShareRepository reflectionShareRepository,
            ReflectionShareObjectStorage objectStorage,
            ApplicationEventPublisher eventPublisher,
            UserRepository userRepository
    ) {
        this.reflectionRepository = reflectionRepository;
        this.reflectionShareRepository = reflectionShareRepository;
        this.objectStorage = objectStorage;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ReflectionShareThemeResponse getThemes() {
        return new ReflectionShareThemeResponse(Arrays.stream(ReflectionShareTheme.values())
                .map(theme -> new ReflectionShareThemeResponse.Theme(
                        theme.themeId(),
                        theme.displayName(),
                        theme.swatch(),
                        objectStorage.resolveThemePreviewUrl(theme.themeId())))
                .toList());
    }

    @Transactional
    public ReflectionShareCreateResponse create(
            Long userId,
            Long reflectionId,
            ReflectionShareCreateRequest request
    ) {
        Reflection reflection = reflectionRepository.findByReflectionIdAndUserId(reflectionId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "본인의 사유록을 찾을 수 없습니다."));
        Long themeId = isProPlan(userId) ? request.themeId() : DEFAULT_THEME_ID;
        ReflectionShareTheme theme = ReflectionShareTheme.findById(themeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT, "지원하지 않는 공유 테마입니다."));

        ReflectionShare share = reflectionShareRepository.saveAndFlush(
                new ReflectionShare(reflection, userId, theme.themeId()));
        eventPublisher.publishEvent(new ReflectionShareCreatedEvent(share.getShareId()));
        return new ReflectionShareCreateResponse(share.getShareId(), share.getStatus());
    }

    @Transactional(readOnly = true)
    public ReflectionShareStatusResponse getStatus(Long userId, Long shareId) {
        ReflectionShare share = reflectionShareRepository.findByShareIdAndUserId(shareId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND, "공유 이미지 작업을 찾을 수 없습니다."));
        String imageUrl = share.getStatus() == ReflectionShareStatus.COMPLETED
                ? objectStorage.resolveUrl(share.getImageKey())
                : null;
        return new ReflectionShareStatusResponse(share.getStatus(), imageUrl);
    }

    private boolean isProPlan(Long userId) {
        return userRepository.findById(userId)
                .map(User::getPlan)
                .map(plan -> plan == SubscriptionPlan.PRO)
                .orElse(false);
    }
}
