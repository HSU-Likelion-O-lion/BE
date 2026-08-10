package com.likelion.olion.domain.community.controller;

import com.likelion.olion.domain.community.dto.CommunityShareStatusResponse;
import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import com.likelion.olion.domain.community.service.CommunityShareService;
import com.likelion.olion.global.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityShareQueryControllerTest {
    @Mock
    private CommunityShareService communityShareService;

    private final Principal principal = () -> "1";

    @Test
    void returnsProcessingResponseForQueuedShare() {
        CommunityShareQueryController controller = new CommunityShareQueryController(
                communityShareService);
        given(communityShareService.getShareStatus(1L, 30L))
                .willReturn(new CommunityShareStatusResponse(CommunityShareStatus.QUEUED, null));

        ResponseEntity<ApiResponse<CommunityShareStatusResponse>> result =
                controller.getShareStatus(principal, 30L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo("SUCCESS_PROCESSING");
        assertThat(result.getBody().message()).isEqualTo("이미지 생성 중입니다.");
        assertThat(result.getBody().data().imageUrl()).isNull();
    }

    @Test
    void returnsSuccessResponseForCompletedShare() {
        CommunityShareQueryController controller = new CommunityShareQueryController(
                communityShareService);
        given(communityShareService.getShareStatus(1L, 30L)).willReturn(
                new CommunityShareStatusResponse(
                        CommunityShareStatus.COMPLETED,
                        "https://cdn.olion.com/share/200.png"));

        ResponseEntity<ApiResponse<CommunityShareStatusResponse>> result =
                controller.getShareStatus(principal, 30L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo("SUCCESS");
        assertThat(result.getBody().message()).isEqualTo("공유 이미지가 생성되었습니다.");
        assertThat(result.getBody().data().imageUrl())
                .isEqualTo("https://cdn.olion.com/share/200.png");
    }
}
