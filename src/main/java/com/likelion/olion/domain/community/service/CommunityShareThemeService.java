package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityShareThemeResponse;
import com.likelion.olion.domain.community.repository.CommunityShareThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityShareThemeService {
    private final CommunityShareThemeRepository communityShareThemeRepository;

    public CommunityShareThemeService(CommunityShareThemeRepository communityShareThemeRepository) {
        this.communityShareThemeRepository = communityShareThemeRepository;
    }

    public CommunityShareThemeResponse getThemes() {
        return new CommunityShareThemeResponse(communityShareThemeRepository
                .findAllByOrderByThemeIdAsc().stream()
                .map(theme -> new CommunityShareThemeResponse.Theme(
                        theme.getThemeId(), theme.getName(), theme.getPreviewUrl()))
                .toList());
    }
}
