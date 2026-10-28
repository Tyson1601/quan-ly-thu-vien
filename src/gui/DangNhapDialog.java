package gui;

import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import java.awt.*;

/** Màn hình đăng nhập. hienThi() trả về TaiKhoan đã đăng nhập (hoặc vừa đăng ký), null nếu đóng cửa sổ. */
public class DangNhapDialog extends JDialog {
    static final Color XANH = new Color(24, 119, 242);

    private final QuanLyThuVien ql;
    private final JTextField txtTen = new JTextField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JLabel lblLoi = new JLabel(" ");
    private TaiKhoan ketQua = null;

    public DangNhapDialog(Window owner, QuanLyThuVien ql) {
        super(owner, "Thư viện - Đăng nhập", ModalityType.APPLICATION_MODAL);
        this.ql = ql;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel tieuDe = new JLabel("Thư viện");
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD, 30f));
        tieuDe.setForeground(XANH);
        JLabel phu = new JLabel("Đăng nhập để mượn và quản lý sách");
        phu.setForeground(Color.GRAY);

        txtTen.setPreferredSize(new Dimension(280, 32));
        txtMatKhau.setPreferredSize(new Dimension(280, 32));
        char kyTuAn = txtMatKhau.getEchoChar();
        JCheckBox chkHien = new JCheckBox("Hiện mật khẩu");
        chkHien.addActionListener(e -> txtMatKhau.setEchoChar(chkHien.isSelected() ? (char) 0 : kyTuAn));

        lblLoi.setForeground(new Color(200, 30, 30));

        JButton btnDangNhap = new JButton("Đăng nhập");
        btnDangNhap.setBackground(XANH);
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setOpaque(true);
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setFont(btnDangNhap.getFont().deriveFont(Font.BOLD, 14f));
        btnDangNhap.setPreferredSize(new Dimension(280, 38));

        JButton btnTaoTaiKhoan = new JButton("Tạo tài khoản mới");
        kieuLienKet(btnTaoTaiKhoan);

        JPanel nen = new JPanel(new GridBagLayout());
        nen.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        int y = 0;
        them(nen, tieuDe, y++, 0);
        them(nen, phu, y++, 2);
        them(nen, new JLabel("Tên đăng nhập"), y++, 18);
        them(nen, txtTen, y++, 4);
        them(nen, new JLabel("Mật khẩu"), y++, 10);
        them(nen, txtMatKhau, y++, 4);
        them(nen, chkHien, y++, 4);
        them(nen, lblLoi, y++, 6);
        them(nen, btnDangNhap, y++, 4);
        them(nen, new JSeparator(), y++, 18);
        JPanel chuaCoTk = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        chuaCoTk.add(new JLabel("Chưa có tài khoản?"));
        chuaCoTk.add(btnTaoTaiKhoan);
        them(nen, chuaCoTk, y++, 10);
        setContentPane(nen);

        btnDangNhap.addActionListener(e -> dangNhap());
        btnTaoTaiKhoan.addActionListener(e -> taoTaiKhoan());
        getRootPane().setDefaultButton(btnDangNhap);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    static void kieuLienKet(JButton b) {
        b.setForeground(XANH);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void them(JPanel p, Component c, int y, int cachTren) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = y;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(cachTren, 0, 0, 0);
        p.add(c, g);
    }

    private void baoLoi(String noiDung) { lblLoi.setText(noiDung); }

    private void dangNhap() {
        String ten = txtTen.getText().trim();
        String mk = new String(txtMatKhau.getPassword());
        if (ten.isEmpty() || mk.isEmpty()) {
            baoLoi("Hãy nhập tên đăng nhập và mật khẩu");
            return;
        }
        try {
            ketQua = ql.dangNhap(ten, mk);
            dispose();
        } catch (Exception ex) {   // SaiDangNhapException
            baoLoi(ex.getMessage());
            txtMatKhau.setText("");
            txtMatKhau.requestFocusInWindow();
        }
    }

    /** Mở form đăng ký; đăng ký xong thì đăng nhập luôn (giống mạng xã hội). */
    private void taoTaiKhoan() {
        TaiKhoan moi = new DangKyDialog(this, ql).hienThi();
        if (moi != null) {
            ketQua = moi;
            dispose();
        }
    }

    public TaiKhoan hienThi() {
        setVisible(true);
        return ketQua;
    }
}