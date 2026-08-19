package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.essay.dto.EssayCreateRequest;
import com.likelion.olion.domain.essay.dto.EssayCreateResponse;
import com.likelion.olion.domain.essay.dto.EssayDetailResponse;
import com.likelion.olion.domain.essay.dto.EssayDraftResponse;
import com.likelion.olion.domain.essay.dto.EssayJobStatusResponse;
import com.likelion.olion.domain.essay.dto.EssayListResponse;
import com.likelion.olion.domain.essay.dto.EssayPublishRequest;
import com.likelion.olion.domain.essay.dto.EssayPublishResponse;
import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayChapter;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import com.likelion.olion.domain.essay.event.EssayGenerationRequestedEvent;
import com.likelion.olion.domain.essay.repository.EssayChapterRepository;
import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import com.likelion.olion.domain.user.entity.SubscriptionPlan;
import com.likelion.olion.domain.user.entity.User;
import com.likelion.olion.domain.user.repository.UserRepository;
import com.likelion.olion.global.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EssayServiceTest {
    @Mock
    private EssayRepository essayRepository;
    @Mock
    private EssayChapterRepository essayChapterRepository;
    @Mock
    private ReflectionRepository reflectionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EssayPdfGenerator essayPdfGenerator;
    @Mock
    private UserRepository userRepository;

    private EssayService service;

    @BeforeEach
    void setUp() {
        service = new EssayService(
                essayRepository, essayChapterRepository, reflectionRepository, eventPublisher,
                essayPdfGenerator, userRepository);
    }

    @Test
    void createsEssayAndPublishesEvent() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 30).boxed().toList();
        List<Reflection> reflections = ids.stream()
                .map(id -> new Reflection(1L, mockSession(), "사유 " + id))
                .toList();
        Essay saved = new Essay(1L);
        ReflectionTestUtils.setField(saved, "essayId", 7L);

        given(reflectionRepository.findByReflectionIdInAndUserId(ids, 1L)).willReturn(reflections);
        given(userRepository.findById(1L)).willReturn(Optional.of(new User("reader@example.com", "encoded", "책읽는사자")));
        given(essayRepository.saveAndFlush(any(Essay.class))).willReturn(saved);

        EssayCreateResponse response = service.create(1L, new EssayCreateRequest(ids));

        assertThat(response.essayId()).isEqualTo(7L);
        assertThat(response.jobStatus()).isEqualTo(EssayStatus.QUEUED);
        verify(essayRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
                essay -> "책읽는사자".equals(essay.getAuthorName())));
        reflections.forEach(reflection -> assertThat(reflection.getEssay()).isEqualTo(saved));
        verify(eventPublisher).publishEvent(new EssayGenerationRequestedEvent(7L));
    }

    @Test
    void rejectsWhenSomeReflectionsNotOwnedOrMissing() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 30).boxed().toList();
        given(reflectionRepository.findByReflectionIdInAndUserId(ids, 1L))
                .willReturn(List.of(new Reflection(1L, mockSession(), "사유 1")));

        assertThatThrownBy(() -> service.create(1L, new EssayCreateRequest(ids)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsWhenReflectionCountIsNotExactlyThirty() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 31).boxed().toList();

        assertThatThrownBy(() -> service.create(1L, new EssayCreateRequest(ids)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsWhenReflectionIdsContainDuplicates() {
        List<Long> ids = new java.util.ArrayList<>(
                java.util.stream.LongStream.rangeClosed(1, 29).boxed().toList());
        ids.add(29L);

        assertThatThrownBy(() -> service.create(1L, new EssayCreateRequest(ids)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void returnsEssayListForUserOrderedByCreatedAtDesc() {
        Essay essay = new Essay(1L);
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        given(essayRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(essay));

        EssayListResponse response = service.getList(1L);

        assertThat(response.essays()).hasSize(1);
        assertThat(response.essays().get(0).essayId()).isEqualTo(7L);
        assertThat(response.essays().get(0).status()).isEqualTo(EssayStatus.QUEUED);
    }

    @Test
    void returnsJobStatusForOwnedEssay() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        EssayJobStatusResponse response = service.getJobStatus(1L, 7L);

        assertThat(response.status()).isEqualTo(EssayStatus.PROCESSING);
    }

    @Test
    void rejectsJobStatusWhenEssayNotOwnedOrMissing() {
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getJobStatus(1L, 7L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void retriesFailedEssayAndPublishesEvent() {
        Essay essay = new Essay(1L);
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        essay.startProcessing();
        essay.fail();
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        EssayJobStatusResponse response = service.retry(1L, 7L);

        assertThat(response.status()).isEqualTo(EssayStatus.QUEUED);
        verify(eventPublisher).publishEvent(new EssayGenerationRequestedEvent(7L));
    }

    @Test
    void rejectsRetryWhenEssayNotFailed() {
        Essay essay = new Essay(1L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        assertThatThrownBy(() -> service.retry(1L, 7L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void returnsDraftForCompletedEssay() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        EssayChapter chapter = new EssayChapter(essay, 1, "1장", "본문");
        ReflectionTestUtils.setField(chapter, "chapterId", 100L);
        Reflection reflection = new Reflection(1L, mockSession(), "사유 1");
        ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
        reflection.assignToChapter(chapter);

        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));
        given(essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(7L)).willReturn(List.of(chapter));
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of(reflection));

        EssayDraftResponse response = service.getDraft(1L, 7L);

        assertThat(response.chapters()).hasSize(1);
        assertThat(response.chapters().get(0).title()).isEqualTo("1장");
        assertThat(chapter.getContent()).isEqualTo("본문");
        assertThat(response.chapters().get(0).reflectionIds()).containsExactly(88L);
    }

    @Test
    void rejectsDraftWhenEssayNotCompleted() {
        Essay essay = new Essay(1L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        assertThatThrownBy(() -> service.getDraft(1L, 7L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void returnsDetailForCompletedEssay() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        EssayChapter chapter = new EssayChapter(essay, 1, "1장", "본문");
        ReflectionTestUtils.setField(chapter, "chapterId", 100L);
        Reflection reflection = new Reflection(1L, mockSession(), "사유 1");
        ReflectionTestUtils.setField(reflection, "reflectionId", 88L);
        reflection.assignToChapter(chapter);

        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));
        given(essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(7L)).willReturn(List.of(chapter));
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of(reflection));

        EssayDetailResponse response = service.getDetail(1L, 7L);

        assertThat(response.essayId()).isEqualTo(7L);
        assertThat(response.chapters()).hasSize(1);
        assertThat(response.chapters().get(0).title()).isEqualTo("1장");
        assertThat(response.chapters().get(0).reflections()).containsExactly("사유 1");
    }

    @Test
    void downloadsPdfForCompletedEssay() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        byte[] pdfBytes = new byte[]{1, 2, 3};

        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));
        given(essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(7L)).willReturn(List.of());
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of());
        given(essayPdfGenerator.generate(any(EssayDetailResponse.class), anyBoolean())).willReturn(pdfBytes);

        byte[] result = service.downloadPdf(1L, 7L);

        assertThat(result).isEqualTo(pdfBytes);
        verify(essayPdfGenerator).generate(any(EssayDetailResponse.class), eq(true));
    }

    @Test
    void keepsWatermarkForNonProPlanEvenIfRemovalRequested() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        User user = User.builder().email("basic@test.com").password("encoded").nickname("닉네임").build();

        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));
        given(essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(7L)).willReturn(List.of());
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(essayPdfGenerator.generate(any(EssayDetailResponse.class), anyBoolean())).willReturn(new byte[0]);

        service.downloadPdf(1L, 7L, true);

        verify(essayPdfGenerator).generate(any(EssayDetailResponse.class), eq(true));
    }

    @Test
    void removesWatermarkForProPlanWhenRequested() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        User user = User.builder().email("pro@test.com").password("encoded").nickname("닉네임").build();
        user.changePlan(SubscriptionPlan.PRO);

        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));
        given(essayChapterRepository.findByEssay_EssayIdOrderByChapterNo(7L)).willReturn(List.of());
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(essayPdfGenerator.generate(any(EssayDetailResponse.class), anyBoolean())).willReturn(new byte[0]);

        service.downloadPdf(1L, 7L, true);

        verify(essayPdfGenerator).generate(any(EssayDetailResponse.class), eq(false));
    }

    @Test
    void rejectsDownloadWhenEssayNotCompleted() {
        Essay essay = new Essay(1L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        assertThatThrownBy(() -> service.downloadPdf(1L, 7L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsDetailWhenEssayNotCompleted() {
        Essay essay = new Essay(1L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        assertThatThrownBy(() -> service.getDetail(1L, 7L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void publishesCompletedEssayWithTitle() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        ReflectionTestUtils.setField(essay, "essayId", 7L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        EssayPublishResponse response = service.publish(1L, 7L, new EssayPublishRequest("흔들려도 걷는 마음"));

        assertThat(response.essayId()).isEqualTo(7L);
        assertThat(response.title()).isEqualTo("흔들려도 걷는 마음");
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    void rejectsPublishWhenEssayNotCompleted() {
        Essay essay = new Essay(1L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        assertThatThrownBy(() -> service.publish(1L, 7L, new EssayPublishRequest("제목")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelsQueuedEssay() {
        Essay essay = new Essay(1L);
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        EssayJobStatusResponse response = service.cancel(1L, 7L);

        assertThat(response.status()).isEqualTo(EssayStatus.CANCELED);
    }

    @Test
    void rejectsCancelingCompletedEssay() {
        Essay essay = new Essay(1L);
        essay.startProcessing();
        essay.complete();
        given(essayRepository.findByEssayIdAndUserId(7L, 1L)).willReturn(Optional.of(essay));

        assertThatThrownBy(() -> service.cancel(1L, 7L))
                .isInstanceOf(BusinessException.class);
    }

    private ReadingSession mockSession() {
        return new ReadingSession(1L, mock(UserBook.class), 30);
    }
}
