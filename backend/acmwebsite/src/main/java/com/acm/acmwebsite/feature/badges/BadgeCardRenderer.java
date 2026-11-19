package com.acm.acmwebsite.feature.badges;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
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
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.seasons.PeriodType;

// Draws the 1200×627 image LinkedIn shows for a shared badge, in the badge page's design: a deep-blue card
// with a web of nodes and lines, the medal on the left and, on the right, the ACM logo and chapter name, the
// member's name (largest), the badge, the period and committee, and the award date. Uses Java's built-in
// sans-serif font.
@Component
public class BadgeCardRenderer {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 627;

    private static final Color GOLD_TEXT = new Color(0xFFE7A3);
    private static final Color GOLD_LINE = new Color(0xF5B301);
    private static final Color LIGHT_TEXT = new Color(0xCFE6F5);
    private static final Color SOFT_TEXT = new Color(0xBFE0F5);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    // The medal's colors per period, as in the site's BadgeMedal (with its light-on-dark wreath): the face
    // gradient (top, middle, bottom), the rim, the ink for the icon and text, and the two leaf colors.
    private record Palette(Color a, Color b, Color c, Color rim1, Color rim2, Color ink, Color leafOuter, Color leafInner) {
    }

    private static Palette palette(PeriodType type) {
        if (type == null) {
            return new Palette(c(0xCBD5E1), c(0x94A3B8), c(0x475569), c(0xF8FAFC), c(0xCBD5E1), Color.WHITE, c(0xCBD5E1), c(0x94A3B8));
        }
        return switch (type) {
            case MONTH -> new Palette(c(0x8FD0F5), c(0x3D93CF), c(0x1E5F8E), c(0xE9F6FF), c(0x7FB6DB), Color.WHITE, c(0x8FD0F5), c(0x5BAEE3));
            case QUARTER -> new Palette(c(0x6E8BF2), c(0x3651C9), c(0x1D2C7A), c(0xEEF1FF), c(0x97A6E6), Color.WHITE, c(0x9DB0FA), c(0x6E8BF2));
            case SEASON -> new Palette(c(0xFFE27A), c(0xF5B301), c(0xB26F00), c(0xFFF7D6), c(0xE9B530), c(0x5A3A00), c(0xFFE27A), c(0xF5B301));
        };
    }

    private static Color c(int rgb) {
        return new Color(rgb);
    }

    // The ACM ribbon's two panels on a dark background.
    private static final Color RIBBON_DEEP = new Color(0x4F9FD3);
    private static final Color RIBBON_LIGHT = new Color(0xA9D5F2);

    // The medal's geometry is drawn in the site's 120-unit box (the ribbon at the top, the wreath, the medal).
    private static final double MEDAL_CX = 60;
    private static final double MEDAL_CY = 72;
    private static final double WREATH_RADIUS = 38;

    // The ACM logo on its own, with a white outline and no circle, before the chapter
    // name. Loaded once; the chapter name is still drawn (without the logo) if the file is missing.
    private static final int LOGO_ROW = 66;
    private final BufferedImage logo = loadLogo();

    private static BufferedImage loadLogo() {
        try (java.io.InputStream in = BadgeCardRenderer.class.getResourceAsStream("/badge/acm-logo-outline.png")) {
            if (in == null) {
                return null;
            }
            BufferedImage source = ImageIO.read(in);
            double scale = (double) LOGO_ROW / source.getHeight();
            int w = Math.max(1, (int) Math.round(source.getWidth() * scale));
            BufferedImage scaled = new BufferedImage(w, LOGO_ROW, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = scaled.createGraphics();
            sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            sg.drawImage(source.getScaledInstance(w, LOGO_ROW, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
            sg.dispose();
            return scaled;
        } catch (IOException e) {
            return null;
        }
    }

    // The logo with its top left corner at (x, y); its width, or 0 if the file is missing.
    private int drawLogo(Graphics2D g, int x, int y) {
        if (logo == null) {
            return 0;
        }
        g.drawImage(logo, x, y, null);
        return logo.getWidth();
    }

    // The background: a deep blue gradient, two soft blue glows, and a web of nodes and fine lines. The nodes
    // are {x, y, radius}; the page's banner uses the same list (frontend network.js, WIDE_NODES), so the image
    // and the page match. Two nodes closer than 210 px are joined by a line that fades with distance.
    private static final double[][] NODES = {
            {518.7, 280.3, 3.0}, {458.9, 298.9, 5.1}, {1044.1, 216.8, 3.5}, {931.6, 118.4, 3.2}, {735.9, 213.2, 2.3},
            {1099.3, 129.4, 3.9}, {988.0, 32.3, 3.7}, {277.2, 344.6, 3.6}, {117.4, 419.3, 4.8}, {1078.0, 406.8, 4.1},
            {220.2, 208.6, 3.3}, {373.8, 398.5, 3.5}, {466.4, 581.4, 4.0}, {904.0, 197.1, 5.0}, {1105.7, 498.3, 4.2},
            {153.3, 295.6, 2.2}, {121.8, 454.1, 3.4}, {942.5, 175.5, 2.8}, {790.6, 527.8, 2.1}, {29.6, 76.1, 2.7},
            {722.4, 471.4, 3.0}, {1164.9, 134.4, 5.2}, {1165.3, 160.2, 2.9}, {11.8, 54.2, 4.7}, {985.6, 452.6, 3.4},
            {379.5, 140.7, 3.6}, {943.5, 156.5, 3.3}, {194.2, 349.8, 3.4}, {63.5, 349.3, 5.2}, {431.7, 260.4, 4.6},
            {403.7, 123.5, 4.4}, {1002.1, 361.9, 5.3}, {554.0, 31.0, 4.4}, {3.8, 426.3, 2.1}};

    private static void drawBackground(Graphics2D g) {
        // CSS linear-gradient(135deg, #0B2E49, #0F3A5A 55%, #15507A) over the whole image.
        g.setPaint(new LinearGradientPaint(143.3f, -143.2f, 1056.7f, 770.2f, new float[] {0f, .55f, 1f},
                new Color[] {new Color(0x0B2E49), new Color(0x0F3A5A), new Color(0x15507A)}));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        glow(g, 980, 90, 320, new Color(0x5F, 0xA8, 0xD8, 127));
        glow(g, 150, 560, 260, new Color(0x2F, 0x86, 0xC4, 115));

        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < NODES.length; i++) {
            for (int j = i + 1; j < NODES.length; j++) {
                double d = Math.hypot(NODES[i][0] - NODES[j][0], NODES[i][1] - NODES[j][1]);
                if (d < 210) {
                    g.setColor(new Color(0x8F, 0xD0, 0xF5, (int) Math.round(255 * 0.3 * (1 - d / 210))));
                    g.draw(new Line2D.Double(NODES[i][0], NODES[i][1], NODES[j][0], NODES[j][1]));
                }
            }
        }
        g.setColor(new Color(0xBF, 0xE0, 0xF5, 153));
        for (double[] n : NODES) {
            g.fill(circle(n[0], n[1], n[2]));
        }
    }

    // A soft round glow: `color` at the center fading to nothing at the radius.
    private static void glow(Graphics2D g, double cx, double cy, double radius, Color color) {
        g.setPaint(new java.awt.RadialGradientPaint((float) cx, (float) cy, (float) radius, new float[] {0f, 1f},
                new Color[] {color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)}));
        g.fill(circle(cx, cy, radius));
    }

    public byte[] render(AwardView badge) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            drawBackground(g);

            drawMedal(g, badge, 230, HEIGHT / 2.0, 340);
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

    // The medal in the site's design: the ACM ribbon, a laurel wreath, and a glossy medal with the icon and the
    // period printed on it. `size` is the height and width of the 120-unit box, centered on (cx, cy).
    private void drawMedal(Graphics2D g, AwardView badge, double cx, double cy, double size) {
        Palette p = palette(badge.periodType());
        double s = size / 120.0;
        double ox = cx - size / 2;
        double oy = cy - size / 2;
        AffineTransform saved = g.getTransform();
        g.translate(ox, oy);
        g.scale(s, s);

        // Ribbon.
        g.setColor(RIBBON_DEEP);
        g.fill(polygon(36, 2, 52, 2, 66, 40, 50, 40));
        g.setColor(RIBBON_LIGHT);
        g.fill(polygon(84, 2, 68, 2, 54, 40, 70, 40));
        g.setColor(new Color(255, 255, 255, 217));
        g.fill(polygon(41.5, 2, 45, 2, 58.5, 40, 55, 40));
        g.fill(polygon(78.5, 2, 75, 2, 61.5, 40, 65, 40));

        drawWreath(g, p);

        // Medal: rim, face, dashed ring, shine.
        g.setPaint(new LinearGradientPaint((float) (MEDAL_CX - 31), (float) (MEDAL_CY - 31), (float) (MEDAL_CX + 31), (float) (MEDAL_CY + 31),
                new float[] {0f, .5f, 1f}, new Color[] {p.rim1(), p.rim2(), p.rim1()}));
        g.fill(circle(MEDAL_CX, MEDAL_CY, 31));
        g.setPaint(new LinearGradientPaint(0f, (float) (MEDAL_CY - 26), 0f, (float) (MEDAL_CY + 26),
                new float[] {0f, .55f, 1f}, new Color[] {p.a(), p.b(), p.c()}));
        g.fill(circle(MEDAL_CX, MEDAL_CY, 26));
        g.setColor(new Color(255, 255, 255, 77));
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[] {1.5f, 2.5f}, 0f));
        g.draw(circle(MEDAL_CX, MEDAL_CY, 23));
        g.setColor(new Color(255, 255, 255, 56));
        g.fill(shine());

        String[] text = medalText(badge.periodType(), badge.periodLabel());
        double k = 26.0 / 28.0;
        g.setColor(p.ink());
        g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (text[0].isEmpty()) {
            drawIcon(g, badge.icon(), MEDAL_CX, MEDAL_CY, 1.4 * k);
            g.setTransform(saved);
            return;
        }
        drawIcon(g, badge.icon(), MEDAL_CX, MEDAL_CY - 9 * k, 0.78 * k);
        g.setTransform(saved);
        // Text is drawn in pixels (not through the scaled transform) so the glyphs stay crisp.
        centered(g, text[0], ox + MEDAL_CX * s, oy + (MEDAL_CY + 9 * k) * s, 11.5 * k * s, 0.6 * s, p.ink());
        centered(g, text[1], ox + MEDAL_CX * s, oy + (MEDAL_CY + 17.5 * k) * s, 6.2 * k * s, 1.0 * s, p.ink());
    }

    // The two lines on the medal: "Nov 2026" → NOV / 2026, "Q2 · 2027" → Q2 / 2027, "Season 2027" → 2027 / SEASON.
    static String[] medalText(PeriodType type, String label) {
        String l = label == null ? "" : label.trim();
        if (l.isEmpty()) {
            return new String[] {"", ""};
        }
        if (type == PeriodType.SEASON) {
            return new String[] {l.replaceFirst("(?i)^season\\s*", ""), "SEASON"};
        }
        String[] parts = l.split("[\\s·]+");
        return new String[] {parts[0].toUpperCase(Locale.ROOT), parts.length > 1 ? parts[1] : ""};
    }

    // Two sprigs of leaves along an arc on each side of the medal, each leaf tilted along its stem.
    private static void drawWreath(Graphics2D g, Palette p) {
        for (int side : new int[] {-1, 1}) {
            g.setColor(p.leafOuter());
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D stem = new Path2D.Double();
            for (int deg = 172; deg >= 62; deg -= 2) {
                double[] pt = wreathPoint(side, deg);
                if (deg == 172) {
                    stem.moveTo(pt[0], pt[1]);
                } else {
                    stem.lineTo(pt[0], pt[1]);
                }
            }
            g.draw(stem);
            for (int i = 0; i < 6; i++) {
                double deg = 165 - i * 19;
                double t = Math.toRadians(deg);
                double[] pt = wreathPoint(side, deg);
                double tangent = Math.toDegrees(Math.atan2(-Math.sin(t), -side * Math.cos(t)));
                for (int out : new int[] {1, -1}) {
                    double angle = tangent + out * side * 32;
                    double lx = pt[0] + Math.cos(Math.toRadians(angle)) * 6;
                    double ly = pt[1] + Math.sin(Math.toRadians(angle)) * 6;
                    AffineTransform before = g.getTransform();
                    g.rotate(Math.toRadians(angle), lx, ly);
                    g.setColor(out > 0 ? p.leafOuter() : p.leafInner());
                    g.fill(new Ellipse2D.Double(lx - 6.4, ly - 2.7, 12.8, 5.4));
                    g.setTransform(before);
                }
            }
        }
    }

    private static double[] wreathPoint(int side, double deg) {
        double t = Math.toRadians(deg);
        return new double[] {MEDAL_CX + side * WREATH_RADIUS * Math.sin(t), MEDAL_CY - WREATH_RADIUS * Math.cos(t)};
    }

    // A soft highlight across the top of the medal face.
    private static Path2D shine() {
        double centerY = MEDAL_CY - 8 + Math.sqrt(25 * 25 - 22 * 22);
        double from = Math.atan2(-Math.sqrt(25 * 25 - 22 * 22), -22);
        double to = Math.atan2(-Math.sqrt(25 * 25 - 22 * 22), 22);
        Path2D path = new Path2D.Double();
        path.moveTo(MEDAL_CX - 22, MEDAL_CY - 8);
        for (int i = 1; i <= 24; i++) {
            double a = from + (to - from) * i / 24.0;
            path.lineTo(MEDAL_CX + 25 * Math.cos(a), centerY + 25 * Math.sin(a));
        }
        path.curveTo(MEDAL_CX + 10, MEDAL_CY - 14, MEDAL_CX - 10, MEDAL_CY - 14, MEDAL_CX - 22, MEDAL_CY - 8);
        path.closePath();
        return path;
    }

    // The badge icon, centered on (cx, cy) in the medal's units, drawn at `scale` times its 24-unit box.
    private static void drawIcon(Graphics2D g, BadgeIcon icon, double cx, double cy, double scale) {
        AffineTransform saved = g.getTransform();
        g.translate(cx - 12 * scale, cy - 12 * scale);
        g.scale(scale, scale);
        for (Shape s : icon(icon)) {
            g.draw(s);
        }
        g.setTransform(saved);
    }

    // Bold text centered on x with the given baseline and letter spacing (all in pixels).
    private static void centered(Graphics2D g, String text, double x, double baseline, double size, double tracking, Color color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 12).deriveFont(Map.of(
                TextAttribute.SIZE, (float) size, TextAttribute.TRACKING, (float) (tracking / size)));
        TextLayout layout = new TextLayout(text, font, g.getFontRenderContext());
        g.setColor(color);
        layout.draw(g, (float) (x - layout.getAdvance() / 2), (float) baseline);
    }

    private static Path2D polygon(double... xy) {
        Path2D path = new Path2D.Double();
        path.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) {
            path.lineTo(xy[i], xy[i + 1]);
        }
        path.closePath();
        return path;
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
                LOGO_ROW,
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

        // The chapter row: the logo, then the chapter name centered on it.
        int logoWidth = drawLogo(g, x, y);
        FontMetrics chapterMetrics = g.getFontMetrics(chapter);
        int chapterX = x + (logoWidth > 0 ? logoWidth + 16 : 0);
        drawLine(g, chapter, GOLD_TEXT, "ACM Alexandria Student Chapter", chapterX,
                y + LOGO_ROW / 2 + (chapterMetrics.getAscent() - chapterMetrics.getDescent()) / 2, maxWidth - (chapterX - x));
        y += sizes[0];
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
