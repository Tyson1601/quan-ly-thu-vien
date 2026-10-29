package gui;

import model.DocGia;
import model.PhieuMuon;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DocGiaPanel extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QuanLyThuVien ql;
    private final DefaultTableModel model;
    private final JTable table;

    /** laAdmin = false thì ẩn Thêm/Sửa/Xóa, vẫn được xem Lịch sử. */
    public DocGiaPanel(QuanLyThuVien ql, boolean laAdmin) {
        this.ql = ql;
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        model = new DefaultTableModel(new String[]{"Mã", "Tên", "SĐT", "Đang mượn"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 3 ? Integer.class : String.class;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        UIStyle.styleTable(table);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        bottom.setOpaque(false);
        if (laAdmin) {
            JButton btnThem = UIStyle.createButton("Thêm mới", UIStyle.PRIMARY);
            JButton btnSua = UIStyle.createButton("Cập nhật", UIStyle.WARNING);
            JButton btnXoa = UIStyle.createButton("Xóa", UIStyle.DANGER);
            bottom.add(btnThem);
            bottom.add(btnSua);
            bottom.add(btnXoa);
            btnThem.addActionListener(e -> nhap(null));
            btnSua.addActionListener(e -> sua());
            btnXoa.addActionListener(e -> xoa());
        }
        JButton btnLichSu = UIStyle.createButton("Lịch sử mượn", new Color(14, 165, 233));
        btnLichSu.addActionListener(e -> lichSu());
        bottom.add(btnLichSu);
        add(bottom, BorderLayout.SOUTH);

        napLai();
    }

    public void napLai() {
        model.setRowCount(0);
        for (DocGia d : ql.layTatCaDocGia()) {
            model.addRow(new Object[]{d.getMa(), d.getTen(), d.getSdt(), ql.demSachDangMuon(d.getMa())});
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

    private DocGia timDocGia(String ma) {
        for (DocGia d : ql.layTatCaDocGia()) {
            if (d.getMa().equals(ma)) return d;
        }
        return null;
    }

    private void sua() {
        String ma = maDangChon();
        if (ma == null) return;
        DocGia d = timDocGia(ma);
        if (d == null) {
            napLai();
            return;
        }
        nhap(d);
    }

    /** cu == null: thêm độc giả mới; cu != null: sửa độc giả đó (khóa ô mã). */
    private void nhap(DocGia cu) {
        boolean cheDoSua = (cu != null);
        JTextField txtMa = new JTextField(cheDoSua ? cu.getMa() : "", 15);
        txtMa.setEditable(!cheDoSua);
        JTextField txtTen = new JTextField(cheDoSua ? cu.getTen() : "", 15);
        JTextField txtSdt = new JTextField(cheDoSua && cu.getSdt() != null ? cu.getSdt() : "", 15);

        UIStyle.styleTextField(txtMa);
        UIStyle.styleTextField(txtTen);
        UIStyle.styleTextField(txtSdt);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Mã:"));
        form.add(txtMa);
        form.add(new JLabel("Tên:"));
        form.add(txtTen);
        form.add(new JLabel("SĐT:"));
        form.add(txtSdt);

        // Lặp lại để khi lỗi vẫn giữ nguyên nội dung đã gõ
        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form,
                    cheDoSua ? "Sửa độc giả" : "Thêm độc giả",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã và tên không được để trống");
                continue;
            }
            try {
                if (cheDoSua) ql.suaDocGia(ma, ten, sdt);
                else ql.themDocGia(new DocGia(ma, ten, sdt));
                napLai();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void xoa() {
        String ma = maDangChon();
        if (ma == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xóa độc giả có mã \"" + ma + "\"?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        try {
            ql.xoaDocGia(ma);
        } catch (Exception ex) {   // có thể là DocGiaDangMuonException
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        napLai();
    }

    private void lichSu() {
        String ma = maDangChon();
        if (ma == null) return;
        DocGia d = timDocGia(ma);
        String ten = (d == null) ? ma : d.getTen();

        List<PhieuMuon> ds = ql.layPhieuCuaDocGia(ma);
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Độc giả này chưa mượn cuốn sách nào");
            return;
        }

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Mã phiếu", "Sách", "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái", "Tiền phạt"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        for (PhieuMuon p : ds) {
            m.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getNgayMuon().format(NGAY),
                    p.getHanTra().format(NGAY),
                    p.getNgayTra() == null ? "" : p.getNgayTra().format(NGAY),
                    p.daTra() ? "Đã trả" : "Đang mượn",
                    String.format("%,.0f đ", p.tinhTienPhat())
            });
        }
        JTable t = new JTable(m);
        t.setAutoCreateRowSorter(true);
        UIStyle.styleTable(t);
        UIStyle.setBadgeRenderer(t, 5);

        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(680, 220));
        JOptionPane.showMessageDialog(this, sp, "Lịch sử mượn - " + ten, JOptionPane.PLAIN_MESSAGE);
    }
}
