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
        top.add(Box.createHorizontalStrut(16));
        top.add(new JLabel("Hiển thị:"));
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(rdoBang);
        nhom.add(rdoBia);
        top.add(rdoBang);
        top.add(rdoBia);
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

        // Lưới ảnh bìa: JList xếp ngang, tự xuống dòng theo bề rộng cửa sổ
        listBia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listBia.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listBia.setVisibleRowCount(-1);
        listBia.setFixedCellWidth(AnhBia.RONG + 30);
        listBia.setFixedCellHeight(AnhBia.CAO + 66);
        listBia.setCellRenderer(new BiaRenderer());

        khuVuc.add(new JScrollPane(table), CHE_DO_BANG);
        khuVuc.add(new JScrollPane(listBia), CHE_DO_BIA);
        add(khuVuc, BorderLayout.CENTER);

        // Nút thao tác (chỉ admin)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (laAdmin) {
            JButton btnThem = new JButton("Thêm");
            JButton btnSua = new JButton("Sửa");
            JButton btnXoa = new JButton("Xóa");
            bottom.add(btnThem);
            bottom.add(btnSua);
            bottom.add(btnXoa);
            JButton btnAnh = new JButton("Đặt ảnh bìa");
            JButton btnGoAnh = new JButton("Gỡ ảnh bìa");
            bottom.add(btnAnh);
            bottom.add(btnGoAnh);
            btnAnh.addActionListener(e -> datAnhBia());
            btnGoAnh.addActionListener(e -> goAnhBia());
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

        rdoBang.addActionListener(e -> doiCheDo());
        rdoBia.addActionListener(e -> doiCheDo());
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
        modelBia.clear();
        for (Sach s : ds) {
            model.addRow(new Object[]{s.getMa(), s.getTen(), s.getLoai(), s.getSoLuong()});
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
            // HTML để tên dài tự xuống dòng; chiều cao ô cố định nên tên quá dài sẽ bị cắt
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
        loc();
        if (ma != null) {
            for (int i = 0; i < modelBia.size(); i++) {
                if (modelBia.get(i).getMa().equals(ma)) { listBia.setSelectedIndex(i); break; }
            }
        }
    }

    private String maDangChon() {
        if (rdoBia.isSelected()) {   // đang xem ảnh bìa thì lấy sách chọn trong lưới
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