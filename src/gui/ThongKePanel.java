package gui;

import model.PhieuMuon;
import model.Sach;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ThongKePanel extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QuanLyThuVien ql;
    private final JLabel lblTongConLai = new JLabel();
    private final JLabel lblMuonNhieuNhat = new JLabel();
    private final DefaultTableModel model;

    public ThongKePanel(QuanLyThuVien ql) {
        this.ql = ql;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Hai số liệu tổng hợp
        Font font = lblTongConLai.getFont().deriveFont(Font.BOLD, 14f);
        lblTongConLai.setFont(font);
        lblMuonNhieuNhat.setFont(font);
        JPanel top = new JPanel(new GridLayout(2, 1, 4, 4));
        top.add(lblTongConLai);
        top.add(lblMuonNhieuNhat);
        add(top, BorderLayout.NORTH);

        // Bảng phiếu chưa trả
        model = new DefaultTableModel(new String[]{"Mã phiếu", "Tên sách", "Độc giả", "Ngày mượn"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Phiếu chưa trả"));
        add(scroll, BorderLayout.CENTER);

        napLai();
    }

    /** Nạp lại toàn bộ số liệu từ 3 hàm thống kê của service. */
    public void napLai() {
        lblTongConLai.setText("Tổng số sách còn lại: " + ql.tongSoSachConLai());

        Sach s = ql.sachMuonNhieuNhat();
        lblMuonNhieuNhat.setText("Sách được mượn nhiều nhất: "
                + (s == null ? "Chưa có ai mượn" : s.getMa() + " - " + s.getTen()));

        model.setRowCount(0);
        for (PhieuMuon p : ql.layPhieuChuaTra()) {
            model.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getDocGia().getTen(),
                    p.getNgayMuon().format(NGAY)
            });
        }
    }
}
