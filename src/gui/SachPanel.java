package gui;

import model.Sach;
import model.SachGiaoTrinh;
import model.TapChi;
import model.TruyenTranh;
import service.NhapSachCsv;
import service.QuanLyThuVien;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SachPanel extends JPanel {
    private final QuanLyThuVien ql;
    private final DefaultTableModel model;
    private final JTable table;

    // Các thành phần của Bộ lọc nâng cao đa tiêu chí
    private JTextField txtTimKiem;
    private JComboBox<String> cboLoaiSach;
    private JComboBox<String> cboTacGia;
    private JComboBox<String> cboTheLoai;
    private JComboBox<String> cboTrangThai;
    private boolean dangCapNhatCombo = false;

    // Hai chế độ hiển thị: danh sách (bảng) và ảnh bìa (lưới)
    private static final String CHE_DO_BANG = "bang";
    private static final String CHE_DO_BIA = "bia";
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel khuVuc = new JPanel(cardLayout);
    private final DefaultListModel<Sach> modelBia = new DefaultListModel<>();
    private final JList<Sach> listBia = new JList<>(modelBia);
    private final JRadioButton rdoBang = new JRadioButton("Danh sách", true);
    private final JRadioButton rdoBia = new JRadioButton("Ảnh bìa");

    /** laAdmin = false thì ẩn các nút Thêm/Sửa/Xóa. */
    public SachPanel(QuanLyThuVien ql, boolean laAdmin) {
        this.ql = ql;
        setLayout(new BorderLayout(12, 12));
        setBackground(UIStyle.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // 1. Thanh tìm kiếm và bộ lọc đa tiêu chí
        add(createFilterBar(), BorderLayout.NORTH);

        // 2. Bảng hiển thị danh sách sách
        model = new DefaultTableModel(new String[]{"Mã", "Tên", "Loại", "Chi tiết / Môn học", "Số lượng"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 4 ? Integer.class : String.class;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        UIStyle.styleTable(table);
        UIStyle.setBadgeRenderer(table, 2);

        // 3. Lưới ảnh bìa: JList xếp ngang, tự xuống dòng theo bề rộng cửa sổ
        listBia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listBia.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listBia.setVisibleRowCount(-1);
        listBia.setFixedCellWidth(AnhBia.RONG + 30);
        listBia.setFixedCellHeight(AnhBia.CAO + 66);
        listBia.setCellRenderer(new BiaRenderer());

        khuVuc.setOpaque(false);
        khuVuc.add(new JScrollPane(table), CHE_DO_BANG);
        khuVuc.add(new JScrollPane(listBia), CHE_DO_BIA);
        add(khuVuc, BorderLayout.CENTER);

        // 4. Nút thao tác dưới cùng (chỉ admin có quyền chỉnh sửa)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        bottom.setOpaque(false);
        if (laAdmin) {
            JButton btnThem = UIStyle.createButton("Thêm mới", UIStyle.PRIMARY);
            JButton btnSua = UIStyle.createButton("Cập nhật", UIStyle.WARNING);
            JButton btnXoa = UIStyle.createButton("Xóa", UIStyle.DANGER);
            bottom.add(btnThem);
            bottom.add(btnSua);
            bottom.add(btnXoa);
            JButton btnAnh = UIStyle.createButton("Đặt ảnh bìa", new Color(14, 165, 233));
            JButton btnGoAnh = UIStyle.createButton("Gỡ ảnh bìa", new Color(100, 116, 139));
            bottom.add(btnAnh);
            bottom.add(btnGoAnh);
            btnAnh.addActionListener(e -> datAnhBia());
            btnGoAnh.addActionListener(e -> goAnhBia());
            JButton btnNhapCsv = UIStyle.createButton("Nhập CSV", UIStyle.SUCCESS);
            bottom.add(btnNhapCsv);
            btnNhapCsv.addActionListener(e -> nhapCsv());
            btnThem.addActionListener(e -> them());
            btnSua.addActionListener(e -> sua());
            btnXoa.addActionListener(e -> xoa());
        } else {
            JLabel lblNote = new JLabel("Tài khoản người dùng: chỉ được xem, tìm kiếm và lọc sách.");
            lblNote.setFont(UIStyle.FONT_BODY);
            lblNote.setForeground(UIStyle.TEXT_MUTED);
            bottom.add(lblNote);
        }
        add(bottom, BorderLayout.SOUTH);

        rdoBang.addActionListener(e -> doiCheDo());
        rdoBia.addActionListener(e -> doiCheDo());

        napLai();
    }

    /** Tạo Card Panel chứa toàn bộ thanh tìm kiếm và các hộp chọn lọc đa tiêu chí */
    private JPanel createFilterBar() {
        JPanel filterCard = UIStyle.createCardPanel(new BorderLayout(10, 8));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row.setOpaque(false);

        JLabel lblTim = new JLabel("Tìm kiếm:");
        lblTim.setFont(UIStyle.FONT_BOLD);
        txtTimKiem = new JTextField(18);
        UIStyle.styleTextField(txtTimKiem);
        txtTimKiem.setToolTipText("Nhập mã sách, tên sách, tác giả hoặc môn học/chi tiết...");

        cboLoaiSach = new JComboBox<>(new String[]{
                "Tất cả loại hình", "Giáo trình", "Tạp chí", "Truyện tranh"
        });
        cboTacGia = new JComboBox<>(new String[]{"Tất cả tác giả"});
        cboTheLoai = new JComboBox<>(new String[]{"Tất cả thể loại"});
        cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả trạng thái", "Còn sách", "Đã hết"
        });

        UIStyle.styleComboBox(cboLoaiSach);
        UIStyle.styleComboBox(cboTacGia);
        UIStyle.styleComboBox(cboTheLoai);
        UIStyle.styleComboBox(cboTrangThai);

        JButton btnResetLoc = UIStyle.createButton("Đặt lại lọc", new Color(100, 116, 139));
        btnResetLoc.setFont(new Font(UIStyle.FONT_FAMILY, Font.BOLD, 12));
        btnResetLoc.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnResetLoc.addActionListener(e -> {
            txtTimKiem.setText("");
            cboLoaiSach.setSelectedIndex(0);
            cboTacGia.setSelectedIndex(0);
            cboTheLoai.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            locDuLieuSach();
        });

        row.add(lblTim);
        row.add(txtTimKiem);
        row.add(new JLabel("Loại:"));
        row.add(cboLoaiSach);
        row.add(new JLabel("Thể loại / Môn:"));
        row.add(cboTheLoai);
        row.add(new JLabel("Tác giả:"));
        row.add(cboTacGia);
        row.add(cboTrangThai);
        row.add(btnResetLoc);

        row.add(Box.createHorizontalStrut(12));
        row.add(new JLabel("Hiển thị:"));
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(rdoBang);
        nhom.add(rdoBia);
        rdoBang.setOpaque(false);
        rdoBia.setOpaque(false);
        row.add(rdoBang);
        row.add(rdoBia);

        // Gắn sự kiện: Gõ phím tới đâu -> lọc ngay tới đó
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locDuLieuSach(); }
            @Override public void removeUpdate(DocumentEvent e) { locDuLieuSach(); }
            @Override public void changedUpdate(DocumentEvent e) { locDuLieuSach(); }
        });

        // Gắn sự kiện chọn ComboBox -> lọc tức thì
        ActionListener filterAction = e -> {
            if (!dangCapNhatCombo) locDuLieuSach();
        };
        cboLoaiSach.addActionListener(filterAction);
        cboTacGia.addActionListener(filterAction);
        cboTheLoai.addActionListener(filterAction);
        cboTrangThai.addActionListener(filterAction);

        filterCard.add(row, BorderLayout.CENTER);
        return filterCard;
    }

    /** Tự động quét và cập nhật danh sách Tác giả & Thể loại/Môn học từ dữ liệu sách thực tế */
    public void capNhatDanhSachBoLoc() {
        if (cboTacGia == null || cboTheLoai == null) return;
        dangCapNhatCombo = true;

        String tacGiaDangChon = (String) cboTacGia.getSelectedItem();
        String theLoaiDangChon = (String) cboTheLoai.getSelectedItem();

        Set<String> dsTacGia = new TreeSet<>();
        Set<String> dsTheLoai = new TreeSet<>();

        for (Sach s : ql.layTatCa()) {
            String tg = layTacGia(s);
            if (!tg.isEmpty()) {
                dsTacGia.add(tg);
            }
            String tl = layTheLoaiHoacChiTiet(s);
            if (!tl.isEmpty()) {
                dsTheLoai.add(tl);
            }
        }

        cboTacGia.removeAllItems();
        cboTacGia.addItem("Tất cả tác giả");
        for (String tg : dsTacGia) cboTacGia.addItem(tg);

        cboTheLoai.removeAllItems();
        cboTheLoai.addItem("Tất cả thể loại");
        for (String tl : dsTheLoai) cboTheLoai.addItem(tl);

        // Giữ lại lựa chọn cũ nếu còn tồn tại
        if (tacGiaDangChon != null) cboTacGia.setSelectedItem(tacGiaDangChon);
        if (theLoaiDangChon != null) cboTheLoai.setSelectedItem(theLoaiDangChon);

        dangCapNhatCombo = false;
    }

    /** Lấy Thể loại / Môn học / Chi tiết từ các lớp con */
    private String layTheLoaiHoacChiTiet(Sach s) {
        if (s instanceof SachGiaoTrinh) {
            return ((SachGiaoTrinh) s).getMonHoc();
        } else if (s instanceof TapChi) {
            return "Số phát hành: " + ((TapChi) s).getSoPhatHanh();
        } else if (s instanceof TruyenTranh) {
            return "Truyện tranh";
        }
        return "";
    }

    /** Lấy tác giả nếu có */
    private String layTacGia(Sach s) {
        return "";
    }

    /** Lọc kết hợp 5 tiêu chí: Từ khóa, Loại hình, Tác giả, Thể loại/Chi tiết, Trạng thái */
    public void locDuLieuSach() {
        if (dangCapNhatCombo) return;

        String tuKhoa = (txtTimKiem != null) ? txtTimKiem.getText().trim().toLowerCase() : "";
        String loaiChon = (cboLoaiSach != null) ? (String) cboLoaiSach.getSelectedItem() : null;
        String tacGiaChon = (cboTacGia != null) ? (String) cboTacGia.getSelectedItem() : null;
        String theLoaiChon = (cboTheLoai != null) ? (String) cboTheLoai.getSelectedItem() : null;
        String trangThaiChon = (cboTrangThai != null) ? (String) cboTrangThai.getSelectedItem() : null;

        List<Sach> ketQua = new ArrayList<>();
        for (Sach s : ql.layTatCa()) {
            String ma = s.getMa().toLowerCase();
            String ten = s.getTen().toLowerCase();
            String loai = s.getLoai().toLowerCase();
            String chiTiet = layTheLoaiHoacChiTiet(s).toLowerCase();
            String tacGia = layTacGia(s).toLowerCase();

            // 1. Kiểm tra từ khóa (Mã, Tên sách, Loại, Tác giả, Chi tiết...)
            if (!tuKhoa.isEmpty()) {
                boolean khop = ma.contains(tuKhoa) || ten.contains(tuKhoa)
                        || loai.contains(tuKhoa) || chiTiet.contains(tuKhoa)
                        || tacGia.contains(tuKhoa);
                if (!khop) continue;
            }

            // 2. Kiểm tra Loại sách (Giáo trình / Tạp chí / Truyện tranh)
            if (loaiChon != null && !loaiChon.equals("Tất cả loại hình") && !loaiChon.equals("Tất cả loại")) {
                if (!s.getLoai().equalsIgnoreCase(loaiChon)) continue;
            }

            // 3. Kiểm tra Tác giả
            if (tacGiaChon != null && !tacGiaChon.equals("Tất cả tác giả")) {
                if (!layTacGia(s).equalsIgnoreCase(tacGiaChon)) continue;
            }

            // 4. Kiểm tra Thể loại / Môn học / Chi tiết
            if (theLoaiChon != null && !theLoaiChon.equals("Tất cả thể loại")) {
                if (!layTheLoaiHoacChiTiet(s).equalsIgnoreCase(theLoaiChon)) continue;
            }

            // 5. Kiểm tra Trạng thái Còn sách / Đã hết
            if (trangThaiChon != null && !trangThaiChon.equals("Tất cả trạng thái")) {
                boolean con = s.getSoLuong() > 0;
                if (trangThaiChon.equals("Còn sách") && !con) continue;
                if (trangThaiChon.equals("Đã hết") && con) continue;
            }

            ketQua.add(s);
        }

        hienThi(ketQua);
    }

    /** Nạp lại danh sách bộ lọc và dữ liệu hiển thị từ service */
    public void napLai() {
        capNhatDanhSachBoLoc();
        locDuLieuSach();
    }

    private void hienThi(List<Sach> ds) {
        model.setRowCount(0);
        modelBia.clear();
        for (Sach s : ds) {
            model.addRow(new Object[]{
                    s.getMa(),
                    s.getTen(),
                    s.getLoai(),
                    layTheLoaiHoacChiTiet(s),
                    s.getSoLuong()
            });
            modelBia.addElement(s);
        }
    }

    private void doiCheDo() {
        cardLayout.show(khuVuc, rdoBia.isSelected() ? CHE_DO_BIA : CHE_DO_BANG);
    }

    /** Mỗi ô trong lưới: ảnh bìa, tên sách (tối đa 2 dòng) và số lượng còn. */
    private static class BiaRenderer extends JPanel implements ListCellRenderer<Sach> {
        private final JLabel lblAnh = new JLabel();
        private final JLabel lblTen = new JLabel("", SwingConstants.CENTER);
        private final JLabel lblSl = new JLabel("", SwingConstants.CENTER);

        BiaRenderer() {
            super(new BorderLayout(0, 4));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            lblAnh.setHorizontalAlignment(SwingConstants.CENTER);
            lblSl.setFont(lblSl.getFont().deriveFont(Font.PLAIN, 11f));
            JPanel chu = new JPanel(new BorderLayout());
            chu.setOpaque(false);
            chu.add(lblTen, BorderLayout.CENTER);
            chu.add(lblSl, BorderLayout.SOUTH);
            add(lblAnh, BorderLayout.NORTH);
            add(chu, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Sach> list, Sach s, int index,
                                                      boolean chon, boolean focus) {
            lblAnh.setIcon(AnhBia.lay(s));
            lblTen.setText("<html><div style='text-align:center;width:" + (AnhBia.RONG - 22) + "px'>"
                    + escape(s.getTen()) + "</div></html>");
            lblSl.setText(s.getMa() + "  |  còn " + s.getSoLuong());
            lblSl.setForeground(s.getSoLuong() <= 0 ? new Color(176, 0, 32) : Color.GRAY);
            setBackground(chon ? list.getSelectionBackground() : list.getBackground());
            lblTen.setForeground(chon ? list.getSelectionForeground() : list.getForeground());
            setOpaque(true);
            return this;
        }

        private static String escape(String t) {
            return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    private void datAnhBia() {
        String ma = maDangChon();
        if (ma == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh bìa cho sách " + ma);
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            AnhBia.datAnh(ma, fc.getSelectedFile());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        capNhatHienThi();
    }

    private void goAnhBia() {
        String ma = maDangChon();
        if (ma == null) return;
        if (!AnhBia.coAnhThat(ma)) {
            JOptionPane.showMessageDialog(this, "Sách này đang dùng bìa mặc định");
            return;
        }
        AnhBia.xoaAnh(ma);
        capNhatHienThi();
    }

    /** Vẽ lại danh sách nhưng giữ nguyên ô tìm kiếm / bộ lọc / sách đang chọn. */
    private void capNhatHienThi() {
        String ma = null;
        Sach chon = listBia.getSelectedValue();
        if (chon != null) ma = chon.getMa();
        locDuLieuSach();
        if (ma != null) {
            for (int i = 0; i < modelBia.size(); i++) {
                if (modelBia.get(i).getMa().equals(ma)) { listBia.setSelectedIndex(i); break; }
            }
        }
    }

    private String maDangChon() {
        if (rdoBia.isSelected()) {
            Sach s = listBia.getSelectedValue();
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Hãy chọn một cuốn sách");
                return null;
            }
            return s.getMa();
        }
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
                    ql.luuFile();
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
