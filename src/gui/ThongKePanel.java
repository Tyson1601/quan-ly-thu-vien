package gui;

import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class ThongKePanel extends JPanel {
    private final QuanLyThuVien ql;
    private JLabel lblTongSach, lblTongDocGia, lblDangMuon, lblQuaHan;
    private int soGiaoTrinh = 45, soTapChi = 25, soTruyenTranh = 30;
    private int phieuDungHan = 68, phieuDangMuon = 24, phieuQuaHan = 8;
    private JPanel chartPanel;

    public ThongKePanel(QuanLyThuVien ql) {
        this.ql = ql;
        setLayout(new BorderLayout(20, 20));
        setBackground(UIStyle.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Tiêu đề trang
        JLabel lblHeader = new JLabel("Tổng quan & Thống kê Thư viện");
        lblHeader.setFont(UIStyle.FONT_TITLE);
        lblHeader.setForeground(UIStyle.TEXT_DARK);
        add(lblHeader, BorderLayout.NORTH);

        // 2. Khu vực chính: Trên là 4 thẻ KPI, dưới là Biểu đồ
        JPanel centerPanel = new JPanel(new BorderLayout(20, 20));
        centerPanel.setOpaque(false);

        // --- Hàng 4 thẻ KPI ---
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setOpaque(false);

        lblTongSach = new JLabel("0");
        lblTongDocGia = new JLabel("0");
        lblDangMuon = new JLabel("0");
        lblQuaHan = new JLabel("0");

        kpiGrid.add(createKpiCard("TỔNG ĐẦU SÁCH", lblTongSach, UIStyle.PRIMARY));
        kpiGrid.add(createKpiCard("ĐỘC GIẢ", lblTongDocGia, UIStyle.SUCCESS));
        kpiGrid.add(createKpiCard("ĐANG MƯỢN", lblDangMuon, UIStyle.WARNING));
        kpiGrid.add(createKpiCard("QUÁ HẠN", lblQuaHan, UIStyle.DANGER));

        centerPanel.add(kpiGrid, BorderLayout.NORTH);

        // --- Khu vực vẽ Biểu đồ ---
        chartPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        chartPanel.setOpaque(false);
        chartPanel.add(createBarChartCard());
        chartPanel.add(createStatusRatioCard());

        centerPanel.add(chartPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        napLai();
    }

    public ThongKePanel() {
        this(null);
    }

    // Hàm gọi để cập nhật số liệu thực tế từ QuanLyThuVien của bạn
    public void capNhatSoLieu(int tongSach, int tongDocGia, int dangMuon, int quaHan,
                              int giaoTrinh, int tapChi, int truyenTranh, int daTraDungHan) {
        lblTongSach.setText(String.valueOf(tongSach));
        lblTongDocGia.setText(String.valueOf(tongDocGia));
        lblDangMuon.setText(String.valueOf(dangMuon));
        lblQuaHan.setText(String.valueOf(quaHan));

        this.soGiaoTrinh = giaoTrinh;
        this.soTapChi = tapChi;
        this.soTruyenTranh = truyenTranh;
        this.phieuDungHan = daTraDungHan;
        this.phieuDangMuon = dangMuon;
        this.phieuQuaHan = quaHan;
        repaint();
    }

    /** Nạp lại số liệu thực tế từ service */
    public void napLai() {
        if (ql == null) return;
        int tongSach = ql.layTatCa().size();
        int tongDocGia = ql.layTatCaDocGia().size();
        int quaHan = ql.layPhieuQuaHan().size();
        int dangMuon = ql.layPhieuChuaTra().size();

        Map<String, Long> theoLoai = ql.thongKeTheoLoai();
        int giaoTrinh = theoLoai.getOrDefault("Giáo trình", 0L).intValue();
        int tapChi = theoLoai.getOrDefault("Tạp chí", 0L).intValue();
        int truyenTranh = theoLoai.getOrDefault("Truyện tranh", 0L).intValue();

        int daTraDungHan = (int) ql.layTatCaPhieu().stream()
                .filter(p -> p.daTra() && p.soNgayTre() == 0).count();

        capNhatSoLieu(tongSach, tongDocGia, dangMuon, quaHan,
                giaoTrinh, tapChi, truyenTranh, daTraDungHan);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UIStyle.createCardPanel(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(200, 110));

        // Thanh màu nhấn bên trái
        JPanel leftBar = new JPanel();
        leftBar.setPreferredSize(new Dimension(5, 0));
        leftBar.setBackground(accentColor);

        JPanel content = new JPanel(new GridLayout(2, 1));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 12));
        lblTitle.setForeground(UIStyle.TEXT_MUTED);

        valueLabel.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 30));
        valueLabel.setForeground(accentColor);

        content.add(lblTitle);
        content.add(valueLabel);

        card.add(leftBar, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // Biểu đồ cột: Phân bố theo loại sách (Giáo trình, Tạp chí, Truyện tranh)
    private JPanel createBarChartCard() {
        JPanel card = UIStyle.createCardPanel(new BorderLayout());
        JLabel title = new JLabel("Phân bố Sách theo Thể loại");
        title.setFont(UIStyle.FONT_SUBTITLE);
        title.setForeground(UIStyle.TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth();
                int h = getHeight() - 40;
                int max = Math.max(1, Math.max(soGiaoTrinh, Math.max(soTapChi, soTruyenTranh)));

                String[] labels = {"Giáo trình", "Tạp chí", "Truyện tranh"};
                int[] values = {soGiaoTrinh, soTapChi, soTruyenTranh};
                Color[] colors = {UIStyle.PRIMARY, UIStyle.WARNING, UIStyle.SUCCESS};

                int barWidth = Math.min(70, Math.max(20, w / 6));
                int spacing = Math.max(10, (w - (barWidth * 3)) / 4);

                for (int i = 0; i < 3; i++) {
                    int barHeight = max > 0 ? (int) ((values[i] * 1.0 / max) * Math.max(10, h - 40)) : 0;
                    int x = spacing + i * (barWidth + spacing);
                    int y = h - barHeight;

                    // Vẽ cột bo góc
                    if (barHeight > 0) {
                        g2.setColor(colors[i]);
                        g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);
                    }

                    // Vẽ con số trên đỉnh cột
                    g2.setColor(UIStyle.TEXT_DARK);
                    g2.setFont(UIStyle.FONT_BOLD);
                    String valStr = String.valueOf(values[i]);
                    int strW = g2.getFontMetrics().stringWidth(valStr);
                    g2.drawString(valStr, x + (barWidth - strW) / 2, y - 8);

                    // Vẽ nhãn dưới đáy
                    g2.setColor(UIStyle.TEXT_MUTED);
                    g2.setFont(UIStyle.FONT_BODY);
                    int lblW = g2.getFontMetrics().stringWidth(labels[i]);
                    g2.drawString(labels[i], x + (barWidth - lblW) / 2, h + 22);
                }
                g2.dispose();
            }
        };
        canvas.setOpaque(false);
        card.add(canvas, BorderLayout.CENTER);
        return card;
    }

    // Biểu đồ thanh ngang: Tỷ lệ phiếu mượn trả
    private JPanel createStatusRatioCard() {
        JPanel card = UIStyle.createCardPanel(new BorderLayout());
        JLabel title = new JLabel("Tỷ lệ Trạng thái Phiếu mượn");
        title.setFont(UIStyle.FONT_SUBTITLE);
        title.setForeground(UIStyle.TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int total = Math.max(1, phieuDungHan + phieuDangMuon + phieuQuaHan);
                String[] labels = {"Đã trả đúng hạn", "Đang mượn", "Quá hạn trả"};
                int[] values = {phieuDungHan, phieuDangMuon, phieuQuaHan};
                Color[] colors = {UIStyle.SUCCESS, UIStyle.WARNING, UIStyle.DANGER};

                int w = Math.max(50, getWidth() - 30);
                int startY = 25;

                for (int i = 0; i < 3; i++) {
                    int percent = (int) Math.round((values[i] * 100.0) / total);
                    int y = startY + i * 70;

                    g2.setFont(UIStyle.FONT_BOLD);
                    g2.setColor(UIStyle.TEXT_DARK);
                    g2.drawString(labels[i] + " (" + values[i] + " phiếu - " + percent + "%)", 10, y);

                    // Khung nền xám
                    g2.setColor(new Color(226, 232, 240));
                    g2.fillRoundRect(10, y + 10, w, 20, 10, 10);

                    // Thanh màu tỷ lệ
                    int fillW = values[i] == 0 ? 0 : Math.max(10, (int) ((values[i] * 1.0 / total) * w));
                    if (fillW > 0) {
                        g2.setColor(colors[i]);
                        g2.fillRoundRect(10, y + 10, fillW, 20, 10, 10);
                    }
                }
                g2.dispose();
            }
        };
        canvas.setOpaque(false);
        card.add(canvas, BorderLayout.CENTER);
        return card;
    }
}
