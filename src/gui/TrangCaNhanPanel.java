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
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Thông tin tài khoản
        JPanel thongTin = new JPanel(new GridBagLayout());
        thongTin.setOpaque(false);
        thongTin.setBorder(BorderFactory.createTitledBorder("Thông tin tài khoản"));
        dong(thongTin, 0, "Họ và tên:", lblHoTen);
        dong(thongTin, 1, "Mã độc giả:", lblMaDocGia);
        dong(thongTin, 2, "Tên đăng nhập:", lblTenDangNhap);
        dong(thongTin, 3, "Số điện thoại:", lblSdt);
        dong(thongTin, 4, "Email:", lblEmail);

        JButton btnCapNhat = UIStyle.createButton("Cập nhật thông tin", UIStyle.PRIMARY);
        JButton btnDoiMk = UIStyle.createButton("Đổi mật khẩu", new Color(100, 116, 139));
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        nut.setOpaque(false);
        nut.add(btnCapNhat);
        nut.add(btnDoiMk);

        JPanel tren = new JPanel(new BorderLayout());
        tren.setOpaque(false);
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
        UIStyle.styleTable(table);
        UIStyle.setBadgeRenderer(table, 6);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Lịch sử mượn của tôi"));
        add(sp, BorderLayout.CENTER);

        lblTongKet.setFont(UIStyle.FONT_BOLD);
        lblTongKet.setForeground(UIStyle.TEXT_DARK);
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
        l.setForeground(UIStyle.TEXT_MUTED);
        l.setFont(UIStyle.FONT_BOLD);
        p.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        giaTri.setFont(UIStyle.FONT_BODY);
        giaTri.setForeground(UIStyle.TEXT_DARK);
        p.add(giaTri, g);
    }

    public void napLai() {
        DocGia d = ql.layDocGiaCuaTaiKhoan(taiKhoan);
        lblTenDangNhap.setText(taiKhoan.getTenDangNhap());
        lblEmail.setText(taiKhoan.getEmail().isEmpty() ? "(chưa có)" : taiKhoan.getEmail());
        model.setRowCount(0);

        if (d == null) {
            lblHoTen.setText("(hồ sơ độc giả không còn tồn tại)");
            lblMaDocGia.setText("");
            lblSdt.setText("");
            lblTongKet.setText("Bạn chưa liên kết với hồ sơ độc giả nào");
            return;
        }

        lblHoTen.setText(d.getTen());
        lblMaDocGia.setText(d.getMa());
        lblSdt.setText(d.getSdt().isEmpty() ? "(chưa có)" : d.getSdt());

        int dangMuon = 0;
        int quaHan = 0;
        for (PhieuMuon p : ql.layPhieuCuaDocGia(d.getMa())) {
            boolean daTra = p.daTra();
            if (!daTra) {
                dangMuon++;
                if (p.soNgayTre() > 0) quaHan++;
            }
            model.addRow(new Object[]{
                    p.getMaPhieu(),
                    p.getSach().getTen(),
                    p.getNgayMuon().format(NGAY),
                    p.getHanTra().format(NGAY),
                    p.getNgayTra() == null ? "" : p.getNgayTra().format(NGAY),
                    p.soNgayTre(),
                    daTra ? "Đã trả" : (p.soNgayTre() > 0 ? "Quá hạn" : "Đang mượn"),
                    String.format("%,.0f đ", p.tinhTienPhat())
            });
        }

        double tongPhat = ql.tongTienPhatTamTinh(d.getMa());
        String tongKet = String.format("Đang mượn %d cuốn", dangMuon);
        if (quaHan > 0) {
            tongKet += String.format("  |  %d cuốn QUÁ HẠN  |  Tiền phạt tạm tính: %,.0f đ", quaHan, tongPhat);
        }
        lblTongKet.setText(tongKet);
    }

    private void capNhatThongTin() {
        DocGia d = ql.layDocGiaCuaTaiKhoan(taiKhoan);
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Tài khoản chưa gắn với hồ sơ độc giả");
            return;
        }

        JTextField txtTen = new JTextField(d.getTen(), 15);
        JTextField txtSdt = new JTextField(d.getSdt(), 15);
        JTextField txtEmail = new JTextField(taiKhoan.getEmail(), 15);

        UIStyle.styleTextField(txtTen);
        UIStyle.styleTextField(txtSdt);
        UIStyle.styleTextField(txtEmail);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Họ và tên:"));
        p.add(txtTen);
        p.add(new JLabel("Số điện thoại:"));
        p.add(txtSdt);
        p.add(new JLabel("Email:"));
        p.add(txtEmail);

        int kq = JOptionPane.showConfirmDialog(this, p, "Cập nhật thông tin cá nhân",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (kq != JOptionPane.OK_OPTION) return;

        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống");
            return;
        }

        try {
            ql.suaDocGia(d.getMa(), ten, txtSdt.getText().trim());
            taiKhoan.setEmail(txtEmail.getText().trim());
            napLai();
            khiDoiThongTin.run();
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin thành công");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}
