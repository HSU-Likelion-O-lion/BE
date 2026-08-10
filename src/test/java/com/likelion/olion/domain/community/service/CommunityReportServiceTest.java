package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityReportRequest;
import com.likelion.olion.domain.community.dto.CommunityReportResponse;
import com.likelion.olion.domain.community.entity.CommunityPost;
import com.likelion.olion.domain.community.entity.CommunityPostReport;
import com.likelion.olion.domain.community.entity.CommunityReportStatus;
import com.likelion.olion.domain.community.repository.CommunityPostReportRepository;
import com.likelion.olion.domain.community.repository.CommunityPostRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommunityReportServiceTest {
    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityPostReportRepository communityPostReportRepository;

    @Test
    void reportsPostWithNormalStatus() {
        CommunityReportService service = new CommunityReportService(
                communityPostRepository, communityPostReportRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "신고할 내용");
        CommunityPostReport savedReport = new CommunityPostReport(post, 1L, "부적절한 표현");
        ReflectionTestUtils.setField(savedReport, "reportId", 9L);
        given(communityPostRepository.findByIdForUpdate(200L)).willReturn(Optional.of(post));
        given(communityPostReportRepository.existsByPostPostIdAndUserId(200L, 1L)).willReturn(false);
        given(communityPostReportRepository.saveAndFlush(any(CommunityPostReport.class)))
                .willReturn(savedReport);
        given(communityPostReportRepository.countByPostPostId(200L)).willReturn(3L);

        CommunityReportResponse response = service.reportPost(
                1L, 200L, new CommunityReportRequest("  부적절한 표현  "));

        assertThat(response.reportId()).isEqualTo(9L);
        assertThat(response.status()).isEqualTo(CommunityReportStatus.NORMAL);
        ArgumentCaptor<CommunityPostReport> reportCaptor = ArgumentCaptor
                .forClass(CommunityPostReport.class);
        verify(communityPostReportRepository).saveAndFlush(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getReason()).isEqualTo("부적절한 표현");
    }

    @Test
    void movesFourthReportToPendingReview() {
        CommunityReportService service = new CommunityReportService(
                communityPostRepository, communityPostReportRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "신고할 내용");
        CommunityPostReport savedReport = new CommunityPostReport(post, 4L, null);
        ReflectionTestUtils.setField(savedReport, "reportId", 10L);
        given(communityPostRepository.findByIdForUpdate(200L)).willReturn(Optional.of(post));
        given(communityPostReportRepository.existsByPostPostIdAndUserId(200L, 4L)).willReturn(false);
        given(communityPostReportRepository.saveAndFlush(any(CommunityPostReport.class)))
                .willReturn(savedReport);
        given(communityPostReportRepository.countByPostPostId(200L)).willReturn(4L);

        CommunityReportResponse response = service.reportPost(
                4L, 200L, new CommunityReportRequest(null));

        assertThat(response.status()).isEqualTo(CommunityReportStatus.PENDING_REVIEW);
    }

    @Test
    void rejectsReportWhenPostDoesNotExist() {
        CommunityReportService service = new CommunityReportService(
                communityPostRepository, communityPostReportRepository);
        given(communityPostRepository.findByIdForUpdate(200L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.reportPost(
                1L, 200L, new CommunityReportRequest(null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
        verify(communityPostReportRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateReport() {
        CommunityReportService service = new CommunityReportService(
                communityPostRepository, communityPostReportRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "신고할 내용");
        given(communityPostRepository.findByIdForUpdate(200L)).willReturn(Optional.of(post));
        given(communityPostReportRepository.existsByPostPostIdAndUserId(200L, 1L)).willReturn(true);

        assertThatThrownBy(() -> service.reportPost(
                1L, 200L, new CommunityReportRequest(null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.CONFLICT);
        verify(communityPostReportRepository, never()).saveAndFlush(any());
    }

    @Test
    void mapsConcurrentDuplicateToConflict() {
        CommunityReportService service = new CommunityReportService(
                communityPostRepository, communityPostReportRepository);
        CommunityPost post = new CommunityPost(12L, 2L, "고요한 파도", "신고할 내용");
        given(communityPostRepository.findByIdForUpdate(200L)).willReturn(Optional.of(post));
        given(communityPostReportRepository.existsByPostPostIdAndUserId(200L, 1L)).willReturn(false);
        given(communityPostReportRepository.saveAndFlush(any(CommunityPostReport.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.reportPost(
                1L, 200L, new CommunityReportRequest(null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).errorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
