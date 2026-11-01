package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.*;
import java.awt.*;

/**
 * Các lớp vẽ (UI delegate) kiểu Minecraft. Được đăng ký trong {@link MinecraftTheme#apply()},
 * nên mọi JButton / JPanel / JLabel / JScrollBar / JComboBox trong ứng dụng tự dùng chúng.
 * Các lớp phải public và có hàm static createUI thì Swing mới tạo được qua tên lớp.
 */
public final class McUI {
    private McUI() {}

    // ------------------------------------------------------------------ NÚT
    /** Nút xám có viền đen và cạnh nổi. Màu nền do setBackground quyết định (xem createButton). */
    public static class McButtonUI extends BasicButtonUI {
        private static final McButtonUI INSTANCE = new McButtonUI();

        public static ComponentUI createUI(JComponent c) { return INSTANCE; }

        @Override
        protected void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            b.setOpaque(false);
            b.setRolloverEnabled(true);
            b.setFocusPainted(false);
            if (b.getBorder() == null || b.getBorder() instanceof UIResource) {
                b.setBorder(new BorderUIResource(new EmptyBorder(7, 16, 7, 16)));
            }
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            if (d != null) d.height = Math.max(d.height, 30);
            return d;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            boolean nhan = m.isArmed() && m.isPressed();

            // Màu nền mặc định = xám đá; màu do code đặt (xanh/đỏ/vàng...) được pha với xám để vẫn "Minecraft"
            Color mau = b.getBackground();
            Color nen = (mau == null || mau instanceof UIResource)
                    ? MinecraftTheme.STONE_BTN
                    : MinecraftTheme.tron(mau, MinecraftTheme.STONE_BTN, 0.5);
            if (!m.isEnabled()) nen = new Color(0x474747);
            else if (nhan) nen = MinecraftTheme.toi(nen, 16);
            else if (m.isRollover()) nen = MinecraftTheme.sang(nen, 22);

            MinecraftTheme.veKhoiNoi(g, 0, 0, c.getWidth(), c.getHeight(), nen, nhan);
            super.paint(g, c);   // vẽ chữ/biểu tượng lên trên
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            ButtonModel m = b.getModel();
            FontMetrics fm = g.getFontMetrics(b.getFont());
            Color mau;
            if (!m.isEnabled()) mau = new Color(0xA0A0A0);
            else if (m.isRollover() && !m.isPressed()) mau = MinecraftTheme.HOVER_TEXT;
            else mau = b.getForeground();
            int dich = (m.isArmed() && m.isPressed()) ? 1 : 0;
            MinecraftTheme.veChuCoBong(g, text, textRect.x + dich, textRect.y + fm.getAscent() + dich, mau);
        }

        @Override
        protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect,
                                  Rectangle textRect, Rectangle iconRect) {
            // Minecraft không vẽ khung focus
        }
    }

    // ------------------------------------------------------------------ PANEL
    /**
     * Panel có màu nền trùng màu "đất" (BG_MAIN, cũng là màu Panel mặc định) hoặc "đá" (BG_SIDEBAR)
     * sẽ được lát texture thay vì tô một màu. Panel có màu khác vẫn tô màu thường.
     */
    public static class McPanelUI extends BasicPanelUI {
        private static final McPanelUI INSTANCE = new McPanelUI();

        public static ComponentUI createUI(JComponent c) { return INSTANCE; }

        @Override
        public void update(Graphics g, JComponent c) {
            if (c.isOpaque()) {
                Color nen = c.getBackground();
                if (UIStyle.BG_MAIN.equals(nen)) {
                    MinecraftTheme.latTexture(g, c, MinecraftTheme.DIRT);
                } else if (UIStyle.BG_SIDEBAR.equals(nen)) {
                    MinecraftTheme.latTexture(g, c, MinecraftTheme.STONE);
                } else {
                    g.setColor(nen);
                    g.fillRect(0, 0, c.getWidth(), c.getHeight());
                }
            }
            paint(g, c);
        }
    }

    // ------------------------------------------------------------------ NHÃN
    /** Nhãn có bóng đổ như chữ trong game (chỉ áp dụng cho chữ thường, không áp dụng cho nhãn HTML). */
    public static class McLabelUI extends BasicLabelUI {
        private static final McLabelUI INSTANCE = new McLabelUI();

        public static ComponentUI createUI(JComponent c) { return INSTANCE; }

        @Override
        protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
            Color mau = l.getForeground();
            g.setColor(new Color(mau.getRed() / 4, mau.getGreen() / 4, mau.getBlue() / 4));
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, s, -1, textX + 1, textY + 1);
            g.setColor(mau);
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, s, l.getDisplayedMnemonicIndex(), textX, textY);
        }
    }

    // ------------------------------------------------------------------ THANH CUỘN
    /** Thanh cuộn: rãnh đen, thanh kéo xám nổi, không có nút mũi tên. */
    public static class McScrollBarUI extends BasicScrollBarUI {
        public static ComponentUI createUI(JComponent c) { return new McScrollBarUI(); }

        private JButton nutKhong() {
            JButton b = new JButton();
            Dimension khong = new Dimension(0, 0);
            b.setPreferredSize(khong);
            b.setMinimumSize(khong);
            b.setMaximumSize(khong);
            return b;
        }

        @Override protected JButton createDecreaseButton(int orientation) { return nutKhong(); }
        @Override protected JButton createIncreaseButton(int orientation) { return nutKhong(); }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(new Color(0x101010));
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Color nen = isDragging ? MinecraftTheme.sang(MinecraftTheme.STONE_BTN, 20)
                    : isThumbRollover() ? MinecraftTheme.sang(MinecraftTheme.STONE_BTN, 12)
                    : MinecraftTheme.STONE_BTN;
            MinecraftTheme.veKhoiNoi(g, r.x, r.y, r.width, r.height, nen, false);
        }
    }

    // ------------------------------------------------------------------ COMBOBOX
    /** Combo: giữ nguyên cách chạy của Basic, chỉ đổi nút mũi tên thành khối nổi xám. */
    public static class McComboBoxUI extends BasicComboBoxUI {
        public static ComponentUI createUI(JComponent c) { return new McComboBoxUI(); }

        @Override
        protected JButton createArrowButton() {
            BasicArrowButton nut = new BasicArrowButton(SwingConstants.SOUTH,
                    MinecraftTheme.STONE_BTN, new Color(0x303030), Color.WHITE, new Color(0xA8A8A8));
            nut.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            return nut;
        }
    }
}
