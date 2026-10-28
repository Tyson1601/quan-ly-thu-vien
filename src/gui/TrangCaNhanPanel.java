package gui;

import model.DocGia;
import model.PhieuMuon;
import model.TaiKhoan;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/** Trang cá nhân của người dùng: thông tin tài khoản, sách đang mượn và tiền phạt của riêng mình. */
public class TrangCaNhanPanel extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final QuanLyThuVien ql;
    private final TaiKhoan taiKhoan;
    private final Runnable khiDoiThongTin;   // để MainFrame cập nhật lời chào
    private final JLabel lblHoTen = new JLabel();
    private final JLabel lblMaDocGia = new JLabel();
    private final JLabel lblTenDangNhap = new JLabel();
    private final JLabel lblSdt = new JLabel();
    private final JLabel lblEmail = new JLabel();
    private final JLabel lblTongKet = new JLabel();
    private final DefaultTableModel model;

    public TrangCaNhanPanel(QuanLyThuVien ql, TaiKhoan taiKhoan, Runnable khiDoiThongTin) {
        this.ql = ql;
        this.taiKhoan = taiKhoan;
        this.khiDoiThongTin = khiDoiThongTin;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Thông tin tài khoản
        JPanel thongTin = new JPanel(new GridBagLayout());
        thongTin.setBorder(BorderFactory.createTitledBorder("Thông tin tài khoản"));
        dong(thongTin, 0, "Họ và tên:", lblHoTen);
        dong(thongTin, 1, "Mã độc giả:", lblMaDocGia);
        dong(thongTin, 2, "Tên đăng nhập:", lblTenDangNhap);
        dong(thongTin, 3, "Số điện thoại:", lblSdt);
        dong(thongTin, 4, "Email:", lblEmail);

        JButton btnCapNhat = new JButton("Cập nhật thông tin");
        JButton btnDoiMk = new JButton("Đổi mật khẩu");
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nut.add(btnCapNhat);
        nut.add(btnDoiMk);

        JPanel tren = new JPanel(new BorderLayout());
        tren.add(thongTin, BorderLayout.CENTER);
        tren.add(nut, BorderLayout.SOUTH);
        add(tren, BorderLayout.NORTH);

        // Lịch sử mượn của riêng mình
        model = new DefaultTableModel(new String[]{
                "Mã phiếu", "Sách", "Ngày mượn", "Hạn trả", "Ngày trả", "Số ngày trễ", "Trạng thái", "Tiền phạt"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 5 ? Integer.class : String.class;
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Lịch sử mượn của tôi"));
        add(sp, BorderLayout.CENTER);

        lblTongKet.setFont(lblTongKet.getFont().deriveFont(Font.BOLD, 13f));
        add(lblTongKet, BorderLayout.SOUTH);

        btnCapNhat.addActionListener(e -> capNhatThongTin());
        btnDoiMk.addActionListener(e -> DoiMatKhauDialog.hien(this, ql, taiKhoan));

        napLai();
    }

    private static void dong(JPanel p, int y, String nhan, JLabel giaTri) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(3, 8, 3, 8);
        g.gridx = 0;
        JLabel l = new JLabel(nhan);
        l.setForeground(Color.GRAY);
        p.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(giaTri, g);
    }

    public void napLai() {
        DocGia d = ql.layDocGiaCuaTaiKhoan(taiKhoan);
        lblTenDangNhap.setText(taiKhoan.getTenDangNhap());
        lblEmail.setText(taiKhoan.getEmail().isEmpty() ? "(chưa có)" : taiKhoan.getEmail());
        model.setRowCount(0);

        if (d == null) {
            lblHoTen.setText("(hồ sơ độc giả không còn tồn tại)");
            lblMaDocGia.setText("-");
            lblSdt.setText("-");
            lblTongKet.setText(" ");
            return;
        }
        lblHoTen.setText(d.getTen());
        lblMaDocGia.setText(d.getMa());
        lblSdt.setText(d.getSdt() == null || d.getSdt().isEmpty() ? "(chưa có)" : d.getSdt());

        for (PhieuMuon p : ql.layPhieuCuaDocGia(d.getMa())) {
            model.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getNgayMuon().format(NGAY),
                    p.getHanTra().format(NGAY),
                    p.getNgayTra() == null ? "" : p.getNgayTra().format(NGAY),
                    p.soNgayTre(),
                    p.daTra() ? "Đã trả" : "Đang mượn",
                    String.format("%,.0f đ", p.tinhTienPhat())
            });
        }
        lblTongKet.setText(String.format("Đang mượn: %d/%d cuốn     |     Tiền phạt tạm tính (phiếu quá hạn): %,.0f đ",
                ql.demSachDangMuon(d.getMa()), QuanLyThuVien.GIOI_HAN_MUON,
                ql.tongTienPhatTamTinh(d.getMa())));
    }

    private void capNhatThongTin() {
        DocGia d = ql.layDocGiaCuaTaiKhoan(taiKhoan);
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Hồ sơ độc giả không còn tồn tại");
            return;
        }
        JTextField txtHoTen = new JTextField(d.getTen(), 18);
        JTextField txtSdt = new JTextField(d.getSdt() == null ? "" : d.getSdt(), 18);
        JTextField txtEmail = new JTextField(taiKhoan.getEmail(), 18);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Họ và tên:"));
        form.add(txtHoTen);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtSdt);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);

        while (true) {
            int kq = JOptionPane.showConfirmDialog(this, form, "Cập nhật thông tin",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;
            try {
                ql.capNhatThongTin(taiKhoan, txtHoTen.getText(), txtSdt.getText(), txtEmail.getText());
                try {
                    ql.luuFile();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật nhưng chưa lưu được vào file: " + ex.getMessage());
                }
                napLai();
                khiDoiThongTin.run();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
