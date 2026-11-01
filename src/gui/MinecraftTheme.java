package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Giao diện kiểu Minecraft cho toàn bộ ứng dụng.
 * Mọi hình (nền đất, nền đá, nút, logo) đều được TỰ VẼ bằng code, không dùng ảnh hay font của game,
 * nên không cần thêm file nào và không dính bản quyền tài nguyên của Mojang.
 *
 * Cách hoạt động: {@link #apply()} đổi Look&Feel sang Metal rồi thay các lớp vẽ (xem {@link McUI})
 * cho nút, panel, nhãn, thanh cuộn, combo. Nhờ đó các panel cũ không phải sửa gì vẫn đổi diện mạo.
 */
public final class MinecraftTheme {
    private MinecraftTheme() {}

    /** Màu nền mặc định của nút "đá". */
    static final Color STONE_BTN = new Color(0x707070);
    /** Chữ khi rê chuột lên nút (vàng nhạt như trong game). */
    static final Color HOVER_TEXT = new Color(0xFFFFA0);

    /** Ô texture 16x16 phóng to 4 lần = 64x64 điểm ảnh, lát lặp lại để làm nền. */
    static final BufferedImage DIRT = taoTexture(UIStyle.BG_MAIN, 13, 11L);
    static final BufferedImage STONE = taoTexture(UIStyle.BG_SIDEBAR, 9, 23L);

    // ======================== VẼ ========================

    private static BufferedImage taoTexture(Color nen, int bienDo, long hat) {
        final int n = 16, k = 4;
        Random r = new Random(hat);   // cố định hạt giống để lần nào chạy cũng ra cùng một hoa văn
        BufferedImage t = new BufferedImage(n * k, n * k, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                int d = r.nextInt(2 * bienDo + 1) - bienDo;
                int may = r.nextInt(16);
                if (may == 0) d -= bienDo;             // đốm tối
                else if (may == 1) d += bienDo / 2;    // đốm sáng
                int rgb = new Color(kep(nen.getRed() + d), kep(nen.getGreen() + d), kep(nen.getBlue() + d)).getRGB();
                for (int j = 0; j < k; j++) {
                    for (int i = 0; i < k; i++) t.setRGB(x * k + i, y * k + j, rgb);
                }
            }
        }
        return t;
    }

    private static int kep(int v) { return Math.max(0, Math.min(255, v)); }

    static Color sang(Color c, int d) {
        return new Color(kep(c.getRed() + d), kep(c.getGreen() + d), kep(c.getBlue() + d), c.getAlpha());
    }

    static Color toi(Color c, int d) { return sang(c, -d); }

    /** Trộn hai màu: phần trăm wa của a, phần còn lại của b. */
    static Color tron(Color a, Color b, double wa) {
        return new Color(
                kep((int) Math.round(a.getRed() * wa + b.getRed() * (1 - wa))),
                kep((int) Math.round(a.getGreen() * wa + b.getGreen() * (1 - wa))),
                kep((int) Math.round(a.getBlue() * wa + b.getBlue() * (1 - wa))));
    }

    /** Lát texture lên toàn bộ component; neo theo cửa sổ gốc để các panel lồng nhau khớp đường nối. */
    static void latTexture(Graphics g, JComponent c, BufferedImage tile) {
        Graphics2D g2 = (Graphics2D) g.create();
        int ox = 0, oy = 0;
        JRootPane goc = c.getRootPane();
        if (goc != null) {
            Point p = SwingUtilities.convertPoint(c, 0, 0, goc);
            ox = -p.x;
            oy = -p.y;
        }
        g2.setPaint(new TexturePaint(tile, new Rectangle(ox, oy, tile.getWidth(), tile.getHeight())));
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        g2.dispose();
    }

    /** Khối "nổi" kiểu nút Minecraft: viền đen, cạnh trên/trái sáng, cạnh dưới/phải tối. */
    static void veKhoiNoi(Graphics g, int x, int y, int w, int h, Color nen, boolean lom) {
        if (w < 6 || h < 6) {
            g.setColor(nen);
            g.fillRect(x, y, w, h);
            return;
        }
        Color sang = sang(nen, 48), toi = toi(nen, 60);
        if (lom) { Color t = sang; sang = toi; toi = t; }
        g.setColor(Color.BLACK);
        g.fillRect(x, y, w, h);
        g.setColor(nen);
        g.fillRect(x + 1, y + 1, w - 2, h - 2);
        g.setColor(sang);
        g.fillRect(x + 1, y + 1, w - 2, 2);
        g.fillRect(x + 1, y + 1, 2, h - 2);
        g.setColor(toi);
        g.fillRect(x + 1, y + h - 3, w - 2, 2);
        g.fillRect(x + w - 3, y + 1, 2, h - 2);
    }

    /** Chữ có bóng đổ đen lệch (1,1) như chữ trong game. */
    static void veChuCoBong(Graphics g, String chu, int x, int y, Color mau) {
        g.setColor(new Color(mau.getRed() / 4, mau.getGreen() / 4, mau.getBlue() / 4));
        g.drawString(chu, x + 1, y + 1);
        g.setColor(mau);
        g.drawString(chu, x, y);
    }

    // ======================== LOGO ========================

    /** Chữ khối kiểu logo Minecraft: viền đen, mặt đá, dày xuống dưới-phải. */
    public static JComponent taoLogo(String chu, int coChu) {
        return new Logo(chu, coChu);
    }

    private static class Logo extends JComponent {
        private final String chu;
        private final Font font;

        Logo(String chu, int coChu) {
            this.chu = chu;
            this.font = new Font(UIStyle.FONT_FAMILY, Font.BOLD, coChu);
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            Insets in = getInsets();
            return new Dimension(fm.stringWidth(chu) + 10 + in.left + in.right, fm.getHeight() + 10 + in.top + in.bottom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics(font);
            Insets in = getInsets();
            int x = in.left + 3;
            int y = in.top + fm.getAscent() + 3;
            GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), chu);
            Shape chuHinh = gv.getOutline(x, y);

            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 4; i >= 1; i--) {   // phần "dày" của chữ
                Shape lech = AffineTransform.getTranslateInstance(i, i).createTransformedShape(chuHinh);
                g2.setColor(Color.BLACK);
                g2.draw(lech);
                g2.setColor(new Color(0x3B3B3B));
                g2.fill(lech);
            }
            g2.setColor(Color.BLACK);
            g2.draw(chuHinh);
            g2.setPaint(new GradientPaint(0, y - fm.getAscent(), new Color(0xE2E2E2),
                    0, y + fm.getDescent(), new Color(0x8A8A8A)));
            g2.fill(chuHinh);
            g2.dispose();
        }
    }

    // ======================== CÀI ĐẶT TOÀN CỤC ========================

    /** Gọi một lần trong main, TRƯỚC khi tạo bất kỳ cửa sổ nào. */
    public static void apply() {
        try {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());   // bỏ hiệu ứng gradient của theme Ocean
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {
            // không đổi được thì dùng L&F hiện tại, các lớp vẽ bên dưới vẫn có tác dụng
        }

        // Các lớp vẽ riêng (xem McUI)
        UIManager.put("ButtonUI", McUI.McButtonUI.class.getName());
        UIManager.put("PanelUI", McUI.McPanelUI.class.getName());
        UIManager.put("LabelUI", McUI.McLabelUI.class.getName());
        UIManager.put("ScrollBarUI", McUI.McScrollBarUI.class.getName());
        UIManager.put("ComboBoxUI", McUI.McComboBoxUI.class.getName());

        // Phông chữ: toàn bộ khóa "*.font" dùng phông chung, riêng vài loại in đậm
        FontUIResource thuong = new FontUIResource(UIStyle.FONT_BODY);
        FontUIResource dam = new FontUIResource(UIStyle.FONT_BOLD);
        List<Object> khoaFont = new ArrayList<>();
        for (java.util.Enumeration<Object> e = UIManager.getDefaults().keys(); e.hasMoreElements(); ) {
            Object k = e.nextElement();
            if (k instanceof String && ((String) k).endsWith(".font")) khoaFont.add(k);
        }
        for (Object k : khoaFont) UIManager.put(k, thuong);
        for (String k : new String[]{"Button.font", "TitledBorder.font", "TableHeader.font",
                "OptionPane.buttonFont", "ToolTip.font"}) {
            UIManager.put(k, dam);
        }

        Color chu = UIStyle.TEXT_DARK;
        Color den = new Color(0x000000), oDen = new Color(0x1B1B1B), chon = new Color(0x4D4D4D);
        Color vang = new Color(0xFFFF55), vienXam = new Color(0xA0A0A0);

        // Nền & chữ chung
        put("Panel.background", UIStyle.BG_MAIN);
        put("Panel.foreground", chu);
        put("Viewport.background", oDen);
        put("ScrollPane.background", oDen);
        put("Label.foreground", chu);
        put("Label.disabledForeground", new Color(0x808080));
        put("Button.background", STONE_BTN);
        put("Button.foreground", chu);
        put("RadioButton.foreground", chu);
        put("RadioButton.background", UIStyle.BG_MAIN);
        put("CheckBox.foreground", chu);
        put("CheckBox.background", UIStyle.BG_MAIN);
        put("Separator.foreground", den);
        put("Separator.background", new Color(0x6E6E6E));

        // Ô nhập: nền đen, chữ trắng, viền xám như khung chat
        javax.swing.border.Border oNhap = new BorderUIResource.CompoundBorderUIResource(
                new LineBorder(vienXam, 1), new EmptyBorder(3, 6, 3, 6));
        for (String k : new String[]{"TextField", "PasswordField", "FormattedTextField", "TextArea"}) {
            put(k + ".background", den);
            put(k + ".foreground", chu);
            put(k + ".caretForeground", chu);
            put(k + ".selectionBackground", new Color(0x3C5AA6));
            put(k + ".selectionForeground", chu);
            put(k + ".inactiveBackground", new Color(0x2A2A2A));
            put(k + ".inactiveForeground", new Color(0x9A9A9A));
            put(k + ".disabledBackground", new Color(0x2A2A2A));
            UIManager.put(k + ".border", oNhap);
        }

        // Danh sách / combo / bảng
        for (String k : new String[]{"List", "ComboBox", "Table"}) {
            put(k + ".background", oDen);
            put(k + ".foreground", chu);
            put(k + ".selectionBackground", chon);
            put(k + ".selectionForeground", vang);
        }
        put("ComboBox.background", den);
        put("ComboBox.disabledBackground", new Color(0x2A2A2A));
        put("ComboBox.disabledForeground", new Color(0x808080));
        put("Table.gridColor", den);
        UIManager.put("ComboBox.border", new BorderUIResource(new LineBorder(vienXam, 1)));
        UIManager.put("Table.focusCellHighlightBorder", new BorderUIResource(new EmptyBorder(1, 1, 1, 1)));
        BorderUIResource khungCuon = new BorderUIResource(new LineBorder(den, 2));
        UIManager.put("ScrollPane.border", khungCuon);
        UIManager.put("Table.scrollPaneBorder", khungCuon);
        UIManager.put("ScrollBar.width", 16);
        UIManager.put("TitledBorder.border", new BorderUIResource(new LineBorder(den, 2)));
        put("TitledBorder.titleColor", chu);

        // Hộp thoại JOptionPane (đổi nút sang tiếng Việt cho đồng bộ với ứng dụng)
        put("OptionPane.background", UIStyle.BG_MAIN);
        put("OptionPane.foreground", chu);
        put("OptionPane.messageForeground", chu);
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");

        // Tooltip: nền đen tím, viền tím như tooltip vật phẩm trong game
        put("ToolTip.background", new Color(0x100010));
        put("ToolTip.foreground", chu);
        UIManager.put("ToolTip.border", new BorderUIResource(new LineBorder(new Color(0x3C0F7A), 2)));
    }

    private static void put(String khoa, Color mau) {
        UIManager.put(khoa, new ColorUIResource(mau));
    }
}
