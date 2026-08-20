package com.likelion.olion.domain.reflectionshare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReflectionShareCreateRequest(
        @NotNull @Positive Long themeId
) {
}
