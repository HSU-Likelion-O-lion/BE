package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.entity.Essay;
import com.likelion.olion.domain.essay.entity.EssayChapter;
import com.likelion.olion.domain.essay.entity.EssayStatus;
import com.likelion.olion.domain.essay.repository.EssayChapterRepository;
import com.likelion.olion.domain.essay.repository.EssayRepository;
import com.likelion.olion.domain.reflection.entity.Reflection;
import com.likelion.olion.domain.reflection.repository.ReflectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
public class EssayGenerationWorker {
    private static final Logger log = Logger.getLogger(EssayGenerationWorker.class.getName());

    private final EssayRepository essayRepository;
    private final EssayChapterRepository essayChapterRepository;
    private final ReflectionRepository reflectionRepository;
    private final EssayEditor essayEditor;
    private final AiEssayEditor aiEssayEditor;

    @Autowired
    public EssayGenerationWorker(
            EssayRepository essayRepository,
            EssayChapterRepository essayChapterRepository,
            ReflectionRepository reflectionRepository,
            EssayEditor essayEditor
    ) {
        this(essayRepository, essayChapterRepository, reflectionRepository, essayEditor, null);
    }

    public EssayGenerationWorker(
            EssayRepository essayRepository,
            EssayChapterRepository essayChapterRepository,
            ReflectionRepository reflectionRepository,
            EssayEditor essayEditor,
            AiEssayEditor aiEssayEditor
    ) {
        this.essayRepository = essayRepository;
        this.essayChapterRepository = essayChapterRepository;
        this.reflectionRepository = reflectionRepository;
        this.essayEditor = essayEditor;
        this.aiEssayEditor = aiEssayEditor;
    }

    @Transactional
    public void process(Long essayId) {
        Essay essay = essayRepository.findByIdForUpdate(essayId).orElse(null);
        if (essay == null || essay.getStatus() != EssayStatus.QUEUED) {
            return;
        }

        essay.startProcessing();
        try {
            List<Reflection> reflections = reflectionRepository.findByEssay_EssayId(essayId);
            List<EssayEditor.ChapterDraft> chapters = aiEssayEditor == null
                    ? essayEditor.organize(reflections)
                    : aiEssayEditor.organize(essay.getUserId(), reflections, essayEditor);

            if (essay.getStatus() == EssayStatus.CANCELED) {
                return;
            }

            int chapterNo = 1;
            for (EssayEditor.ChapterDraft draft : chapters) {
                EssayChapter chapter = essayChapterRepository.save(new EssayChapter(essay, chapterNo, draft.title()));
                for (Reflection reflection : draft.reflections()) {
                    reflection.assignToChapter(chapter);
                }
                chapterNo++;
            }
            essay.complete();
        } catch (RuntimeException exception) {
            if (essay.getStatus() != EssayStatus.CANCELED) {
                essay.fail();
            }
            log.warning("Essay generation failed for essayId=" + essayId + ": " + exception.getMessage());
        }
    }
}
