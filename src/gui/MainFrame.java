package gui;

import model.DocGia;
import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // DUY NHẤT một QuanLyThuVien cho cả ứng dụng (tạo ở Main, truyền xuống các panel)
    private final QuanLyThuVien ql;
    private final TaiKhoan taiKhoan;
    private final Runnable khiDangXuat;
    private final JLabel lblNgay = new JLabel();
    private final JLabel lblChao = new JLabel();

    public MainFrame(QuanLyThuVien ql, TaiKhoan taiKhoan, Runnable khiDangXuat) {
        super("Quản lý thư viện");
        this.ql = ql;
        this.taiKhoan = taiKhoan;
        this.khiDangXuat = khiDangXuat;
        boolean laAdmin = taiKhoan.laAdmin();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 640);
        setLocationRelativeTo(null);

        // Admin: Sách, Độc giả, Mượn/Trả, Thống kê, Thời gian
        // Người dùng: Sách (chỉ xem), Mượn/Trả (của mình), Trang cá nhân
        SachPanel sachPanel = new SachPanel(ql, laAdmin);
        DocGiaPanel docGiaPanel = laAdmin ? new DocGiaPanel(ql, true) : null;
        MuonTraPanel muonTraPanel = new MuonTraPanel(ql, taiKhoan);
        ThongKePanel thongKePanel = laAdmin ? new ThongKePanel(ql) : null;
        ThoiGianPanel thoiGianPanel = laAdmin ? new ThoiGianPanel(ql, this::capNhatNgay) : null;
        TrangCaNhanPanel trangCaNhanPanel = laAdmin ? null : new TrangCaNhanPanel(ql, taiKhoan, this::capNhatChao);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sách", sachPanel);
        if (docGiaPanel != null) tabs.addTab("Độc giả", docGiaPanel);
        tabs.addTab("Mượn/Trả", muonTraPanel);
        if (thongKePanel != null) tabs.addTab("Thống kê", thongKePanel);
        if (thoiGianPanel != null) tabs.addTab("Thời gian", thoiGianPanel);
        if (trangCaNhanPanel != null) tabs.addTab("Trang cá nhân", trangCaNhanPanel);

        // Dữ liệu dùng chung nên mỗi lần chuyển tab thì nạp lại tab đó
        tabs.addChangeListener(e -> {
            Component tab = tabs.getSelectedComponent();
            if (tab == sachPanel) sachPanel.napLai();
            else if (tab == docGiaPanel) docGiaPanel.napLai();
            else if (tab == muonTraPanel) muonTraPanel.napLai();
            else if (tab == thongKePanel) thongKePanel.napLai();
            else if (tab == thoiGianPanel) thoiGianPanel.napLai();
            else if (tab == trangCaNhanPanel) trangCaNhanPanel.napLai();
            capNhatNgay();
        });
        add(tabs, BorderLayout.CENTER);

        // Thanh trạng thái: ngày hệ thống đang dùng để tính hạn trả và tiền phạt
        JPanel thanhTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thanhTrangThai.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        thanhTrangThai.add(lblNgay);
        add(thanhTrangThai, BorderLayout.SOUTH);
        capNhatNgay();

        // Menu Tài khoản + lời chào (giống góc phải của mạng xã hội)
        JMenuItem miDoiMatKhau = new JMenuItem("Đổi mật khẩu...");
        JMenuItem miDangXuat = new JMenuItem("Đăng xuất");
        JMenu menuTaiKhoan = new JMenu("Tài khoản");
        menuTaiKhoan.add(miDoiMatKhau);
        menuTaiKhoan.addSeparator();
        menuTaiKhoan.add(miDangXuat);
        JMenuBar thanhMenu = new JMenuBar();
        thanhMenu.add(menuTaiKhoan);
        thanhMenu.add(Box.createHorizontalGlue());
        lblChao.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        thanhMenu.add(lblChao);
        JButton btnDangXuat = new JButton("Đăng xuất");   // nút hiện sẵn ở góc phải, ngoài menu
        DangNhapDialog.kieuLienKet(btnDangXuat);
        btnDangXuat.setMargin(new Insets(2, 8, 2, 8));
        thanhMenu.add(btnDangXuat);
        setJMenuBar(thanhMenu);
        capNhatChao();

        miDoiMatKhau.addActionListener(e -> DoiMatKhauDialog.hien(this, ql, taiKhoan));
        miDangXuat.addActionListener(e -> dangXuat());
        btnDangXuat.addActionListener(e -> dangXuat());

        // Lưu dữ liệu khi đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { luu(); }
        });
    }

    private void luu() {
        try {
            ql.luuFile();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không lưu được dữ liệu: " + ex.getMessage());
        }
    }

    /** Lưu, đóng cửa sổ này rồi quay về màn hình đăng nhập. */
    private void dangXuat() {
        luu();
        dispose();
        khiDangXuat.run();
    }

    private void capNhatChao() {
        String ten;
        if (taiKhoan.laAdmin()) {
            ten = "Quản trị viên";
        } else {
            DocGia d = ql.layDocGiaCuaTaiKhoan(taiKhoan);
            ten = (d != null) ? d.getTen() : taiKhoan.getTenDangNhap();
        }
        lblChao.setText("Xin chào, " + ten);
    }

    /** Cập nhật dòng "Ngày hệ thống" ở góc dưới (đỏ khi đang giả lập). */
    private void capNhatNgay() {
        String chu = "Ngày hệ thống: " + ql.ngayHienTai().format(NGAY);
        if (ql.dangGiaLapNgay()) {
            chu += "   (ĐANG GIẢ LẬP - ngày thật: " + LocalDate.now().format(NGAY) + ")";
            lblNgay.setForeground(new Color(176, 0, 32));
        } else {
            lblNgay.setForeground(UIManager.getColor("Label.foreground"));
        }
        lblNgay.setText(chu);
    }
}