package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.dto.EssayDetailResponse;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class EssayPdfGenerator {
    private static final String WATERMARK_IMAGE_PATH = "images/mascot.png";
    private static final float WATERMARK_OPACITY = 0.15f;

    public byte[] generate(EssayDetailResponse detail) {
        return generate(detail, true);
    }

    public byte[] generate(EssayDetailResponse detail, boolean showWatermark) {
        try {
            BaseFont baseFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 22, Font.BOLD);
            Font chapterFont = new Font(baseFont, 16, Font.BOLD);
            Font bodyFont = new Font(baseFont, 12, Font.NORMAL);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            if (showWatermark) {
                writer.setPageEvent(new WatermarkPageEvent());
            }
            document.open();

            document.add(new Paragraph(
                    detail.title() != null ? detail.title() : "제목 없음", titleFont));
            if (detail.authorName() != null && !detail.authorName().isBlank()) {
                document.add(new Paragraph(detail.authorName(), bodyFont));
            }
            document.add(Chunk.NEWLINE);

            for (EssayDetailResponse.Chapter chapter : detail.chapters()) {
                document.add(new Paragraph(chapter.title(), chapterFont));
                document.add(Chunk.NEWLINE);
                String content = chapter.content();
                if (content == null || content.isBlank()) {
                    content = String.join("\n\n", chapter.reflections());
                }
                document.add(new Paragraph(content, bodyFont));
                document.add(Chunk.NEWLINE);
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "PDF 생성에 실패했습니다.");
        }
    }

    private static class WatermarkPageEvent extends PdfPageEventHelper {
        private Image watermark;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                watermark = Image.getInstance(new ClassPathResource(WATERMARK_IMAGE_PATH).getURL());
            } catch (IOException e) {
                throw new IllegalStateException("워터마크 이미지를 불러올 수 없습니다.", e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            float pageWidth = document.getPageSize().getWidth();
            float pageHeight = document.getPageSize().getHeight();
            float scale = Math.min(pageWidth / watermark.getWidth(), pageHeight / watermark.getHeight()) * 0.5f;
            float width = watermark.getWidth() * scale;
            float height = watermark.getHeight() * scale;

            PdfGState gState = new PdfGState();
            gState.setFillOpacity(WATERMARK_OPACITY);
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.saveState();
            canvas.setGState(gState);
            canvas.addImage(watermark, width, 0, 0, height,
                    (pageWidth - width) / 2, (pageHeight - height) / 2);
            canvas.restoreState();
        }
    }
}
