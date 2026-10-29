package gui;

import model.PhieuMuon;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/** Tab dành cho admin: chỉnh ngày hệ thống để trình diễn phiếu quá hạn và tiền phạt. */
public class ThoiGianPanel extends JPanel {
    private static final DateTimeFormatter NGAY_HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_NHAP =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final QuanLyThuVien ql;
    private final Runnable khiDoiNgay;   // báo cho MainFrame cập nhật thanh trạng thái
    private final JLabel lblNgayHeThong = new JLabel();
    private final JLabel lblGhiChu = new JLabel();
    private final JTextField txtNgay = new JTextField(10);
    private final JLabel lblTongPhat = new JLabel();
    private final DefaultTableModel model;

    public ThoiGianPanel(QuanLyThuVien ql, Runnable khiDoiNgay) {
        this.ql = ql;
        this.khiDoiNgay = khiDoiNgay;
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Phần trên: ngày hiện tại + các nút chỉnh
        lblNgayHeThong.setFont(UIStyle.FONT_TITLE);
        lblNgayHeThong.setForeground(UIStyle.TEXT_DARK);
        lblGhiChu.setFont(lblGhiChu.getFont().deriveFont(Font.ITALIC));
        lblGhiChu.setForeground(UIStyle.TEXT_MUTED);

        UIStyle.styleTextField(txtNgay);
        JButton btnDat = UIStyle.createButton("Đặt ngày", UIStyle.PRIMARY);
        JButton btn1 = UIStyle.createButton("+1 ngày", new Color(100, 116, 139));
        JButton btn7 = UIStyle.createButton("+7 ngày", new Color(100, 116, 139));
        JButton btn14 = UIStyle.createButton("+14 ngày", new Color(100, 116, 139));
        JButton btn30 = UIStyle.createButton("+30 ngày", new Color(100, 116, 139));
        JButton btnThat = UIStyle.createButton("Về ngày thật", UIStyle.SUCCESS);

        JPanel dieuKhien = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        dieuKhien.setOpaque(false);
        dieuKhien.add(new JLabel("Đặt ngày (dd/MM/yyyy):"));
        dieuKhien.add(txtNgay);
        dieuKhien.add(btnDat);
        dieuKhien.add(Box.createHorizontalStrut(10));
        dieuKhien.add(btn1);
        dieuKhien.add(btn7);
        dieuKhien.add(btn14);
        dieuKhien.add(btn30);
        dieuKhien.add(Box.createHorizontalStrut(10));
        dieuKhien.add(btnThat);

        JPanel tren = new JPanel(new GridLayout(0, 1, 4, 4));
        tren.setOpaque(false);
        tren.add(lblNgayHeThong);
        tren.add(lblGhiChu);
        tren.add(dieuKhien);
        add(tren, BorderLayout.NORTH);

        // Giữa: bảng phiếu quá hạn tại ngày hệ thống
        model = new DefaultTableModel(
                new String[]{"Mã phiếu", "Sách", "Độc giả", "Hạn trả", "Số ngày trễ", "Tiền phạt"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 4 ? Integer.class : String.class;
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        UIStyle.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Phiếu quá hạn tại ngày hệ thống"));
        add(sp, BorderLayout.CENTER);

        // Dưới: tổng tiền phạt
        lblTongPhat.setFont(UIStyle.FONT_BOLD);
        lblTongPhat.setForeground(UIStyle.DANGER);
        add(lblTongPhat, BorderLayout.SOUTH);

        btnDat.addActionListener(e -> datNgay());
        txtNgay.addActionListener(e -> datNgay());
        btn1.addActionListener(e -> tuaNhanh(1));
        btn7.addActionListener(e -> tuaNhanh(7));
        btn14.addActionListener(e -> tuaNhanh(14));
        btn30.addActionListener(e -> tuaNhanh(30));
        btnThat.addActionListener(e -> {
            ql.veNgayThat();
            capNhat();
        });

        napLai();
    }

    /** Nạp lại ngày hệ thống và bảng phiếu quá hạn từ service. */
    public void napLai() {
        LocalDate homNay = ql.ngayHienTai();
        lblNgayHeThong.setText("Ngày hệ thống: " + homNay.format(NGAY_HIEN_THI));
        lblGhiChu.setText(ql.dangGiaLapNgay()
                ? "Đang giả lập thời gian (ngày thật: " + LocalDate.now().format(NGAY_HIEN_THI)
                  + "). Mượn, trả, gia hạn và tiền phạt đều tính theo ngày này."
                : "Đang dùng ngày thật của máy.");
        txtNgay.setText(homNay.format(NGAY_HIEN_THI));

        List<PhieuMuon> quaHan = ql.layPhieuQuaHan();
        model.setRowCount(0);
        for (PhieuMuon p : quaHan) {
            model.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getDocGia().getTen(),
                    p.getHanTra().format(NGAY_HIEN_THI),
                    p.soNgayTre(),
                    String.format("%,.0f đ", p.tinhTienPhat())
            });
        }
        lblTongPhat.setText(String.format("Tổng tiền phạt tạm tính: %,.0f đ  (%d phiếu quá hạn)",
                ql.tongTienPhatTamTinh(), quaHan.size()));
    }

    private void capNhat() {
        napLai();
        khiDoiNgay.run();
    }

    private void datNgay() {
        try {
            ql.datNgayHienTai(LocalDate.parse(txtNgay.getText().trim(), NGAY_NHAP));
            capNhat();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ngày không hợp lệ. Nhập theo dạng dd/MM/yyyy, ví dụ 15/11/2026");
        }
    }

    private void tuaNhanh(int soNgay) {
        ql.tuaNhanhNgay(soNgay);
        capNhat();
    }
}
