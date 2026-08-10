package com.likelion.olion.domain.community.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PngCommunityShareImageRenderer implements CommunityShareImageRenderer {
    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1350;
    private static final int HORIZONTAL_PADDING = 120;
    private static final int MAX_THEME_IMAGE_BYTES = 10 * 1024 * 1024;

    private final Path uploadDir;
    private final Set<String> allowedThemeHosts;

    public PngCommunityShareImageRenderer(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${community.share.allowed-theme-hosts:}") String allowedThemeHosts
    ) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.allowedThemeHosts = Arrays.stream(allowedThemeHosts.split(","))
                .map(String::trim)
                .filter(host -> !host.isEmpty())
                .map(host -> host.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public byte[] render(CommunityShareRenderRequest request) {
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            configureRendering(graphics);
            Optional<BufferedImage> background = loadBackground(request.themePreviewUrl());
            if (background.isPresent()) {
                drawCoverImage(graphics, background.get());
            } else {
                drawFallbackBackground(graphics, request.shareId());
            }
            drawOverlay(graphics);
            drawThemeName(graphics, request.themeName());
            drawContent(graphics, request.content());
            drawBrand(graphics);
        } finally {
            graphics.dispose();
        }
        return encodePng(canvas);
    }

    private void configureRendering(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private Optional<BufferedImage> loadBackground(String previewUrl) {
        if (previewUrl == null || previewUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            if (previewUrl.startsWith("/images/")) {
                Path imagePath = uploadDir.resolve(previewUrl.substring("/images/".length()))
                        .normalize();
                if (!imagePath.startsWith(uploadDir)
                        || !Files.isRegularFile(imagePath)
                        || Files.size(imagePath) > MAX_THEME_IMAGE_BYTES) {
                    return Optional.empty();
                }
                return Optional.ofNullable(ImageIO.read(imagePath.toFile()));
            }

            URI uri = URI.create(previewUrl);
            if (!("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null
                    || !allowedThemeHosts.contains(uri.getHost().toLowerCase(Locale.ROOT))) {
                return Optional.empty();
            }
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            try (var inputStream = connection.getInputStream()) {
                byte[] bytes = inputStream.readNBytes(MAX_THEME_IMAGE_BYTES + 1);
                if (bytes.length > MAX_THEME_IMAGE_BYTES) {
                    return Optional.empty();
                }
                return Optional.ofNullable(ImageIO.read(new ByteArrayInputStream(bytes)));
            }
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private void drawCoverImage(Graphics2D graphics, BufferedImage background) {
        double scale = Math.max(
                (double) WIDTH / background.getWidth(),
                (double) HEIGHT / background.getHeight());
        int scaledWidth = (int) Math.ceil(background.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(background.getHeight() * scale);
        int x = (WIDTH - scaledWidth) / 2;
        int y = (HEIGHT - scaledHeight) / 2;
        graphics.drawImage(background, x, y, scaledWidth, scaledHeight, null);
    }

    private void drawFallbackBackground(Graphics2D graphics, Long shareId) {
        float hue = (shareId == null ? 0 : Math.floorMod(shareId, 360)) / 360.0f;
        Color start = Color.getHSBColor(hue, 0.42f, 0.42f);
        Color end = Color.getHSBColor((hue + 0.12f) % 1.0f, 0.55f, 0.18f);
        graphics.setPaint(new GradientPaint(0, 0, start, WIDTH, HEIGHT, end));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawOverlay(Graphics2D graphics) {
        graphics.setComposite(AlphaComposite.SrcOver.derive(0.38f));
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawThemeName(Graphics2D graphics, String themeName) {
        graphics.setColor(new Color(255, 255, 255, 190));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 30));
        String value = themeName == null || themeName.isBlank() ? "오늘의 사유" : themeName.trim();
        graphics.drawString(value, HORIZONTAL_PADDING, 190);
    }

    private void drawContent(Graphics2D graphics, String content) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 52));
        FontMetrics metrics = graphics.getFontMetrics();
        int maxWidth = WIDTH - HORIZONTAL_PADDING * 2;
        int lineHeight = 78;
        int y = 390;
        String normalized = content == null || content.isBlank()
                ? "오늘의 마음을 천천히 들여다보세요."
                : content.trim();
        for (String paragraph : normalized.split("\\R", -1)) {
            for (String line : wrapText(paragraph, metrics, maxWidth)) {
                if (y > HEIGHT - 260) {
                    graphics.drawString("…", HORIZONTAL_PADDING, y);
                    return;
                }
                graphics.drawString(line, HORIZONTAL_PADDING, y);
                y += lineHeight;
            }
            y += 22;
        }
    }

    private java.util.List<String> wrapText(
            String text,
            FontMetrics metrics,
            int maxWidth
    ) {
        if (text.isEmpty()) {
            return java.util.List.of("");
        }
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
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
        return lines;
    }

    private void drawBrand(Graphics2D graphics) {
        graphics.setColor(new Color(255, 255, 255, 210));
        graphics.setFont(new Font("SansSerif", Font.BOLD, 34));
        graphics.drawString("OLION", HORIZONTAL_PADDING, HEIGHT - 120);
    }

    private byte[] encodePng(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", outputStream)) {
                throw new IllegalStateException("PNG 인코더를 찾을 수 없습니다.");
            }
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("공유 이미지 생성에 실패했습니다.", exception);
        }
    }
}
