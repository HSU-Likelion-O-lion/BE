package com.likelion.olion.domain.essay.service;

import com.likelion.olion.domain.essay.dto.EssayDetailResponse;
import com.likelion.olion.global.common.exception.BusinessException;
import com.likelion.olion.global.common.exception.ErrorCode;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class EssayPdfGenerator {
    public byte[] generate(EssayDetailResponse detail) {
        try {
            BaseFont baseFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 22, Font.BOLD);
            Font chapterFont = new Font(baseFont, 16, Font.BOLD);
            Font bodyFont = new Font(baseFont, 12, Font.NORMAL);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
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
                for (String content : chapter.reflections()) {
                    document.add(new Paragraph(content, bodyFont));
                    document.add(Chunk.NEWLINE);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "PDF 생성에 실패했습니다.");
        }
    }
}
