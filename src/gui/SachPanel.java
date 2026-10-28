package gui;

import model.Sach;
import service.NhapSachCsv;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SachPanel extends JPanel {
    private static final String TAT_CA = "Tất cả loại";
    private static final String[] CAC_LOAI = {TAT_CA, "Truyện tranh", "Giáo trình", "Tạp chí"};

    private final QuanLyThuVien ql;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtTim = new JTextField(20);
    private final JComboBox<String> cboLoai = new JComboBox<>(CAC_LOAI);

    /** laAdmin = false thì ẩn các nút Thêm/Sửa/Xóa. */
    public SachPanel(QuanLyThuVien ql, boolean laAdmin) {
        this.ql = ql;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Thanh tìm kiếm + lọc theo loại
        JButton btnTim = new JButton("Tìm");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tìm theo tên:"));
        top.add(txtTim);
        top.add(new JLabel("Loại:"));
        top.add(cboLoai);
        top.add(btnTim);
        add(top, BorderLayout.NORTH);

        // Bảng
        model = new DefaultTableModel(new String[]{"Mã", "Tên", "Loại", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 3 ? Integer.class : String.class;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Nút thao tác (chỉ admin)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (laAdmin) {
            JButton btnThem = new JButton("Thêm");
            JButton btnSua = new JButton("Sửa");
            JButton btnXoa = new JButton("Xóa");
            bottom.add(btnThem);
            bottom.add(btnSua);
            bottom.add(btnXoa);
            JButton btnNhapCsv = new JButton("Nhập từ CSV");
            bottom.add(btnNhapCsv);
            btnNhapCsv.addActionListener(e -> nhapCsv());
            btnThem.addActionListener(e -> them());
            btnSua.addActionListener(e -> sua());
            btnXoa.addActionListener(e -> xoa());
        } else {
            bottom.add(new JLabel("Tài khoản người dùng: chỉ được xem, tìm kiếm và lọc sách."));
        }
        add(bottom, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> loc());
        txtTim.addActionListener(e -> loc());
        cboLoai.addActionListener(e -> loc());

        napLai();
    }

    /** Nạp lại bảng từ service, xóa ô tìm kiếm và bộ lọc. */
    public void napLai() {
        txtTim.setText("");
        cboLoai.setSelectedIndex(0);
        loc();
    }

    /** Kết hợp ô tìm theo tên và lọc theo loại (logic nằm trong service). */
    private void loc() {
        String tuKhoa = txtTim.getText().trim();
        String loai = (String) cboLoai.getSelectedItem();

        List<Sach> ds = new ArrayList<>(tuKhoa.isEmpty() ? ql.layTatCa() : ql.tim(tuKhoa));
        if (loai != null && !TAT_CA.equals(loai)) {
            ds.retainAll(ql.locTheoLoai(loai));
        }
        hienThi(ds);
    }

    private void hienThi(List<Sach> ds) {
        model.setRowCount(0);
        for (Sach s : ds) {
            model.addRow(new Object[]{s.getMa(), s.getTen(), s.getLoai(), s.getSoLuong()});
        }
    }

    private String maDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng");
            return null;
        }
        return (String) table.getValueAt(row, 0);
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
        if (can == null) {
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        napLai();
    }

    /** Chọn file CSV rồi thêm hàng loạt sách (cột: Mã, Tên, Loại, Số lượng, Thông tin thêm). */
    private void nhapCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file CSV danh sách sách");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("File CSV (*.csv)", "csv"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try {
            NhapSachCsv.KetQua kq = NhapSachCsv.nhap(ql, f.getAbsolutePath());
            if (kq.soThem > 0) {
                try {
                    ql.luuFile();   // lưu luôn để không mất nếu chương trình tắt đột ngột
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Đã nhập nhưng chưa lưu được vào file: " + ex.getMessage());
                }
            }
            JOptionPane.showMessageDialog(this, kq.tomTat(), "Kết quả nhập CSV",
                    kq.loi.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không đọc được file: " + ex.getMessage());
        }
        napLai();
    }
}