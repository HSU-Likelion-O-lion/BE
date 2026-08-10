package com.likelion.olion.domain.community.service;

import com.likelion.olion.domain.community.dto.CommunityShareThemeResponse;
import com.likelion.olion.domain.community.entity.CommunityShareTheme;
import com.likelion.olion.domain.community.repository.CommunityShareThemeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommunityShareThemeServiceTest {
    @Mock
    private CommunityShareThemeRepository communityShareThemeRepository;

    @Test
    void returnsThemesInRepositoryOrder() {
        CommunityShareThemeService service = new CommunityShareThemeService(
                communityShareThemeRepository);
        CommunityShareTheme nightSky = new CommunityShareTheme(
                "밤하늘", "https://cdn.olion.com/theme/2.png");
        ReflectionTestUtils.setField(nightSky, "themeId", 2L);
        given(communityShareThemeRepository.findAllByOrderByThemeIdAsc())
                .willReturn(List.of(nightSky));

        CommunityShareThemeResponse response = service.getThemes();

        assertThat(response.themes()).hasSize(1);
        assertThat(response.themes().get(0).themeId()).isEqualTo(2L);
        assertThat(response.themes().get(0).name()).isEqualTo("밤하늘");
        assertThat(response.themes().get(0).previewUrl())
                .isEqualTo("https://cdn.olion.com/theme/2.png");
    }

    @Test
    void returnsEmptyThemeList() {
        CommunityShareThemeService service = new CommunityShareThemeService(
                communityShareThemeRepository);
        given(communityShareThemeRepository.findAllByOrderByThemeIdAsc())
                .willReturn(List.of());

        CommunityShareThemeResponse response = service.getThemes();

        assertThat(response.themes()).isEmpty();
    }
}
