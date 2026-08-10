package com.likelion.olion.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
        String nickname
) {
}
