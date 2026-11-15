import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Bakes the demo poster thumbnails into
 * {@code src/main/resources/Testing_for_bascially_everything/assets/posters}.
 *
 * <p>Run from the project root with:</p>
 *
 * <pre>java tools/GeneratePosters.java</pre>
 *
 * <p>The artwork is generated locally so the project has no external image
 * dependency. The app overlays the movie title and genre tag, so the images are
 * deliberately text-free except for a faint ghost initial.</p>
 */
public final class GeneratePosters {

    private static final int W = 540;
    private static final int H = 810;

    private record Poster(String slug, String from, String to) { }

    private static final Poster[] POSTERS = {
        new Poster("blade-runner-2049", "#553199", "#342066"),
        new Poster("dune-part-two", "#342066", "#1e1b26"),
        new Poster("arrival", "#3d2470", "#553199"),
        new Poster("whiplash", "#221c35", "#342066"),
        new Poster("parasite", "#4a2b85", "#221c35"),
        new Poster("interstellar", "#1e1b26", "#553199"),
        new Poster("la-la-land", "#6a42b8", "#342066"),
        new Poster("nightcrawler", "#2b1a4d", "#1e1b26"),
        new Poster("her", "#553199", "#221c35"),
        new Poster("knives-out", "#342066", "#4a2b85"),
        new Poster("mad-max", "#1e1b26", "#3d2470"),
        new Poster("moonlight", "#221c35", "#553199"),
        new Poster("the-batman", "#2b1a4d", "#342066")
    };

    private GeneratePosters() {
    }

    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/Testing_for_bascially_everything/assets/posters");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + dir.getAbsolutePath());
        }
        for (int index = 0; index < POSTERS.length; index++) {
            BufferedImage image = render(POSTERS[index], index);
            File out = new File(dir, POSTERS[index].slug() + ".png");
            ImageIO.write(image, "png", out);
            System.out.println("wrote " + out.getPath() + " (" + out.length() / 1024 + " KB)");
        }
    }

    private static BufferedImage render(Poster poster, int index) {
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Color from = Color.decode(poster.from());
        Color to = Color.decode(poster.to());

        g.setPaint(new GradientPaint(0, 0, from, W, H, to));
        g.fillRect(0, 0, W, H);

        switch (index) {
            case 0 -> rainyCity(g);
            case 1 -> desertDunes(g);
            case 2 -> monolith(g);
            case 3 -> spotlightDrums(g);
            case 4 -> hillHouse(g);
            case 5 -> wormhole(g);
            case 6 -> cityMoon(g);
            case 7 -> cameraLens(g);
            case 8 -> warmBokeh(g);
            case 9 -> manorKnife(g);
            case 10 -> desertChase(g);
            case 11 -> oceanMoon(g);
            case 12 -> gothicWings(g);
            default -> { }
        }

        ghostInitial(g, titleFor(poster.slug()));
        finish(g);
        grain(g, new Random(poster.slug().hashCode()));
        g.dispose();
        return image;
    }

    private static String titleFor(String slug) {
        return slug.substring(0, 1).toUpperCase();
    }

    // ------------------------------------------------------------- motifs

    private static void rainyCity(Graphics2D g) {
        glow(g, 300, 520, 360, color("#ff8a3c"), 0.30f);
        Random random = new Random(11);
        for (int i = 0; i < 26; i++) {
            int barWidth = 10 + random.nextInt(26);
            int barHeight = 60 + random.nextInt(240);
            int x = random.nextInt(W);
            int y = 660 - barHeight;
            g.setColor(new Color(12, 9, 20, 170 + random.nextInt(60)));
            g.fillRect(x, y, barWidth, barHeight);
            if (random.nextBoolean()) {
                g.setColor(new Color(255, 170, 90, 90));
                g.fillRect(x + 3, y + 6, 2, barHeight - 10);
            }
        }
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 110; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            g.setColor(new Color(200, 190, 255, 18 + random.nextInt(30)));
            g.drawLine(x, y, x - 6, y + 26);
        }
    }

    private static void desertDunes(Graphics2D g) {
        glow(g, 330, 270, 270, color("#ffb066"), 0.40f);
        g.setColor(new Color(255, 196, 120, 220));
        g.fill(new Ellipse2D.Double(240, 170, 180, 180));
        Color[] dunes = {color("#6b4a2a"), color("#5a3d24"), color("#472f1d"), color("#2f1f14")};
        for (int i = 0; i < 4; i++) {
            double y = 470 + i * 92;
            Path2D path = new Path2D.Double();
            path.moveTo(0, y);
            path.curveTo(140, y - 70, 300, y + 50, W, y - 30);
            path.lineTo(W, H);
            path.lineTo(0, H);
            path.closePath();
            g.setColor(dunes[i]);
            g.fill(path);
        }
        g.setColor(new Color(20, 14, 10, 225));
        g.fill(new RoundRectangle2D.Double(268, 500, 8, 26, 4, 4));
        g.fill(new Ellipse2D.Double(269, 492, 7, 7));
    }

    private static void monolith(Graphics2D g) {
        glow(g, 270, 330, 300, color("#9fe0c8"), 0.30f);
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(210, 235, 225, 26));
            g.fillRect(0, 300 + i * 46, W, 14);
        }
        g.setPaint(new GradientPaint(0, 150, color("#3b3f42"), 0, 560, color("#15181a")));
        g.fill(new RoundRectangle2D.Double(200, 140, 140, 430, 70, 70));
        g.setColor(new Color(255, 255, 255, 18));
        g.fill(new RoundRectangle2D.Double(212, 152, 38, 406, 19, 19));
        g.setColor(new Color(10, 12, 12, 225));
        g.fill(new RoundRectangle2D.Double(160, 540, 9, 30, 5, 5));
        g.fill(new Ellipse2D.Double(161, 531, 8, 8));
    }

    private static void spotlightDrums(Graphics2D g) {
        glow(g, 270, 620, 380, color("#e04a3a"), 0.35f);
        Path2D cone = new Path2D.Double();
        cone.moveTo(250, 0);
        cone.lineTo(290, 0);
        cone.lineTo(430, 620);
        cone.lineTo(110, 620);
        cone.closePath();
        g.setPaint(new GradientPaint(0, 0, new Color(255, 240, 210, 85),
            0, 620, new Color(255, 240, 210, 8)));
        g.fill(cone);
        g.setColor(new Color(240, 190, 120, 190));
        g.fill(new Ellipse2D.Double(120, 600, 90, 18));
        g.fill(new Ellipse2D.Double(330, 640, 100, 20));
        g.setColor(new Color(120, 30, 30, 220));
        g.fill(new RoundRectangle2D.Double(230, 600, 90, 60, 10, 10));
        g.setColor(new Color(8, 6, 10, 235));
        g.fill(new Ellipse2D.Double(252, 520, 34, 38));
        g.fill(new RoundRectangle2D.Double(238, 552, 62, 70, 18, 18));
    }

    private static void hillHouse(Graphics2D g) {
        g.setColor(new Color(30, 70, 55, 60));
        g.fillRect(0, 0, W, H);
        Path2D hill = new Path2D.Double();
        hill.moveTo(0, 640);
        hill.curveTo(160, 560, 340, 600, W, 520);
        hill.lineTo(W, H);
        hill.lineTo(0, H);
        hill.closePath();
        g.setColor(new Color(16, 24, 20, 255));
        g.fill(hill);
        g.setColor(new Color(22, 26, 24, 255));
        g.fill(new RoundRectangle2D.Double(150, 500, 250, 120, 4, 4));
        g.setColor(new Color(255, 196, 110, 200));
        for (int i = 0; i < 4; i++) {
            g.fillRect(175 + i * 55, 530, 26, 34);
        }
        g.setColor(new Color(40, 44, 40, 255));
        for (int i = 0; i < 6; i++) {
            g.fillRect(200 + i * 18, 640 + i * 12, 120 - i * 10, 10);
        }
        Random random = new Random(44);
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 120; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            g.setColor(new Color(190, 220, 210, 24));
            g.drawLine(x, y, x - 4, y + 18);
        }
    }

    private static void wormhole(Graphics2D g) {
        Random random = new Random(55);
        for (int i = 0; i < 220; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            int size = 1 + random.nextInt(2);
            g.setColor(new Color(255, 255, 255, 40 + random.nextInt(150)));
            g.fillRect(x, y, size, size);
        }
        for (int i = 0; i < 7; i++) {
            double radius = 60 + i * 26;
            g.setStroke(new BasicStroke(Math.max(1f, 3f - i * 0.25f)));
            g.setColor(new Color(200, 180, 255, 110 - i * 12));
            g.draw(new Ellipse2D.Double(270 - radius, 360 - radius * 0.72,
                radius * 2, radius * 1.44));
        }
        glow(g, 270, 360, 200, color("#b9a6ff"), 0.35f);
    }

    private static void cityMoon(Graphics2D g) {
        glow(g, 340, 220, 260, color("#ffd9a0"), 0.35f);
        g.setColor(new Color(255, 236, 200, 230));
        g.fill(new Ellipse2D.Double(270, 120, 160, 160));
        Random random = new Random(66);
        for (int i = 0; i < 18; i++) {
            int barWidth = 14 + random.nextInt(24);
            int barHeight = 40 + random.nextInt(150);
            int x = random.nextInt(W);
            g.setColor(new Color(18, 12, 28, 210));
            g.fillRect(x, 640 - barHeight, barWidth, barHeight);
        }
        g.setColor(new Color(255, 200, 140, 40));
        g.fillRect(0, 640, W, 40);
        g.setColor(new Color(10, 8, 14, 235));
        g.fill(new Ellipse2D.Double(240, 540, 22, 24));
        g.fill(new RoundRectangle2D.Double(236, 562, 30, 80, 12, 12));
        g.fill(new Ellipse2D.Double(285, 545, 20, 22));
        g.fill(new RoundRectangle2D.Double(282, 566, 26, 76, 12, 12));
    }

    private static void cameraLens(Graphics2D g) {
        glow(g, 270, 340, 300, color("#6fc6ff"), 0.25f);
        for (int i = 0; i < 6; i++) {
            double radius = 70 + i * 28;
            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(150, 210, 255, 90 - i * 12));
            g.draw(new Ellipse2D.Double(270 - radius, 340 - radius, radius * 2, radius * 2));
        }
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(170, 220, 255, 70));
        g.drawLine(0, 340, W, 340);
        g.drawLine(270, 0, 270, H);
        Random random = new Random(77);
        for (int i = 0; i < 40; i++) {
            int y = 560 + random.nextInt(220);
            g.setColor(new Color(255, 180, 120, 20 + random.nextInt(50)));
            g.fillRect(random.nextInt(W), y, 20 + random.nextInt(90), 3);
        }
    }

    private static void warmBokeh(Graphics2D g) {
        glow(g, 300, 300, 320, color("#ffb36b"), 0.30f);
        Random random = new Random(88);
        for (int i = 0; i < 26; i++) {
            int radius = 12 + random.nextInt(60);
            int x = random.nextInt(W);
            int y = random.nextInt(H - 100);
            g.setColor(new Color(255, 190, 120, 12 + random.nextInt(30)));
            g.fill(new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2));
        }
        g.setColor(new Color(18, 12, 10, 230));
        g.fill(new Ellipse2D.Double(250, 400, 44, 48));
        g.fill(new RoundRectangle2D.Double(236, 446, 72, 220, 26, 26));
    }

    private static void manorKnife(Graphics2D g) {
        glow(g, 270, 520, 300, color("#ff4d5a"), 0.28f);
        g.setColor(new Color(24, 16, 26, 235));
        g.fill(new Rectangle2D.Double(90, 430, 360, 240));
        Path2D roof = new Path2D.Double();
        roof.moveTo(90, 430);
        roof.lineTo(270, 330);
        roof.lineTo(450, 430);
        roof.closePath();
        g.fill(roof);
        g.setColor(new Color(255, 190, 90, 170));
        for (int i = 0; i < 3; i++) {
            g.fillRect(140 + i * 90, 480, 34, 52);
        }
        g.setColor(new Color(10, 6, 10, 255));
        g.fillRect(250, 570, 52, 100);
        g.setStroke(new BasicStroke(5f));
        g.setColor(new Color(230, 235, 245, 220));
        g.drawLine(300, 780, 400, 620);
        g.setStroke(new BasicStroke(9f));
        g.setColor(new Color(60, 40, 30, 255));
        g.drawLine(290, 796, 310, 762);
    }

    private static void desertChase(Graphics2D g) {
        glow(g, 360, 300, 300, color("#ff7a1a"), 0.40f);
        g.setColor(new Color(255, 150, 60, 220));
        g.fill(new Ellipse2D.Double(290, 230, 140, 140));
        Random random = new Random(101);
        for (int i = 0; i < 50; i++) {
            int y = 380 + random.nextInt(360);
            int x = random.nextInt(W);
            g.setColor(new Color(255, 220, 190, 15 + random.nextInt(45)));
            g.fillRect(x, y, 60 + random.nextInt(180), 2 + random.nextInt(3));
        }
        Path2D dune = new Path2D.Double();
        dune.moveTo(0, 640);
        dune.curveTo(160, 600, 360, 680, W, 620);
        dune.lineTo(W, H);
        dune.lineTo(0, H);
        dune.closePath();
        g.setColor(new Color(40, 24, 18, 235));
        g.fill(dune);
    }

    private static void oceanMoon(Graphics2D g) {
        glow(g, 270, 230, 260, color("#8fb8ff"), 0.30f);
        g.setColor(new Color(220, 235, 255, 235));
        g.fill(new Ellipse2D.Double(210, 170, 120, 120));
        g.setColor(new Color(8, 12, 24, 255));
        g.fill(new Rectangle2D.Double(0, 470, W, H - 470));
        Random random = new Random(111);
        for (int i = 0; i < 40; i++) {
            int y = 480 + random.nextInt(300);
            int width = 20 + random.nextInt(120);
            g.setColor(new Color(180, 210, 255, 20 + random.nextInt(60)));
            g.fillRect(270 - width / 2, y, width, 2);
        }
        g.setColor(new Color(5, 7, 12, 255));
        g.fill(new Ellipse2D.Double(120, 600, 16, 18));
        g.fill(new RoundRectangle2D.Double(114, 616, 26, 70, 10, 10));
    }

    private static void gothicWings(Graphics2D g) {
        glow(g, 270, 260, 280, color("#d83a3a"), 0.30f);
        g.setColor(new Color(5, 4, 8, 235));
        Path2D left = new Path2D.Double();
        left.moveTo(270, 250);
        left.curveTo(180, 210, 120, 260, 110, 360);
        left.curveTo(170, 330, 220, 330, 270, 360);
        left.closePath();
        g.fill(left);
        Path2D right = new Path2D.Double();
        right.moveTo(270, 250);
        right.curveTo(360, 210, 420, 260, 430, 360);
        right.curveTo(370, 330, 320, 330, 270, 360);
        right.closePath();
        g.fill(right);
        Random random = new Random(121);
        for (int i = 0; i < 14; i++) {
            int barWidth = 20 + random.nextInt(30);
            int barHeight = 60 + random.nextInt(180);
            int x = random.nextInt(W);
            g.setColor(new Color(8, 6, 12, 240));
            g.fillRect(x, 700 - barHeight, barWidth, barHeight);
        }
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 90; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            g.setColor(new Color(200, 190, 220, 22));
            g.drawLine(x, y, x - 5, y + 22);
        }
    }

    // ------------------------------------------------------------ finishing

    private static void ghostInitial(Graphics2D g, String initial) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 330));
        FontMetrics metrics = g.getFontMetrics();
        int x = (W - metrics.stringWidth(initial)) / 2;
        int y = (H + metrics.getAscent() - metrics.getDescent()) / 2;
        g.setColor(new Color(255, 255, 255, 24));
        g.drawString(initial, x, y);
    }

    private static void finish(Graphics2D g) {
        g.setPaint(new GradientPaint(0, (float) (H * 0.55), new Color(0, 0, 0, 0),
            0, H, new Color(0, 0, 0, 228)));
        g.fillRect(0, (int) (H * 0.55), W, (int) (H * 0.45));

        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 90),
            0, (float) (H * 0.26), new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, W, (int) (H * 0.26));

        float radius = (float) (H * 0.85);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(W / 2.0, H * 0.42), radius,
            new float[]{0.35f, 1f},
            new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)}));
        g.fillRect(0, 0, W, H);
    }

    private static void grain(Graphics2D g, Random random) {
        Composite composite = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver);
        for (int i = 0; i < 5000; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            g.setColor(new Color(255, 255, 255, 4 + random.nextInt(14)));
            g.fillRect(x, y, 1, 1);
        }
        g.setComposite(composite);
    }

    private static void glow(Graphics2D g, double cx, double cy, double radius,
                             Color colour, float alpha) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) radius,
            new float[]{0f, 1f},
            new Color[]{alpha(colour, (int) (alpha * 255)), alpha(colour, 0)}));
        g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
    }

    private static Color alpha(Color colour, int alpha) {
        return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);
    }

    private static Color color(String hex) {
        return Color.decode(hex);
    }
}
