package gui;

import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import java.awt.*;

/** Form tạo tài khoản mới. Tạo xong sẽ có luôn hồ sơ độc giả gắn với tài khoản. */
public class DangKyDialog extends JDialog {
    private final QuanLyThuVien ql;
    private final JTextField txtHoTen = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JPasswordField txtMk1 = new JPasswordField();
    private final JPasswordField txtMk2 = new JPasswordField();
    private final JLabel lblLoi = new JLabel(" ");
    private TaiKhoan ketQua = null;

    public DangKyDialog(Window owner, QuanLyThuVien ql) {
        super(owner, "Thư viện - Tạo tài khoản", ModalityType.APPLICATION_MODAL);
        this.ql = ql;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel tieuDe = new JLabel("Tạo tài khoản mới");
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD, 24f));
        tieuDe.setForeground(DangNhapDialog.XANH);
        JLabel phu = new JLabel("Mỗi tài khoản có hồ sơ độc giả riêng để mượn sách");
        phu.setForeground(Color.GRAY);

        char kyTuAn = txtMk1.getEchoChar();
        JCheckBox chkHien = new JCheckBox("Hiện mật khẩu");
        chkHien.addActionListener(e -> {
            char c = chkHien.isSelected() ? (char) 0 : kyTuAn;
            txtMk1.setEchoChar(c);
            txtMk2.setEchoChar(c);
        });
        lblLoi.setForeground(new Color(200, 30, 30));

        JButton btnDangKy = new JButton("Đăng ký");
        btnDangKy.setBackground(DangNhapDialog.XANH);
        btnDangKy.setForeground(Color.WHITE);
        btnDangKy.setOpaque(true);
        btnDangKy.setBorderPainted(false);
        btnDangKy.setFont(btnDangKy.getFont().deriveFont(Font.BOLD, 14f));
        btnDangKy.setPreferredSize(new Dimension(300, 38));

        JButton btnDaCoTaiKhoan = new JButton("Đăng nhập");
        DangNhapDialog.kieuLienKet(btnDaCoTaiKhoan);

        JPanel nen = new JPanel(new GridBagLayout());
        nen.setBorder(BorderFactory.createEmptyBorder(22, 32, 22, 32));
        int y = 0;
        them(nen, tieuDe, y++, 0);
        them(nen, phu, y++, 2);
        y = truong(nen, "Họ và tên", txtHoTen, y, 14);
        y = truong(nen, "Tên đăng nhập (3-20 ký tự: chữ không dấu, số, _)", txtTen, y, 8);
        y = truong(nen, "Email (không bắt buộc)", txtEmail, y, 8);
        y = truong(nen, "Số điện thoại (không bắt buộc)", txtSdt, y, 8);
        y = truong(nen, "Mật khẩu (từ 6 ký tự)", txtMk1, y, 8);
        y = truong(nen, "Nhập lại mật khẩu", txtMk2, y, 8);
        them(nen, chkHien, y++, 4);
        them(nen, lblLoi, y++, 6);
        them(nen, btnDangKy, y++, 4);
        JPanel daCoTk = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        daCoTk.add(new JLabel("Đã có tài khoản?"));
        daCoTk.add(btnDaCoTaiKhoan);
        them(nen, daCoTk, y++, 12);
        setContentPane(nen);

        btnDangKy.addActionListener(e -> dangKy());
        btnDaCoTaiKhoan.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnDangKy);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private static int truong(JPanel p, String nhan, JComponent o, int y, int cachTren) {
        them(p, new JLabel(nhan), y++, cachTren);
        o.setPreferredSize(new Dimension(300, 30));
        them(p, o, y++, 3);
        return y;
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

    private void dangKy() {
        String mk1 = new String(txtMk1.getPassword());
        String mk2 = new String(txtMk2.getPassword());
        if (!mk1.equals(mk2)) {
            lblLoi.setText("Mật khẩu nhập lại không khớp");
            txtMk2.setText("");
            return;
        }
        try {
            TaiKhoan moi = ql.dangKy(txtHoTen.getText(), txtSdt.getText(), txtEmail.getText(),
                    txtTen.getText(), mk1);
            try {
                ql.luuFile();   // lưu ngay để lần sau mở lại vẫn đăng nhập được
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Đã tạo tài khoản nhưng chưa lưu được vào file: " + ex.getMessage());
            }
            ketQua = moi;
            dispose();
        } catch (Exception ex) {   // tên đăng nhập trùng, sai định dạng...
            lblLoi.setText(ex.getMessage());
        }
    }

    public TaiKhoan hienThi() {
        setVisible(true);
        return ketQua;
    }
}
