package gui;

import model.DocGia;
import model.PhieuMuon;
import model.Sach;
import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class MuonTraPanel extends JPanel {
    private static final String DANG_MUON = "Đang mượn";
    private static final String DA_TRA = "Đã trả";
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int COT_SO_NGAY_TRE = 6;
    private static final int COT_TRANG_THAI = 7;

    private final QuanLyThuVien ql;
    private final TaiKhoan taiKhoan;
    private final JComboBox<Sach> cboSach = new JComboBox<>();
    private final JComboBox<DocGia> cboDocGia = new JComboBox<>();
    private final DefaultTableModel model;
    private final JTable table;

    /**
     * Admin thấy và thao tác trên tất cả phiếu. Người dùng thường chỉ mượn bằng hồ sơ độc giả
     * của chính mình và chỉ thấy phiếu của mình (không có nút Xuất CSV).
     */
    public MuonTraPanel(QuanLyThuVien ql, TaiKhoan taiKhoan) {
        this.ql = ql;
        this.taiKhoan = taiKhoan;
        boolean laAdmin = taiKhoan.laAdmin();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Hàng chọn sách + độc giả + nút Mượn
        cboSach.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Sach) {
                    Sach s = (Sach) value;
                    setText(s.getMa() + " - " + s.getTen() + " (còn " + s.getSoLuong() + ")");
                }
                return this;
            }
        });
        cboDocGia.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DocGia) {
                    DocGia d = (DocGia) value;
                    setText(d.getMa() + " - " + d.getTen());
                }
                return this;
            }
        });
        cboDocGia.setEnabled(laAdmin);
        JButton btnMuon = new JButton("Mượn");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Sách:"));
        top.add(cboSach);
        top.add(new JLabel("Độc giả:"));
        top.add(cboDocGia);
        top.add(btnMuon);
        add(top, BorderLayout.NORTH);

        // Bảng phiếu
        model = new DefaultTableModel(new String[]{
                "Mã phiếu", "Tên sách", "Độc giả", "Ngày mượn", "Hạn trả", "Ngày trả", "Số ngày trễ", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == COT_SO_NGAY_TRE ? Integer.class : String.class;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnTra = new JButton("Trả");
        JButton btnGiaHan = new JButton("Gia hạn");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(btnTra);
        bottom.add(btnGiaHan);
        if (laAdmin) {
            JButton btnXuat = new JButton("Xuất CSV");
            bottom.add(btnXuat);
            btnXuat.addActionListener(e -> xuatCsv());
        }
        add(bottom, BorderLayout.SOUTH);

        btnMuon.addActionListener(e -> muon());
        btnTra.addActionListener(e -> tra());
        btnGiaHan.addActionListener(e -> giaHan());

        napLai();
    }

    /** Nạp lại 2 combo và bảng phiếu từ service (giữ nguyên lựa chọn nếu có thể). */
    public void napLai() {
        int viTriSach = cboSach.getSelectedIndex();
        int viTriDocGia = cboDocGia.getSelectedIndex();

        cboSach.removeAllItems();
        for (Sach s : ql.layTatCa()) cboSach.addItem(s);
        cboDocGia.removeAllItems();
        if (taiKhoan.laAdmin()) {
            for (DocGia d : ql.layTatCaDocGia()) cboDocGia.addItem(d);
        } else {
            DocGia toi = ql.layDocGiaCuaTaiKhoan(taiKhoan);
            if (toi != null) cboDocGia.addItem(toi);
        }

        if (viTriSach >= 0 && viTriSach < cboSach.getItemCount()) cboSach.setSelectedIndex(viTriSach);
        if (viTriDocGia >= 0 && viTriDocGia < cboDocGia.getItemCount()) cboDocGia.setSelectedIndex(viTriDocGia);

        model.setRowCount(0);
        for (PhieuMuon p : layPhieuHienThi()) {
            model.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getDocGia().getTen(),
                    p.getNgayMuon().format(NGAY),
                    p.getHanTra().format(NGAY),
                    p.getNgayTra() == null ? "" : p.getNgayTra().format(NGAY),
                    p.soNgayTre(),
                    p.daTra() ? DA_TRA : DANG_MUON
            });
        }
    }

    /** Admin: tất cả phiếu. Người dùng: chỉ phiếu của hồ sơ độc giả của mình. */
    private List<PhieuMuon> layPhieuHienThi() {
        if (taiKhoan.laAdmin()) return ql.layTatCaPhieu();
        DocGia toi = ql.layDocGiaCuaTaiKhoan(taiKhoan);
        return (toi == null) ? new ArrayList<>() : ql.layPhieuCuaDocGia(toi.getMa());
    }

    private void muon() {
        Sach s = (Sach) cboSach.getSelectedItem();
        DocGia d = (DocGia) cboDocGia.getSelectedItem();
        if (s == null || d == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng");
            return;
        }
        try {
            PhieuMuon p = ql.muon(s.getMa(), d.getMa());
            napLai();
            JOptionPane.showMessageDialog(this, "Đã tạo phiếu " + p.getMaPhieu()
                    + "\nHạn trả: " + p.getHanTra().format(NGAY));
        } catch (Exception ex) {   // hết sách, vượt giới hạn mượn, không tìm thấy...
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /** Mã phiếu của dòng đang chọn, hoặc null nếu chưa chọn (đã báo lỗi). */
    private String maPhieuDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng");
            return null;
        }
        return (String) table.getValueAt(row, 0);
    }

    private boolean dongDaTra() {
        return DA_TRA.equals(table.getValueAt(table.getSelectedRow(), COT_TRANG_THAI));
    }

    private void tra() {
        String maPhieu = maPhieuDangChon();
        if (maPhieu == null) return;
        if (dongDaTra()) {
            JOptionPane.showMessageDialog(this, "Phiếu này đã được trả rồi");
            return;
        }
        try {
            double tienPhat = ql.tra(maPhieu);
            napLai();
            JOptionPane.showMessageDialog(this, tienPhat > 0
                    ? String.format("Trả sách thành công.\nTiền phạt: %,.0f đ", tienPhat)
                    : "Trả sách thành công.\nKhông có tiền phạt.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void giaHan() {
        String maPhieu = maPhieuDangChon();
        if (maPhieu == null) return;
        if (dongDaTra()) {
            JOptionPane.showMessageDialog(this, "Phiếu này đã được trả, không thể gia hạn");
            return;
        }
        try {
            ql.giaHan(maPhieu);
            napLai();
            JOptionPane.showMessageDialog(this, "Gia hạn thành công, xem hạn trả mới trong bảng");
        } catch (Exception ex) {   // quá hạn, hết số lần gia hạn...
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void xuatCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("phieu.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String duongDan = fc.getSelectedFile().getAbsolutePath();
        try {
            ql.xuatPhieuCsv(duongDan);
            JOptionPane.showMessageDialog(this, "Đã xuất file:\n" + duongDan);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không xuất được file: " + ex.getMessage());
        }
    }
}