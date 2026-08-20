package com.likelion.olion.domain.reflectionshare.entity;

import java.util.Arrays;
import java.util.Optional;

public enum ReflectionShareTheme {
    PINK(1L, "핑크", "#F59ACA"),
    BLUE(2L, "블루", "#ADB9F2"),
    GREEN(3L, "그린", "#93E467"),
    YELLOW(4L, "옐로", "#F6E36A");

    private final Long themeId;
    private final String name;
    private final String swatch;

    ReflectionShareTheme(Long themeId, String name, String swatch) {
        this.themeId = themeId;
        this.name = name;
        this.swatch = swatch;
    }

    public Long themeId() {
        return themeId;
    }

    public String displayName() {
        return name;
    }

    public String swatch() {
        return swatch;
    }

    public String objectKey() {
        return "theme/" + themeId + ".png";
    }

    public static Optional<ReflectionShareTheme> findById(Long themeId) {
        return Arrays.stream(values())
                .filter(theme -> theme.themeId.equals(themeId))
                .findFirst();
    }
}
