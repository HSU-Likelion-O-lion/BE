package com.likelion.olion.domain.reflectionshare.service;

import com.likelion.olion.domain.reflectionshare.entity.ReflectionShareTheme;
import com.likelion.olion.domain.user.service.FileStorageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PngReflectionShareImageRenderer implements ReflectionShareImageRenderer {
    static final int WIDTH = 1080;
    static final int HEIGHT = 1350;

    private static final int CARD_X = 90;
    private static final int CARD_Y = 105;
    private static final int CARD_WIDTH = 900;
    private static final int CARD_HEIGHT = 1135;
    private static final int CONTENT_X = 155;
    private static final int CONTENT_WIDTH = 770;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy.MM.dd")
            .withZone(ZoneId.of("Asia/Seoul"));

    private final ReflectionShareObjectStorage objectStorage;
    private final FileStorageService fileStorageService;

    public PngReflectionShareImageRenderer(
            ReflectionShareObjectStorage objectStorage,
            FileStorageService fileStorageService
    ) {
        this.objectStorage = objectStorage;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public byte[] render(ReflectionShareRenderRequest request) {
        ReflectionShareTheme theme = ReflectionShareTheme.findById(request.themeId())
                .orElse(ReflectionShareTheme.BLUE);
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            configureRendering(graphics);
            drawBackground(graphics, theme);
            drawCard(graphics, theme);
            drawTape(graphics, theme);
            drawProfile(graphics, request.profileImageUrl());
            drawCenteredText(graphics, displayName(request.nickname()), 458,
                    font(Font.BOLD, 48), new Color(22, 23, 31));
            drawCenteredText(graphics, formatDate(request), 520,
                    font(Font.PLAIN, 34), new Color(134, 138, 160));
            drawContent(graphics, request.content());
        } finally {
            graphics.dispose();
        }
        return encodePng(canvas);
    }

    private void configureRendering(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void drawBackground(Graphics2D graphics, ReflectionShareTheme theme) {
        Optional<BufferedImage> background = objectStorage.loadTheme(theme.themeId())
                .flatMap(this::decodeImage);
        if (background.isPresent()) {
            drawCoverImage(graphics, background.get());
            return;
        }

        graphics.setColor(swatchColor(theme));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setComposite(AlphaComposite.SrcOver.derive(0.18f));
        graphics.setColor(new Color(93, 107, 196));
        graphics.setStroke(new BasicStroke(2f));
        for (int x = 0; x < WIDTH; x += 60) {
            graphics.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += 60) {
            graphics.drawLine(0, y, WIDTH, y);
        }
        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawCoverImage(Graphics2D graphics, BufferedImage background) {
        double scale = Math.max((double) WIDTH / background.getWidth(),
                (double) HEIGHT / background.getHeight());
        int scaledWidth = (int) Math.ceil(background.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(background.getHeight() * scale);
        graphics.drawImage(background,
                (WIDTH - scaledWidth) / 2,
                (HEIGHT - scaledHeight) / 2,
                scaledWidth,
                scaledHeight,
                null);
    }

    private void drawCard(Graphics2D graphics, ReflectionShareTheme theme) {
        graphics.setComposite(AlphaComposite.SrcOver.derive(0.88f));
        graphics.setColor(cardColor(theme));
        graphics.fill(new RoundRectangle2D.Double(
                CARD_X, CARD_Y, CARD_WIDTH, CARD_HEIGHT, 20, 20));
        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawTape(Graphics2D graphics, ReflectionShareTheme theme) {
        graphics.setComposite(AlphaComposite.SrcOver.derive(0.72f));
        graphics.setColor(tapeColor(theme));
        graphics.rotate(Math.toRadians(4), WIDTH / 2.0, 105);
        graphics.fill(new RoundRectangle2D.Double(WIDTH / 2.0 - 115, 62, 230, 92, 7, 7));
        graphics.rotate(Math.toRadians(-4), WIDTH / 2.0, 105);
        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawProfile(Graphics2D graphics, String profileImageUrl) {
        int size = 178;
        int x = (WIDTH - size) / 2;
        int y = 205;
        BufferedImage profile = loadProfile(profileImageUrl).orElseGet(this::loadDefaultProfile);

        Shape previousClip = graphics.getClip();
        graphics.setClip(new Ellipse2D.Double(x, y, size, size));
        drawCoverImageInBounds(graphics, profile, x, y, size, size);
        graphics.setClip(previousClip);
        graphics.setColor(new Color(253, 253, 255));
        graphics.setStroke(new BasicStroke(5f));
        graphics.draw(new Ellipse2D.Double(x, y, size, size));
    }

    private void drawCoverImageInBounds(
            Graphics2D graphics,
            BufferedImage image,
            int x,
            int y,
            int width,
            int height
    ) {
        double scale = Math.max((double) width / image.getWidth(),
                (double) height / image.getHeight());
        int scaledWidth = (int) Math.ceil(image.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(image.getHeight() * scale);
        graphics.drawImage(image,
                x + (width - scaledWidth) / 2,
                y + (height - scaledHeight) / 2,
                scaledWidth,
                scaledHeight,
                null);
    }

    private void drawCenteredText(
            Graphics2D graphics,
            String text,
            int baselineY,
            Font font,
            Color color
    ) {
        graphics.setFont(font);
        graphics.setColor(color);
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, (WIDTH - metrics.stringWidth(text)) / 2, baselineY);
    }

    private void drawContent(Graphics2D graphics, String content) {
        int fontSize = 38;
        List<String> lines;
        Font bodyFont;
        do {
            bodyFont = font(Font.PLAIN, fontSize);
            graphics.setFont(bodyFont);
            lines = wrapText(normalizeContent(content), graphics.getFontMetrics(), CONTENT_WIDTH);
            fontSize -= 2;
        } while (lines.size() > 11 && fontSize >= 30);

        graphics.setFont(bodyFont);
        graphics.setColor(new Color(38, 40, 56));
        int lineHeight = Math.round(bodyFont.getSize2D() * 1.65f);
        int y = 625;
        for (int index = 0; index < lines.size(); index++) {
            if (y > CARD_Y + CARD_HEIGHT - 65) {
                graphics.drawString("…", CONTENT_X, y - lineHeight);
                break;
            }
            graphics.drawString(lines.get(index), CONTENT_X, y);
            y += lineHeight;
        }
    }

    private List<String> wrapText(String content, FontMetrics metrics, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : content.split("\\R", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder line = new StringBuilder();
            for (int offset = 0; offset < paragraph.length();) {
                int codePoint = paragraph.codePointAt(offset);
                String character = new String(Character.toChars(codePoint));
                if (!line.isEmpty() && metrics.stringWidth(line + character) > maxWidth) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                line.append(character);
                offset += Character.charCount(codePoint);
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    private Optional<BufferedImage> loadProfile(String profileImageUrl) {
        return fileStorageService.loadProfileImage(profileImageUrl)
                .flatMap(this::decodeImage);
    }

    private BufferedImage loadDefaultProfile() {
        try {
            BufferedImage image = ImageIO.read(new ClassPathResource("images/mascot.png").getInputStream());
            if (image != null) {
                return image;
            }
        } catch (IOException ignored) {
        }
        BufferedImage fallback = new BufferedImage(180, 180, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = fallback.createGraphics();
        graphics.setColor(new Color(253, 253, 255));
        graphics.fillRect(0, 0, 180, 180);
        graphics.setColor(new Color(93, 107, 196));
        graphics.fillOval(48, 48, 84, 84);
        graphics.dispose();
        return fallback;
    }

    private Optional<BufferedImage> decodeImage(byte[] bytes) {
        try {
            return Optional.ofNullable(ImageIO.read(new ByteArrayInputStream(bytes)));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private byte[] encodePng(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw new IllegalStateException("PNG 인코더를 찾을 수 없습니다.");
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("사유록 공유 이미지 생성에 실패했습니다.", exception);
        }
    }

    private Font font(int style, int size) {
        for (String family : List.of("Pretendard", "Noto Sans CJK KR", "Apple SD Gothic Neo")) {
            Font candidate = new Font(family, style, size);
            if (!"Dialog".equals(candidate.getFamily())) {
                return candidate;
            }
        }
        return new Font("SansSerif", style, size);
    }

    private String displayName(String nickname) {
        return nickname == null || nickname.isBlank() ? "나" : nickname.trim();
    }

    private String formatDate(ReflectionShareRenderRequest request) {
        return request.reflectionCreatedAt() == null
                ? DATE_FORMATTER.format(java.time.Instant.now())
                : DATE_FORMATTER.format(request.reflectionCreatedAt());
    }

    private String normalizeContent(String content) {
        return content == null || content.isBlank()
                ? "오늘의 마음을 천천히 들여다보세요."
                : content.trim();
    }

    private Color swatchColor(ReflectionShareTheme theme) {
        return switch (theme) {
            case PINK -> new Color(245, 154, 202);
            case BLUE -> new Color(173, 185, 242);
            case GREEN -> new Color(147, 228, 103);
            case YELLOW -> new Color(246, 227, 106);
        };
    }

    private Color cardColor(ReflectionShareTheme theme) {
        return switch (theme) {
            case PINK -> new Color(255, 203, 231);
            case BLUE -> new Color(225, 231, 255);
            case GREEN -> new Color(190, 246, 160);
            case YELLOW -> new Color(255, 242, 156);
        };
    }

    private Color tapeColor(ReflectionShareTheme theme) {
        return switch (theme) {
            case PINK -> new Color(225, 112, 173);
            case BLUE -> new Color(142, 158, 222);
            case GREEN -> new Color(111, 190, 76);
            case YELLOW -> new Color(218, 191, 52);
        };
    }
}
