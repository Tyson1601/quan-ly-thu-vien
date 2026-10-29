package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIStyle {
    // Bảng màu Modern Flat (Sáng sủa, dịu mắt)
    public static final Color BG_MAIN = new Color(244, 246, 249);       // Xám trắng nhạt (Nền chính)
    public static final Color BG_SIDEBAR = new Color(30, 41, 59);       // Xanh đen Slate (Sidebar)
    public static final Color BG_SIDEBAR_HOVER = new Color(51, 65, 85); // Màu khi di chuột ở Sidebar
    public static final Color BG_CARD = Color.WHITE;                    // Trắng (Nền các khung/bảng)

    public static final Color PRIMARY = new Color(37, 99, 235);         // Xanh dương chủ đạo
    public static final Color SUCCESS = new Color(22, 163, 74);         // Xanh lá (Hoàn thành / Còn sách)
    public static final Color WARNING = new Color(217, 119, 6);         // Cam (Cảnh báo / Sắp đến hạn)
    public static final Color DANGER = new Color(220, 38, 38);          // Đỏ (Xóa / Quá hạn / Hết sách)

    public static final Color TEXT_DARK = new Color(15, 23, 42);        // Chữ chính
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Chữ phụ

    // Tự động phát hiện và chọn Font chữ tối ưu nhất trên hệ thống
    public static final String FONT_FAMILY = chonFontTotNhat();

    public static final Font FONT_TITLE    = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_BODY     = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BOLD     = new Font(FONT_FAMILY, Font.BOLD, 14);

    private static String chonFontTotNhat() {
        String[] uuTien = {"Inter", "Be Vietnam Pro", "Roboto", "Tahoma", "Segoe UI"};
        String[] fontMayTinh = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String f : uuTien) {
            for (String sysFont : fontMayTinh) {
                if (sysFont.equalsIgnoreCase(f)) {
                    return sysFont;
                }
            }
        }
        return "SansSerif";
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

    // 1. Hàm tạo nút bấm hiện đại (Bo góc + Hiệu ứng đổi màu khi di chuột)
    public static JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // 2. Hàm tạo nút Menu bên thanh điều hướng (Sidebar)
    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SUBTITLE);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(14, 24, 14, 24));
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(PRIMARY)) {
                    btn.setBackground(BG_SIDEBAR_HOVER);
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(PRIMARY)) {
                    btn.setBackground(BG_SIDEBAR);
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });
        return btn;
    }

    // 3. Hàm làm đẹp JTable (Cao thoáng, xen kẽ màu dòng, Header nổi bật)
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(203, 213, 225)));

        // Căn lề và tô màu xen kẽ các dòng (Zebra stripes)
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    // 4. Hàm gắn nhãn màu (Badge) cho cột Trạng thái hoặc Loại sách
    public static void setBadgeRenderer(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String text = value != null ? value.toString() : "";
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));

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
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                return label;
            }
        });
    }

    // 5. Hàm làm đẹp ô nhập liệu (JTextField)
    public static void styleTextField(JTextField txt) {
        txt.setFont(FONT_BODY);
        txt.setPreferredSize(new Dimension(220, 36));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    // 6. Hàm làm đẹp JComboBox (Đồng bộ phong cách phẳng)
    public static void styleComboBox(JComboBox<?> cbo) {
        cbo.setFont(FONT_BODY);
        cbo.setBackground(Color.WHITE);
        cbo.setForeground(TEXT_DARK);
        cbo.setPreferredSize(new Dimension(155, 36));
        cbo.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true));
        cbo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // 7. Khung Panel trắng bo góc (Dùng để bọc các vùng tìm kiếm hoặc bảng)
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return panel;
    }
}
