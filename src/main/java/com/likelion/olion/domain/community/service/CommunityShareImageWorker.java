package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.entity.CommunityShare;
import com.likelion.olion.domain.community.entity.CommunityShareStatus;
import com.likelion.olion.domain.community.repository.CommunityShareRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Service
public class CommunityShareImageWorker {
    private static final Logger log = Logger.getLogger(
            CommunityShareImageWorker.class.getName());

    private final CommunityShareRepository communityShareRepository;
    private final CommunityShareImageRenderer imageRenderer;
    private final CommunityShareImageStorage imageStorage;

    public CommunityShareImageWorker(
            CommunityShareRepository communityShareRepository,
            CommunityShareImageRenderer imageRenderer,
            CommunityShareImageStorage imageStorage
    ) {
        this.communityShareRepository = communityShareRepository;
        this.imageRenderer = imageRenderer;
        this.imageStorage = imageStorage;
    }

    @Transactional
    public void process(Long shareId) {
        CommunityShare share = communityShareRepository.findByIdForUpdate(shareId).orElse(null);
        if (share == null || share.getStatus() != CommunityShareStatus.QUEUED) {
            return;
        }

        share.startProcessing();
        try {
            byte[] image = imageRenderer.render(new CommunityShareRenderRequest(
                    share.getShareId(),
                    share.getPost().getContent(),
                    share.getTheme().getName(),
                    share.getTheme().getPreviewUrl()));
            String imageUrl = imageStorage.store(share.getShareId(), image);
            share.complete(imageUrl);
        } catch (RuntimeException exception) {
            share.requeue();
            log.warning("Community share image generation failed for shareId="
                    + shareId + ": " + exception.getMessage());
        }
    }
}
