package com.likelion.olion.domain.reflectionshare.controller;

import com.likelion.olion.domain.reflectionshare.dto.ReflectionShareStatusResponse;
import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareStatus;
import com.likelion.olion.domain.reflectionshare.service.ReflectionShareService;
import org.junit.jupiter.api.Test;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ReflectionShareControllerTest {
    private final ReflectionShareService reflectionShareService = mock(ReflectionShareService.class);
    private final ReflectionShareController controller =
            new ReflectionShareController(reflectionShareService);
    private final Principal principal = () -> "1";

    @Test
    void returnsProcessingCodeOnlyWhileWorkIsInProgress() {
        given(reflectionShareService.getStatus(1L, 30L))
                .willReturn(new ReflectionShareStatusResponse(
                        ReflectionShareStatus.PROCESSING, null));

        var response = controller.getStatus(principal, 30L).getBody();

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("SUCCESS_PROCESSING");
    }

    @Test
    void doesNotReportFailedWorkAsProcessing() {
        given(reflectionShareService.getStatus(1L, 30L))
                .willReturn(new ReflectionShareStatusResponse(
                        ReflectionShareStatus.FAILED, null));

        var response = controller.getStatus(principal, 30L).getBody();

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("SUCCESS");
        assertThat(response.message()).isEqualTo("사유록 공유 이미지 생성에 실패했습니다.");
    }
}
