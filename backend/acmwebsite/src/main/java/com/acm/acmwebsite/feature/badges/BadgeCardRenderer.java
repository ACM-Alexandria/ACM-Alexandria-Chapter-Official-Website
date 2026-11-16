package com.acm.acmwebsite.feature.badges;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.seasons.PeriodType;

// Draws the 1200×627 image LinkedIn shows for a shared badge, in the badge page's design: a deep-blue card
// with the medal on the left and, on the right, the chapter, the member's name (largest), the badge, the
// period and committee, and the award date. Uses Java's built-in sans-serif font.
@Component
public class BadgeCardRenderer {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 627;

    private static final Color BACKGROUND = new Color(0x0F3A5A);
    private static final Color GOLD_TEXT = new Color(0xFFE7A3);
    private static final Color GOLD_LINE = new Color(0xF5B301);
    private static final Color LIGHT_TEXT = new Color(0xCFE6F5);
    private static final Color SOFT_TEXT = new Color(0xBFE0F5);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    // Ring, face and icon colors per period, as in the site's BadgeMedal.
    private record Palette(Color ring, Color face, Color icon) {
    }

    private static Palette palette(PeriodType type) {
        if (type == null) {
            return new Palette(new Color(0x94A3B8), new Color(0xF1F5F9), new Color(0x334155));
        }
        return switch (type) {
            case MONTH -> new Palette(new Color(0x4B98C8), new Color(0xE2F0FA), new Color(0x205E85));
            case QUARTER -> new Palette(new Color(0x7DBBE0), new Color(0x205E85), Color.WHITE);
            case SEASON -> new Palette(new Color(0xF5B301), new Color(0xFFE7A3), new Color(0x3A3012));
        };
    }

    public byte[] render(AwardView badge) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g.setColor(BACKGROUND);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setColor(new Color(255, 255, 255, 22));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Ellipse2D.Double(1110 - 210, 90 - 210, 420, 420));
            g.draw(new Ellipse2D.Double(1110 - 130, 90 - 130, 260, 260));

            drawMedal(g, badge, 230, HEIGHT / 2.0, 300);
            drawText(g, badge, 452, 668);
        } finally {
            g.dispose();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // The medal in the site's 100-unit design: ring r48, face r41, dashed circle r35, icon in a 42-unit box.
    private void drawMedal(Graphics2D g, AwardView badge, double cx, double cy, double size) {
        Palette p = palette(badge.periodType());
        double u = size / 100.0;
        g.setColor(p.ring());
        g.fill(circle(cx, cy, 48 * u));
        g.setColor(p.face());
        g.fill(circle(cx, cy, 41 * u));
        g.setColor(new Color(p.icon().getRed(), p.icon().getGreen(), p.icon().getBlue(), 72));
        g.setStroke(new BasicStroke((float) (1.2 * u), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f,
                new float[] {(float) (2 * u), (float) (3 * u)}, 0f));
        g.draw(circle(cx, cy, 35 * u));

        AffineTransform saved = g.getTransform();
        g.translate(cx - 21 * u, cy - 21 * u);
        g.scale(1.75 * u, 1.75 * u);
        g.setColor(p.icon());
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Shape s : icon(badge.icon())) {
            g.draw(s);
        }
        g.setTransform(saved);
    }

    private static Ellipse2D circle(double cx, double cy, double r) {
        return new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
    }

    // The icons in a 24-unit box, following the site's (Lucide-style) drawings.
    private static List<Shape> icon(BadgeIcon icon) {
        if (icon == BadgeIcon.CROWN) {
            Path2D crown = new Path2D.Double();
            crown.moveTo(2, 5);
            crown.lineTo(5.5, 16);
            crown.lineTo(18.5, 16);
            crown.lineTo(22, 5);
            crown.lineTo(16, 11);
            crown.lineTo(12, 4);
            crown.lineTo(8, 11);
            crown.closePath();
            return List.of(crown, new Line2D.Double(5, 20, 19, 20));
        }
        if (icon == BadgeIcon.STAR) {
            double[][] pts = {{12, 2}, {15.09, 8.26}, {22, 9.27}, {17, 14.14}, {18.18, 21.02},
                    {12, 17.77}, {5.82, 21.02}, {7, 14.14}, {2, 9.27}, {8.91, 8.26}};
            Path2D star = new Path2D.Double();
            star.moveTo(pts[0][0], pts[0][1]);
            for (int i = 1; i < pts.length; i++) {
                star.lineTo(pts[i][0], pts[i][1]);
            }
            star.closePath();
            return List.of(star);
        }
        // Trophy (also stands in for any other icon).
        Path2D cup = new Path2D.Double();
        cup.moveTo(6, 9);
        cup.lineTo(6, 2);
        cup.lineTo(18, 2);
        cup.lineTo(18, 9);
        cup.append(new Arc2D.Double(6, 3, 12, 12, 0, -180, Arc2D.OPEN), true);
        cup.closePath();

        Path2D left = new Path2D.Double();
        left.moveTo(6, 9);
        left.lineTo(4.5, 9);
        left.append(new Arc2D.Double(2, 4, 5, 5, -90, -180, Arc2D.OPEN), true);
        left.lineTo(6, 4);

        Path2D right = new Path2D.Double();
        right.moveTo(18, 9);
        right.lineTo(19.5, 9);
        right.append(new Arc2D.Double(17, 4, 5, 5, -90, 180, Arc2D.OPEN), true);
        right.lineTo(18, 4);

        Path2D leftStem = new Path2D.Double();
        leftStem.moveTo(10, 14.66);
        leftStem.lineTo(10, 17);
        leftStem.curveTo(10, 19, 7, 20, 7, 22);

        Path2D rightStem = new Path2D.Double();
        rightStem.moveTo(14, 14.66);
        rightStem.lineTo(14, 17);
        rightStem.curveTo(14, 19, 17, 20, 17, 22);

        return List.of(cup, left, right, leftStem, rightStem, new Line2D.Double(4, 22, 20, 22));
    }

    private void drawText(Graphics2D g, AwardView badge, int x, int maxWidth) {
        Font chapter = new Font(Font.SANS_SERIF, Font.BOLD, 26);
        // The name is the largest text; a long one wraps onto two lines rather than being cut.
        List<String> nameLines = List.of(badge.member().name() == null ? "" : badge.member().name());
        Font name = fitting(g, new Font(Font.SANS_SERIF, Font.BOLD, 76), nameLines, maxWidth, 56);
        if (widest(g, name, nameLines) > maxWidth) {
            nameLines = splitInTwo(nameLines.get(0));
            name = fitting(g, new Font(Font.SANS_SERIF, Font.BOLD, 64), nameLines, maxWidth, 40);
        }
        Font title = new Font(Font.SANS_SERIF, Font.BOLD, 38);
        Font detail = new Font(Font.SANS_SERIF, Font.PLAIN, 28);
        Font date = new Font(Font.SANS_SERIF, Font.PLAIN, 22);

        String period = badge.periodLabel() + " · " + badge.committeeName();
        String awarded = "Awarded " + (badge.awardedAt() != null ? badge.awardedAt().toLocalDate().format(DATE) : "");

        // Line boxes (ascent-based) and the gaps between them, centered as one block.
        FontMetrics nameMetrics = g.getFontMetrics(name);
        int nameLineGap = 6;
        int nameHeight = nameMetrics.getAscent() * nameLines.size() + (nameLines.size() - 1) * (nameMetrics.getDescent() + nameLineGap);
        int[] sizes = {
                g.getFontMetrics(chapter).getAscent(),
                nameHeight,
                g.getFontMetrics(title).getAscent(),
                g.getFontMetrics(detail).getAscent(),
                g.getFontMetrics(date).getAscent()};
        int[] gaps = {30, 22, 18, 44};
        int total = 0;
        for (int s : sizes) {
            total += s;
        }
        for (int gap : gaps) {
            total += gap;
        }
        int y = (HEIGHT - total) / 2;

        y += sizes[0];
        drawLine(g, chapter, GOLD_TEXT, "ACM Alexandria Student Chapter", x, y, maxWidth);
        y += gaps[0];
        for (int i = 0; i < nameLines.size(); i++) {
            y += nameMetrics.getAscent() + (i > 0 ? nameMetrics.getDescent() + nameLineGap : 0);
            drawLine(g, name, Color.WHITE, nameLines.get(i), x, y, maxWidth);
        }
        y += gaps[1] + sizes[2];
        drawLine(g, title, LIGHT_TEXT, badge.name(), x, y, maxWidth);
        y += gaps[2] + sizes[3];
        drawLine(g, detail, LIGHT_TEXT, period, x, y, maxWidth);
        y += gaps[3] + sizes[4];
        g.setColor(GOLD_LINE);
        g.fillRect(x, y - sizes[4] / 2 - 2, 44, 4);
        drawLine(g, date, SOFT_TEXT, awarded, x + 60, y, maxWidth - 60);
    }

    // The largest size, from `font`'s down to `min`, at which every line fits `maxWidth`.
    private static Font fitting(Graphics2D g, Font font, List<String> lines, int maxWidth, int min) {
        Font f = font;
        while (f.getSize() > min && widest(g, f, lines) > maxWidth) {
            f = f.deriveFont((float) (f.getSize() - 2));
        }
        return f;
    }

    private static int widest(Graphics2D g, Font font, List<String> lines) {
        FontMetrics m = g.getFontMetrics(font);
        return lines.stream().mapToInt(m::stringWidth).max().orElse(0);
    }

    // Splits at the space that makes the two lines most even; a single word stays on one line.
    private static List<String> splitInTwo(String text) {
        String t = text.trim();
        int best = -1;
        for (int i = t.indexOf(' '); i >= 0; i = t.indexOf(' ', i + 1)) {
            if (best < 0 || Math.abs(t.length() - 2 * i) < Math.abs(t.length() - 2 * best)) {
                best = i;
            }
        }
        return best < 0 ? List.of(t) : List.of(t.substring(0, best), t.substring(best + 1));
    }

    // One line of text, cut with an ellipsis if it still doesn't fit.
    private static void drawLine(Graphics2D g, Font font, Color color, String text, int x, int baseline, int maxWidth) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics m = g.getFontMetrics();
        String s = text == null ? "" : text;
        if (m.stringWidth(s) > maxWidth) {
            while (!s.isEmpty() && m.stringWidth(s + "…") > maxWidth) {
                s = s.substring(0, s.length() - 1);
            }
            s = s + "…";
        }
        g.drawString(s, x, baseline);
    }
}
