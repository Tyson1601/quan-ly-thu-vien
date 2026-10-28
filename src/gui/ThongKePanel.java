package gui;

import model.PhieuMuon;
import model.Sach;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThongKePanel extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] CAC_COT = {
            "Mã phiếu", "Sách", "Độc giả", "Ngày mượn", "Hạn trả", "Số ngày trễ", "Tiền phạt"};

    private final QuanLyThuVien ql;
    private final JLabel lblTongSach = new JLabel();
    private final JLabel lblMuonNhieu = new JLabel();
    private final JLabel lblTheoLoai = new JLabel();
    private final JLabel lblTongPhat = new JLabel();
    private final DefaultTableModel modelChuaTra = taoModel();
    private final DefaultTableModel modelQuaHan = taoModel();

    public ThongKePanel(QuanLyThuVien ql) {
        this.ql = ql;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel tren = new JPanel(new GridLayout(0, 1, 4, 4));
        tren.add(lblTongSach);
        tren.add(lblMuonNhieu);
        tren.add(lblTheoLoai);
        tren.add(lblTongPhat);
        add(tren, BorderLayout.NORTH);

        JPanel giua = new JPanel(new GridLayout(2, 1, 8, 8));
        giua.add(taoKhung("Phiếu chưa trả", modelChuaTra));
        giua.add(taoKhung("Phiếu quá hạn", modelQuaHan));
        add(giua, BorderLayout.CENTER);

        napLai();
    }

    private static DefaultTableModel taoModel() {
        return new DefaultTableModel(CAC_COT, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 5 ? Integer.class : String.class;
            }
        };
    }

    private JPanel taoKhung(String tieuDe, DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setAutoCreateRowSorter(true);
        JLabel lbl = new JLabel(tieuDe);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    /** Tính lại toàn bộ số liệu từ service. */
    public void napLai() {
        lblTongSach.setText("Tổng số sách còn lại: " + ql.tongSoSachConLai());

        Sach nhieuNhat = ql.sachMuonNhieuNhat();
        lblMuonNhieu.setText("Sách được mượn nhiều nhất: "
                + (nhieuNhat == null ? "chưa có ai mượn" : nhieuNhat.getTen()));

        Map<String, Long> theoLoai = ql.thongKeTheoLoai();
        String chuoi = theoLoai.isEmpty() ? "chưa có sách"
                : theoLoai.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("   |   "));
        lblTheoLoai.setText("Số đầu sách theo loại: " + chuoi);

        lblTongPhat.setText(String.format("Tổng tiền phạt tạm tính (phiếu quá hạn): %,.0f đ", ql.tongTienPhatTamTinh()));
        napBang(modelChuaTra, ql.layPhieuChuaTra());
        napBang(modelQuaHan, ql.layPhieuQuaHan());
    }

    private void napBang(DefaultTableModel m, List<PhieuMuon> ds) {
        m.setRowCount(0);
        for (PhieuMuon p : ds) {
            m.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getDocGia().getTen(),
                    p.getNgayMuon().format(NGAY),
                    p.getHanTra().format(NGAY),
                    p.soNgayTre(),
                    String.format("%,.0f đ", p.tinhTienPhat())
            });
        }
    }
}