package gui;

import model.DocGia;
import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QuanLyThuVien ql;
    private final TaiKhoan taiKhoan;
    private final Runnable khiDangXuat;
    private final JLabel lblNgay = new JLabel();
    private final JLabel lblChao = new JLabel();

    public MainFrame(QuanLyThuVien ql, TaiKhoan taiKhoan, Runnable khiDangXuat) {
        super("Hệ Thống Quản Lý Thư Viện");
        this.ql = ql;
        this.taiKhoan = taiKhoan;
        this.khiDangXuat = khiDangXuat;
        boolean laAdmin = taiKhoan.laAdmin();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Khởi tạo các panel chức năng
        SachPanel sachPanel = new SachPanel(ql, laAdmin);
        DocGiaPanel docGiaPanel = laAdmin ? new DocGiaPanel(ql, true) : null;
        MuonTraPanel muonTraPanel = new MuonTraPanel(ql, taiKhoan);
        ThongKePanel thongKePanel = laAdmin ? new ThongKePanel(ql) : null;
        ThoiGianPanel thoiGianPanel = laAdmin ? new ThoiGianPanel(ql, this::capNhatNgay) : null;
        TrangCaNhanPanel trangCaNhanPanel = laAdmin ? null : new TrangCaNhanPanel(ql, taiKhoan, this::capNhatChao);

        // 1. SIDEBAR BÊN TRÁI
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, getHeight()));

        // Logo / Tên ứng dụng ở góc trên Sidebar
        JLabel lblLogo = new JLabel("THƯ VIỆN SỐ");
        lblLogo.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 20));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setBorder(new EmptyBorder(25, 24, 25, 24));
        lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblLogo);

        // Danh sách nút điều hướng
        List<JButton> navButtons = new ArrayList<>();
        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIStyle.BG_MAIN);

        // Nút Quản lý Sách
        JButton btnMenuSach = UIStyle.createNavButton(laAdmin ? "Quản lý Sách" : "Tra cứu Sách");
        btnMenuSach.setAlignmentX(Component.LEFT_ALIGNMENT);
        navButtons.add(btnMenuSach);
        sidebar.add(btnMenuSach);
        contentPanel.add(sachPanel, "SACH");

        // Nút Quản lý Độc giả (chỉ admin)
        JButton btnMenuDocGia = null;
        if (docGiaPanel != null) {
            btnMenuDocGia = UIStyle.createNavButton("Quản lý Độc giả");
            btnMenuDocGia.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButtons.add(btnMenuDocGia);
            sidebar.add(btnMenuDocGia);
            contentPanel.add(docGiaPanel, "DOCGIA");
        }

        // Nút Mượn - Trả sách
        JButton btnMenuMuonTra = UIStyle.createNavButton(laAdmin ? "Mượn - Trả sách" : "Phiếu mượn");
        btnMenuMuonTra.setAlignmentX(Component.LEFT_ALIGNMENT);
        navButtons.add(btnMenuMuonTra);
        sidebar.add(btnMenuMuonTra);
        contentPanel.add(muonTraPanel, "MUONTRA");

        // Nút Thống kê (chỉ admin)
        JButton btnMenuThongKe = null;
        if (thongKePanel != null) {
            btnMenuThongKe = UIStyle.createNavButton("Thống kê & Báo cáo");
            btnMenuThongKe.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButtons.add(btnMenuThongKe);
            sidebar.add(btnMenuThongKe);
            contentPanel.add(thongKePanel, "THONGKE");
        }

        // Nút Thời gian hệ thống (chỉ admin)
        JButton btnMenuThoiGian = null;
        if (thoiGianPanel != null) {
            btnMenuThoiGian = UIStyle.createNavButton("Thời gian hệ thống");
            btnMenuThoiGian.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButtons.add(btnMenuThoiGian);
            sidebar.add(btnMenuThoiGian);
            contentPanel.add(thoiGianPanel, "THOIGIAN");
        }

        // Nút Trang cá nhân (chỉ độc giả)
        JButton btnMenuCaNhan = null;
        if (trangCaNhanPanel != null) {
            btnMenuCaNhan = UIStyle.createNavButton("Trang cá nhân");
            btnMenuCaNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButtons.add(btnMenuCaNhan);
            sidebar.add(btnMenuCaNhan);
            contentPanel.add(trangCaNhanPanel, "CANHAN");
        }

        sidebar.add(Box.createVerticalGlue());

        // 2. KHU VỰC NỘI DUNG BÊN PHẢI (Chứa Header Thời gian + CardLayout)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(UIStyle.BG_MAIN);

        // Header TopBar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Góc trái TopBar: Ngày hệ thống
        lblNgay.setFont(UIStyle.FONT_BODY);
        topBar.add(lblNgay, BorderLayout.WEST);

        // Góc phải TopBar: Lời chào + Nút Đổi mật khẩu + Nút Đăng xuất
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightTop.setOpaque(false);

        lblChao.setFont(UIStyle.FONT_BOLD);
        lblChao.setForeground(UIStyle.TEXT_DARK);
        rightTop.add(lblChao);

        JButton btnDoiMatKhau = UIStyle.createButton("Đổi mật khẩu", new Color(100, 116, 139));
        JButton btnDangXuat = UIStyle.createButton("Đăng xuất", UIStyle.DANGER);
        btnDoiMatKhau.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 12));
        btnDangXuat.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 12));
        btnDoiMatKhau.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnDangXuat.setBorder(new EmptyBorder(6, 14, 6, 14));

        rightTop.add(btnDoiMatKhau);
        rightTop.add(btnDangXuat);
        topBar.add(rightTop, BorderLayout.EAST);

        rightContainer.add(topBar, BorderLayout.NORTH);
        rightContainer.add(contentPanel, BorderLayout.CENTER);

        // Hàm chuyển tab kèm hiệu ứng sáng nút đang chọn
        Runnable resetNav = () -> {
            for (JButton b : navButtons) {
                b.setBackground(UIStyle.BG_SIDEBAR);
                b.setForeground(new Color(203, 213, 225));
            }
        };

        btnMenuSach.addActionListener(e -> {
            resetNav.run();
            btnMenuSach.setBackground(UIStyle.PRIMARY);
            btnMenuSach.setForeground(Color.WHITE);
            cardLayout.show(contentPanel, "SACH");
            sachPanel.napLai();
            capNhatNgay();
        });

        if (btnMenuDocGia != null) {
            final JButton btnDocGiaRef = btnMenuDocGia;
            btnDocGiaRef.addActionListener(e -> {
                resetNav.run();
                btnDocGiaRef.setBackground(UIStyle.PRIMARY);
                btnDocGiaRef.setForeground(Color.WHITE);
                cardLayout.show(contentPanel, "DOCGIA");
                docGiaPanel.napLai();
                capNhatNgay();
            });
        }

        btnMenuMuonTra.addActionListener(e -> {
            resetNav.run();
            btnMenuMuonTra.setBackground(UIStyle.PRIMARY);
            btnMenuMuonTra.setForeground(Color.WHITE);
            cardLayout.show(contentPanel, "MUONTRA");
            muonTraPanel.napLai();
            capNhatNgay();
        });

        if (btnMenuThongKe != null) {
            final JButton btnThongKeRef = btnMenuThongKe;
            btnThongKeRef.addActionListener(e -> {
                resetNav.run();
                btnThongKeRef.setBackground(UIStyle.PRIMARY);
                btnThongKeRef.setForeground(Color.WHITE);
                cardLayout.show(contentPanel, "THONGKE");
                thongKePanel.napLai();
                capNhatNgay();
            });
        }

        if (btnMenuThoiGian != null) {
            final JButton btnThoiGianRef = btnMenuThoiGian;
            btnThoiGianRef.addActionListener(e -> {
                resetNav.run();
                btnThoiGianRef.setBackground(UIStyle.PRIMARY);
                btnThoiGianRef.setForeground(Color.WHITE);
                cardLayout.show(contentPanel, "THOIGIAN");
                thoiGianPanel.napLai();
                capNhatNgay();
            });
        }

        if (btnMenuCaNhan != null) {
            final JButton btnCaNhanRef = btnMenuCaNhan;
            btnCaNhanRef.addActionListener(e -> {
                resetNav.run();
                btnCaNhanRef.setBackground(UIStyle.PRIMARY);
                btnCaNhanRef.setForeground(Color.WHITE);
                cardLayout.show(contentPanel, "CANHAN");
                trangCaNhanPanel.napLai();
                capNhatNgay();
            });
        }

        btnDoiMatKhau.addActionListener(e -> DoiMatKhauDialog.hien(this, ql, taiKhoan));
        btnDangXuat.addActionListener(e -> dangXuat());

        // Kích hoạt tab đầu tiên mặc định
        btnMenuSach.doClick();

        add(sidebar, BorderLayout.WEST);
        add(rightContainer, BorderLayout.CENTER);

        capNhatChao();
        capNhatNgay();

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

    /** Cập nhật dòng "Ngày hệ thống" ở góc trên (đỏ khi đang giả lập). */
    private void capNhatNgay() {
        String chu = "Ngày hệ thống: " + ql.ngayHienTai().format(NGAY);
        if (ql.dangGiaLapNgay()) {
            chu += "   (ĐANG GIẢ LẬP - ngày thật: " + LocalDate.now().format(NGAY) + ")";
            lblNgay.setForeground(UIStyle.DANGER);
            lblNgay.setFont(UIStyle.FONT_BOLD);
        } else {
            lblNgay.setForeground(UIStyle.TEXT_MUTED);
            lblNgay.setFont(UIStyle.FONT_BODY);
        }
        lblNgay.setText(chu);
    }
}
