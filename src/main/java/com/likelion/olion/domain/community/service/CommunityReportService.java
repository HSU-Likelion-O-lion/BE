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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunityReportService {
    private static final int REVIEW_THRESHOLD = 3;

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostReportRepository communityPostReportRepository;

    public CommunityReportService(
            CommunityPostRepository communityPostRepository,
            CommunityPostReportRepository communityPostReportRepository
    ) {
        this.communityPostRepository = communityPostRepository;
        this.communityPostReportRepository = communityPostReportRepository;
    }

    @Transactional
    public CommunityReportResponse reportPost(
            Long userId,
            Long postId,
            CommunityReportRequest request
    ) {
        CommunityPost post = communityPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        if (post.isBlinded()) {
            throw new BusinessException(ErrorCode.CONFLICT, "블라인드 처리된 게시글입니다.");
        }
        Long reportPostId = post.getPostId() == null ? postId : post.getPostId();
        if (communityPostReportRepository.existsByPostPostIdAndUserId(reportPostId, userId)) {
            throw duplicateReportException();
        }

        CommunityPostReport report;
        try {
            report = communityPostReportRepository.saveAndFlush(new CommunityPostReport(
                    post, userId, normalizeReason(request.reason())));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateReportException();
        }

        long reportCount = communityPostReportRepository.countByPostPostId(reportPostId);
        CommunityReportStatus status = reportCount > REVIEW_THRESHOLD
                ? CommunityReportStatus.BLINDED
                : CommunityReportStatus.NORMAL;
        if (status == CommunityReportStatus.BLINDED) {
            post.blind();
        }
        return new CommunityReportResponse(report.getReportId(), status);
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    private BusinessException duplicateReportException() {
        return new BusinessException(ErrorCode.CONFLICT, "이미 신고한 게시글입니다.");
    }
}
