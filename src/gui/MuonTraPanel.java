package gui;

import model.DocGia;
import model.PhieuMuon;
import model.Sach;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class MuonTraPanel extends JPanel {
    private static final String DANG_MUON = "Đang mượn";
    private static final String DA_TRA = "Đã trả";
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QuanLyThuVien ql;
    private final JComboBox<Sach> cboSach = new JComboBox<>();
    private final JComboBox<DocGia> cboDocGia = new JComboBox<>();
    private final DefaultTableModel model;
    private final JTable table;

    public MuonTraPanel(QuanLyThuVien ql) {
        this.ql = ql;
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
        JButton btnMuon = new JButton("Mượn");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Sách:"));
        top.add(cboSach);
        top.add(new JLabel("Độc giả:"));
        top.add(cboDocGia);
        top.add(btnMuon);
        add(top, BorderLayout.NORTH);

        // Bảng phiếu
        model = new DefaultTableModel(
                new String[]{"Mã phiếu", "Tên sách", "Độc giả", "Ngày mượn", "Ngày trả", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnTra = new JButton("Trả");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(btnTra);
        add(bottom, BorderLayout.SOUTH);

        btnMuon.addActionListener(e -> muon());
        btnTra.addActionListener(e -> tra());

        napLai();
    }

    /** Nạp lại 2 combo và bảng phiếu từ service (giữ nguyên lựa chọn nếu có thể). */
    public void napLai() {
        int viTriSach = cboSach.getSelectedIndex();
        int viTriDocGia = cboDocGia.getSelectedIndex();

        cboSach.removeAllItems();
        for (Sach s : ql.layTatCa()) cboSach.addItem(s);
        cboDocGia.removeAllItems();
        for (DocGia d : ql.layTatCaDocGia()) cboDocGia.addItem(d);

        if (viTriSach >= 0 && viTriSach < cboSach.getItemCount()) cboSach.setSelectedIndex(viTriSach);
        if (viTriDocGia >= 0 && viTriDocGia < cboDocGia.getItemCount()) cboDocGia.setSelectedIndex(viTriDocGia);

        model.setRowCount(0);
        for (PhieuMuon p : ql.layTatCaPhieu()) {
            model.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getDocGia().getTen(),
                    p.getNgayMuon().format(NGAY),
                    p.getNgayTra() == null ? "" : p.getNgayTra().format(NGAY),
                    p.daTra() ? DA_TRA : DANG_MUON
            });
        }
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
            JOptionPane.showMessageDialog(this, "Đã tạo phiếu " + p.getMaPhieu());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void tra() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng");
            return;
        }
        String maPhieu = (String) table.getValueAt(row, 0);
        if (DA_TRA.equals(table.getValueAt(row, 5))) {
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
}
