package gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Bảng màu + hàm dựng thành phần dùng chung cho cả ứng dụng, phong cách Minecraft.
 * Tên hằng và tên hàm giữ nguyên như bản cũ nên các panel không phải sửa; phần vẽ chi tiết
 * (nút nổi, nền đất/đá, thanh cuộn...) nằm ở {@link MinecraftTheme} và {@link McUI}.
 */
public class UIStyle {
    // ===== Màu nền =====
    public static final Color BG_MAIN = new Color(0x58402E);          // Đất tối: panel màu này được lát texture đất
    public static final Color BG_SIDEBAR = new Color(0x4F4F4F);       // Đá: panel màu này được lát texture đá
    public static final Color BG_SIDEBAR_HOVER = new Color(0x6E6E6E); // (giữ lại cho tương thích)
    public static final Color BG_CARD = new Color(0, 0, 0, 150);      // Khung đen trong suốt như danh sách thế giới

    // ===== Màu nhấn: lấy theo bảng màu chữ chat và vật phẩm trong game =====
    public static final Color PRIMARY = new Color(0x4DD0E1);          // Xanh kim cương
    public static final Color SUCCESS = new Color(0x55FF55);          // Xanh lá (ngọc lục bảo)
    public static final Color WARNING = new Color(0xFFAA00);          // Vàng (vàng thỏi)
    public static final Color DANGER = new Color(0xFF5555);           // Đỏ (đá đỏ)

    // ===== Màu chữ (tên cũ giữ nguyên: TEXT_DARK = chữ chính, nay là chữ trắng trên nền tối) =====
    public static final Color TEXT_DARK = new Color(0xFFFFFF);
    public static final Color TEXT_MUTED = new Color(0xBDBDBD);

    // Màu riêng của bảng
    private static final Color HANG_A = new Color(0x1B1B1B);
    private static final Color HANG_B = new Color(0x262626);
    private static final Color HANG_CHON = new Color(0x4D4D4D);
    private static final Color CHU_CHON = new Color(0xFFFF55);

    // Phông chữ: chữ nội dung dùng phông không chân, rõ nét (Segoe UI trên Windows);
    // phông đơn cách kiểu "khối" chỉ dùng cho logo để giữ không khí Minecraft.
    public static final String FONT_FAMILY = chonFontDocDuoc();
    public static final String FONT_MONO = chonFontTotNhat();

    public static final Font FONT_TITLE    = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font FONT_BODY     = new Font(FONT_FAMILY, Font.PLAIN, 15);
    public static final Font FONT_BOLD     = new Font(FONT_FAMILY, Font.BOLD, 15);

    /** Toàn bộ chữ cái tiếng Việt có dấu: font nào không hiển thị được hết thì bị loại. */
    private static final String CHU_VIET =
            "àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ"
          + "ÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ";

    /** Phông không chân dễ đọc, bắt buộc hiển thị đủ dấu tiếng Việt; không có thì dùng SansSerif (Java tự mượn glyph thiếu). */
    private static String chonFontDocDuoc() {
        String[] uuTien = {"Segoe UI", "Tahoma", "Arial", "DejaVu Sans"};
        String[] fontMayTinh = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String f : uuTien) {
            for (String sysFont : fontMayTinh) {
                if (sysFont.equalsIgnoreCase(f)
                        && new Font(sysFont, Font.PLAIN, 14).canDisplayUpTo(CHU_VIET) == -1
                        && new Font(sysFont, Font.BOLD, 14).canDisplayUpTo(CHU_VIET) == -1) {
                    return sysFont;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    private static String chonFontTotNhat() {
        // Ưu tiên phông đơn cách (gần chữ khối của game) NHƯNG phải đủ dấu tiếng Việt.
        // Không có thì dùng phông logic "Monospaced": Java tự mượn glyph còn thiếu từ phông khác.
        String[] uuTien = {"Consolas", "DejaVu Sans Mono", "Courier New"};
        String[] fontMayTinh = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String f : uuTien) {
            for (String sysFont : fontMayTinh) {
                if (sysFont.equalsIgnoreCase(f)
                        && new Font(sysFont, Font.PLAIN, 14).canDisplayUpTo(CHU_VIET) == -1
                        && new Font(sysFont, Font.BOLD, 14).canDisplayUpTo(CHU_VIET) == -1) {
                    return sysFont;
                }
            }
        }
        return Font.MONOSPACED;
    }

    /** Tải font tùy chọn từ resource nếu có file .ttf trong thư mục /fonts/ */
    public static void loadCustomFont() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.io.InputStream isReg = UIStyle.class.getResourceAsStream("/fonts/Inter-Regular.ttf");
            if (isReg != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, isReg));
            }
            java.io.InputStream isBold = UIStyle.class.getResourceAsStream("/fonts/Inter-Bold.ttf");
            if (isBold != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, isBold));
            }
        } catch (Exception e) {
            System.out.println("Chưa có file font ngoài, dùng font hệ thống.");
        }
    }

    // 1. Nút bấm kiểu Minecraft: bgColor là màu "gợi ý" (xanh = chính, đỏ = xóa...), được pha với xám đá
    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(TEXT_DARK);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // 2. Nút menu ở thanh bên: cũng là nút nổi; nút đang chọn được MainFrame tô màu PRIMARY
    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUBTITLE);
        btn.setForeground(TEXT_DARK);
        btn.setBackground(BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(14, 20, 14, 20));
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // 3. Bảng: nền đen, dòng xen kẽ, dòng chọn đổi sang chữ vàng; tiêu đề cột là khối nổi xám
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(Color.BLACK);
        table.setBackground(HANG_A);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(HANG_CHON);
        table.setSelectionForeground(CHU_CHON);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);
        final TableCellRenderer goc = header.getDefaultRenderer();   // giữ mũi tên sắp xếp của renderer gốc
        header.setDefaultRenderer((t, value, sel, foc, row, col) -> {
            Component c = goc.getTableCellRendererComponent(t, value, sel, foc, row, col);
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setOpaque(true);
                l.setBackground(MinecraftTheme.STONE_BTN);
                l.setForeground(TEXT_DARK);
                l.setFont(FONT_BOLD);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 1),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createBevelBorder(BevelBorder.RAISED,
                                        new Color(0xB0B0B0), new Color(0x959595),
                                        new Color(0x303030), new Color(0x4A4A4A)),
                                new EmptyBorder(0, 10, 0, 10))));
            }
            return c;
        });

        // Tô màu xen kẽ các dòng
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? HANG_A : HANG_B);
                    c.setForeground(TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Chữ đơn cách khá rộng: chia đều cột sẽ làm tên bị cắt thành "...", nên canh độ rộng theo nội dung
        final boolean[] dangCho = {false};
        table.getModel().addTableModelListener(e -> {
            if (!dangCho[0]) {   // nạp lại bảng phát ra rất nhiều sự kiện liên tiếp: gộp thành một lần canh
                dangCho[0] = true;
                SwingUtilities.invokeLater(() -> {
                    dangCho[0] = false;
                    canhCot(table);
                });
            }
        });
        canhCot(table);
    }

    /**
     * Đặt độ rộng của từng cột theo chữ dài nhất (tiêu đề hoặc 300 dòng đầu).
     * Độ rộng ưu tiên = đúng nhu cầu; độ rộng tối thiểu = nhu cầu nhưng tối đa 150 px.
     * Nhờ vậy khi khung hẹp thì chỉ các cột chữ dài (tên sách...) bị co lại, cột ngắn (ngày, mã) không bị cắt.
     */
    private static void canhCot(JTable t) {
        FontMetrics fm = t.getFontMetrics(t.getFont());
        FontMetrics fh = t.getFontMetrics(FONT_BOLD);
        int soDong = Math.min(t.getRowCount(), 300);
        for (int c = 0; c < t.getColumnCount(); c++) {
            int w = fh.stringWidth(String.valueOf(t.getColumnName(c))) + 26;
            for (int r = 0; r < soDong; r++) {
                Object v = t.getValueAt(r, c);
                if (v != null) w = Math.max(w, fm.stringWidth(v.toString()) + 20);
            }
            javax.swing.table.TableColumn cot = t.getColumnModel().getColumn(c);
            cot.setMinWidth(Math.min(w, 150));
            cot.setPreferredWidth(Math.min(w, 460));
        }
    }

    // 4. Nhãn màu cho cột Trạng thái / Loại sách (dùng màu chữ chat của game)
    public static void setBadgeRenderer(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String text = value != null ? value.toString() : "";
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(FONT_BOLD);

                String lower = text.toLowerCase();
                if (lower.contains("quá hạn") || lower.contains("hết") || lower.contains("khóa")) {
                    label.setForeground(DANGER);
                } else if (lower.contains("đang mượn") || lower.contains("tạp chí")) {
                    label.setForeground(WARNING);
                } else if (lower.contains("đã trả") || lower.contains("còn") || lower.contains("giáo trình")) {
                    label.setForeground(SUCCESS);
                } else {
                    label.setForeground(PRIMARY);
                }
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? HANG_A : HANG_B);
                }
                return label;
            }
        });
    }

    // 5. Ô nhập liệu: nền đen, viền xám vuông vức như khung chat
    public static void styleTextField(JTextField txt) {
        txt.setFont(FONT_BODY);
        txt.setPreferredSize(new Dimension(220, 36));
        txt.setBackground(Color.BLACK);
        txt.setForeground(TEXT_DARK);
        txt.setCaretColor(TEXT_DARK);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xA0A0A0), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    // 6. Combo: cùng kiểu với ô nhập; nút mũi tên do McComboBoxUI vẽ
    public static void styleComboBox(JComboBox<?> cbo) {
        cbo.setFont(FONT_BODY);
        cbo.setBackground(Color.BLACK);
        cbo.setForeground(TEXT_DARK);
        cbo.setPreferredSize(new Dimension(190, 36));
        cbo.setBorder(BorderFactory.createLineBorder(new Color(0xA0A0A0), 1));
        cbo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // 7. Khung đen trong suốt viền 2 lớp (như khung danh sách của game) để bọc các vùng nội dung
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(BG_CARD);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g.setColor(new Color(0x3A3A3A));
                g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }

    // 8. Chữ khối kiểu logo Minecraft (dùng ở góc thanh bên)
    public static JComponent createLogo(String text) {
        return MinecraftTheme.taoLogo(text, 24);
    }
}
