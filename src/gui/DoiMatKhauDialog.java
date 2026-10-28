package gui;

import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại đổi mật khẩu, dùng chung cho menu Tài khoản và Trang cá nhân. */
public final class DoiMatKhauDialog {
    private DoiMatKhauDialog() { }

    public static void hien(Component cha, QuanLyThuVien ql, TaiKhoan taiKhoan) {
        JPasswordField txtCu = new JPasswordField(15);
        JPasswordField txtMoi = new JPasswordField(15);
        JPasswordField txtNhapLai = new JPasswordField(15);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Mật khẩu hiện tại:"));
        form.add(txtCu);
        form.add(new JLabel("Mật khẩu mới (từ 6 ký tự):"));
        form.add(txtMoi);
        form.add(new JLabel("Nhập lại mật khẩu mới:"));
        form.add(txtNhapLai);

        // Lặp lại để khi lỗi vẫn giữ nguyên nội dung đã gõ
        while (true) {
            int kq = JOptionPane.showConfirmDialog(cha, form, "Đổi mật khẩu",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            String mkMoi = new String(txtMoi.getPassword());
            if (!mkMoi.equals(new String(txtNhapLai.getPassword()))) {
                JOptionPane.showMessageDialog(cha, "Mật khẩu mới nhập lại không khớp");
                txtNhapLai.setText("");
                continue;
            }
            try {
                ql.doiMatKhau(taiKhoan, new String(txtCu.getPassword()), mkMoi);
                try {
                    ql.luuFile();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(cha, "Đã đổi mật khẩu nhưng chưa lưu được vào file: " + ex.getMessage());
                }
                JOptionPane.showMessageDialog(cha, "Đổi mật khẩu thành công");
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(cha, ex.getMessage());
            }
        }
    }
}
