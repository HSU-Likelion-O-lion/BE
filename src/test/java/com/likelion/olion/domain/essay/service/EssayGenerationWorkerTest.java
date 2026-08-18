package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.bookshelf.entity.UserBook;
import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayChapter;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import com.likelion.olion.domain.essay.repository.EssayChapterRepository;
import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.reading.entity.ReadingSession;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EssayGenerationWorkerTest {
    @Mock
    private EssayRepository essayRepository;
    @Mock
    private EssayChapterRepository essayChapterRepository;
    @Mock
    private ReflectionRepository reflectionRepository;
    @Mock
    private EssayEditor essayEditor;
    @Mock
    private AiEssayEditor aiEssayEditor;

    private EssayGenerationWorker worker;
    private Essay essay;

    @BeforeEach
    void setUp() {
        worker = new EssayGenerationWorker(essayRepository, essayChapterRepository, reflectionRepository, essayEditor);
        essay = new Essay(1L);
        ReflectionTestUtils.setField(essay, "essayId", 7L);
    }

    @Test
    void organizesAndCompletesQueuedEssay() {
        Reflection reflection = new Reflection(1L, mockSession(), "오늘 읽은 부분에서...");
        given(essayRepository.findByIdForUpdate(7L)).willReturn(Optional.of(essay));
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of(reflection));
        given(essayEditor.organize(List.of(reflection)))
                .willReturn(List.of(new EssayEditor.ChapterDraft("1장", List.of(reflection))));
        given(essayChapterRepository.save(any(EssayChapter.class))).willAnswer(invocation -> invocation.getArgument(0));

        worker.process(7L);

        assertThat(essay.getStatus()).isEqualTo(EssayStatus.COMPLETED);
        assertThat(reflection.getChapter()).isNotNull();
    }

    @Test
    void skipsEssayThatIsNotQueued() {
        essay.startProcessing();
        given(essayRepository.findByIdForUpdate(7L)).willReturn(Optional.of(essay));

        worker.process(7L);

        verify(reflectionRepository, never()).findByEssay_EssayId(any());
    }

    @Test
    void skipsCanceledEssay() {
        essay.cancel();
        given(essayRepository.findByIdForUpdate(7L)).willReturn(Optional.of(essay));

        worker.process(7L);

        assertThat(essay.getStatus()).isEqualTo(EssayStatus.CANCELED);
        verify(reflectionRepository, never()).findByEssay_EssayId(any());
    }

    @Test
    void failsEssayWhenOrganizationThrows() {
        given(essayRepository.findByIdForUpdate(7L)).willReturn(Optional.of(essay));
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of());
        given(essayEditor.organize(List.of())).willThrow(new IllegalStateException("boom"));

        worker.process(7L);

        assertThat(essay.getStatus()).isEqualTo(EssayStatus.FAILED);
    }

    @Test
    void savesGeneratedTitleAndChapterContent() {
        Reflection reflection = new Reflection(1L, mockSession(), "원문 사유");
        given(essayRepository.findByIdForUpdate(7L)).willReturn(Optional.of(essay));
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of(reflection));
        EssayEditor.ChapterDraft chapter = new EssayEditor.ChapterDraft(
                "시작", "AI가 작성한 본문", List.of(reflection));
        given(aiEssayEditor.generate(1L, List.of(reflection)))
                .willReturn(new EssayEditor.EssayDraft("생성된 제목", List.of(chapter)));
        given(essayChapterRepository.save(any(EssayChapter.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        new EssayGenerationWorker(
                essayRepository, essayChapterRepository, reflectionRepository, essayEditor, aiEssayEditor)
                .process(7L);

        assertThat(essay.getStatus()).isEqualTo(EssayStatus.COMPLETED);
        assertThat(essay.getTitle()).isEqualTo("생성된 제목");
        org.mockito.ArgumentCaptor<EssayChapter> captor =
                org.mockito.ArgumentCaptor.forClass(EssayChapter.class);
        verify(essayChapterRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("AI가 작성한 본문");
    }

    @Test
    void marksEssayFailedWhenAiGenerationFails() {
        given(essayRepository.findByIdForUpdate(7L)).willReturn(Optional.of(essay));
        given(reflectionRepository.findByEssay_EssayId(7L)).willReturn(List.of());
        given(aiEssayEditor.generate(1L, List.of()))
                .willThrow(new AiEssayEditor.EssayGenerationException("invalid response"));

        new EssayGenerationWorker(
                essayRepository, essayChapterRepository, reflectionRepository, essayEditor, aiEssayEditor)
                .process(7L);

        assertThat(essay.getStatus()).isEqualTo(EssayStatus.FAILED);
    }

    private ReadingSession mockSession() {
        return new ReadingSession(1L, mock(UserBook.class), 30);
    }
}
