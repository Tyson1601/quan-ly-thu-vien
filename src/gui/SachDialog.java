package gui;

import model.Sach;
import model.SachGiaoTrinh;
import model.TapChi;
import model.TruyenTranh;
import service.QuanLyThuVien;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại thêm sách (sachSua == null) hoặc sửa sách (sachSua != null). */
public class SachDialog extends JDialog {
    private static final String[] LOAI = {"Truyện tranh", "Giáo trình", "Tạp chí"};
    private static final int TRUYEN_TRANH = 0, GIAO_TRINH = 1, TAP_CHI = 2;

    private final QuanLyThuVien ql;
    private final boolean cheDoSua;

    private final JTextField txtMa = new JTextField(20);
    private final JTextField txtTen = new JTextField(20);
    private final JTextField txtSoLuong = new JTextField(20);
    private final JComboBox<String> cboLoai = new JComboBox<>(LOAI);
    private final JLabel lblMonHoc = new JLabel("Môn học:");
    private final JTextField txtMonHoc = new JTextField(20);
    private final JLabel lblSoPhatHanh = new JLabel("Số phát hành:");
    private final JTextField txtSoPhatHanh = new JTextField(20);

    public SachDialog(Window owner, QuanLyThuVien ql, Sach sachSua) {
        super(owner, sachSua == null ? "Thêm sách" : "Sửa sách", ModalityType.APPLICATION_MODAL);
        this.ql = ql;
        this.cheDoSua = (sachSua != null);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        themDong(form, 0, new JLabel("Mã:"), txtMa);
        themDong(form, 1, new JLabel("Tên:"), txtTen);
        themDong(form, 2, new JLabel("Số lượng:"), txtSoLuong);
        themDong(form, 3, new JLabel("Loại:"), cboLoai);
        themDong(form, 4, lblMonHoc, txtMonHoc);
        themDong(form, 5, lblSoPhatHanh, txtSoPhatHanh);

        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nut.add(btnLuu);
        nut.add(btnHuy);

        add(form, BorderLayout.CENTER);
        add(nut, BorderLayout.SOUTH);

        cboLoai.addActionListener(e -> capNhatOTheoLoai());
        btnLuu.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLuu);

        if (cheDoSua) {
            napDuLieuSua(sachSua);
        }
        capNhatOTheoLoai();
        setLocationRelativeTo(owner);
    }

    private void themDong(JPanel form, int dong, JLabel nhan, JComponent o) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = dong;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        form.add(nhan, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(o, c);
    }

    /** Sửa chỉ đổi được tên + số lượng: khóa mã, loại và các ô riêng của loại. */
    private void napDuLieuSua(Sach s) {
        txtMa.setText(s.getMa());
        txtMa.setEditable(false);
        txtTen.setText(s.getTen());
        txtSoLuong.setText(String.valueOf(s.getSoLuong()));

        cboLoai.setEnabled(false);
        txtMonHoc.setEditable(false);
        txtSoPhatHanh.setEditable(false);
        if (s instanceof SachGiaoTrinh) {
            cboLoai.setSelectedIndex(GIAO_TRINH);
            txtMonHoc.setText(((SachGiaoTrinh) s).getMonHoc());
        } else if (s instanceof TapChi) {
            cboLoai.setSelectedIndex(TAP_CHI);
            txtSoPhatHanh.setText(String.valueOf(((TapChi) s).getSoPhatHanh()));
        } else {
            cboLoai.setSelectedIndex(TRUYEN_TRANH);
        }
    }

    private void capNhatOTheoLoai() {
        int loai = cboLoai.getSelectedIndex();
        lblMonHoc.setVisible(loai == GIAO_TRINH);
        txtMonHoc.setVisible(loai == GIAO_TRINH);
        lblSoPhatHanh.setVisible(loai == TAP_CHI);
        txtSoPhatHanh.setVisible(loai == TAP_CHI);
        pack();
    }

    /** Đọc số nguyên từ ô nhập; sai định dạng thì báo lỗi và trả về null. */
    private Integer docSo(JTextField o, String tenTruong) {
        try {
            return Integer.parseInt(o.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, tenTruong + " phải là số nguyên");
            return null;
        }
    }

    private void luu() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã và tên không được để trống");
            return;
        }
        Integer soLuong = docSo(txtSoLuong, "Số lượng");
        if (soLuong == null) return;
        if (soLuong < 0) {
            JOptionPane.showMessageDialog(this, "Số lượng không được âm");
            return;
        }

        try {
            if (cheDoSua) {
                ql.sua(ma, ten, soLuong);
            } else {
                Sach moi;
                switch (cboLoai.getSelectedIndex()) {
                    case GIAO_TRINH:
                        moi = new SachGiaoTrinh(ma, ten, soLuong, txtMonHoc.getText().trim());
                        break;
                    case TAP_CHI:
                        Integer soPhatHanh = docSo(txtSoPhatHanh, "Số phát hành");
                        if (soPhatHanh == null) return;
                        moi = new TapChi(ma, ten, soLuong, soPhatHanh);
                        break;
                    default:
                        moi = new TruyenTranh(ma, ten, soLuong);
                }
                ql.them(moi);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
