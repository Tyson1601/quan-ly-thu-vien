package gui;

import model.DocGia;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DocGiaPanel extends JPanel {
    private final QuanLyThuVien ql;
    private final DefaultTableModel model;

    public DocGiaPanel(QuanLyThuVien ql) {
        this.ql = ql;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        model = new DefaultTableModel(new String[]{"Mã", "Tên", "SĐT"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnThem = new JButton("Thêm");
        btnThem.addActionListener(e -> them());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(btnThem);
        add(bottom, BorderLayout.SOUTH);

        napLai();
    }

    /** Nạp lại bảng từ ql.layTatCaDocGia(). */
    public void napLai() {
        model.setRowCount(0);
        for (DocGia d : ql.layTatCaDocGia()) {
            model.addRow(new Object[]{d.getMa(), d.getTen(), d.getSdt()});
        }
    }

    private void them() {
        JTextField txtMa = new JTextField(15);
        JTextField txtTen = new JTextField(15);
        JTextField txtSdt = new JTextField(15);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Mã:"));
        form.add(txtMa);
        form.add(new JLabel("Tên:"));
        form.add(txtTen);
        form.add(new JLabel("SĐT:"));
        form.add(txtSdt);

        // Lặp lại để khi lỗi (trùng mã, thiếu dữ liệu...) vẫn giữ nguyên nội dung đã gõ
        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form, "Thêm độc giả",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã và tên không được để trống");
                continue;
            }
            try {
                ql.themDocGia(new DocGia(ma, ten, txtSdt.getText().trim()));
                napLai();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
