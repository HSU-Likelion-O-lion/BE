package com.likelion.olion.domain.reflectionshare.dto;

import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;

public record ReflectionShareStatusResponse(
        ReflectionShareStatus status,
        String imageUrl
) {
}
