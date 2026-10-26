package gui;

import model.Sach;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SachPanel extends JPanel {
    private final QuanLyThuVien ql;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtTim = new JTextField(20);

    public SachPanel(QuanLyThuVien ql) {
        this.ql = ql;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Thanh tìm kiếm
        JButton btnTim = new JButton("Tìm");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tìm theo tên:"));
        top.add(txtTim);
        top.add(btnTim);
        add(top, BorderLayout.NORTH);

        // Bảng
        model = new DefaultTableModel(new String[]{"Mã", "Tên", "Loại", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 3 ? Integer.class : String.class; // để sắp xếp số đúng thứ tự
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Nút thao tác
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(btnThem);
        bottom.add(btnSua);
        bottom.add(btnXoa);
        add(bottom, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> tim());
        txtTim.addActionListener(e -> tim());
        btnThem.addActionListener(e -> them());
        btnSua.addActionListener(e -> sua());
        btnXoa.addActionListener(e -> xoa());

        napLai();
    }

    /** Nạp lại bảng từ ql.layTatCa() (và xóa ô tìm kiếm cho khớp với nội dung bảng). */
    public void napLai() {
        txtTim.setText("");
        hienThi(ql.layTatCa());
    }

    private void hienThi(List<Sach> ds) {
        model.setRowCount(0);
        for (Sach s : ds) {
            model.addRow(new Object[]{s.getMa(), s.getTen(), s.getLoai(), s.getSoLuong()});
        }
    }

    private void tim() {
        String tuKhoa = txtTim.getText().trim();
        hienThi(tuKhoa.isEmpty() ? ql.layTatCa() : ql.tim(tuKhoa));
    }

    /** Trả về mã sách của dòng đang chọn, hoặc null nếu chưa chọn (đã báo lỗi). */
    private String maDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng");
            return null;
        }
        return (String) table.getValueAt(row, 0); // getValueAt dùng chỉ số view nên đúng cả khi đã sắp xếp
    }

    private void them() {
        new SachDialog(SwingUtilities.getWindowAncestor(this), ql, null).setVisible(true);
        napLai();
    }

    private void sua() {
        String ma = maDangChon();
        if (ma == null) return;
        Sach can = null;
        for (Sach s : ql.layTatCa()) {
            if (s.getMa().equals(ma)) { can = s; break; }
        }
        if (can == null) { // bảng đã cũ so với dữ liệu
            napLai();
            return;
        }
        new SachDialog(SwingUtilities.getWindowAncestor(this), ql, can).setVisible(true);
        napLai();
    }

    private void xoa() {
        String ma = maDangChon();
        if (ma == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xóa sách có mã \"" + ma + "\"?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        try {
            ql.xoa(ma);
        } catch (Exception ex) { // Exception: sau này service sẽ thêm SachDangMuonException
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        napLai();
    }
}
