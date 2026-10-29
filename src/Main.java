import gui.DangNhapDialog;
import gui.MainFrame;
import gui.UIStyle;
import model.TaiKhoan;
import service.DuLieuMau;
import service.QuanLyThuVien;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        // 1. Ép Java render font bằng công nghệ Sub-pixel LCD (Mượt hơn "on" thường rất nhiều)
        System.setProperty("awt.useSystemAAFontSettings", "lcd_hrgb");
        System.setProperty("swing.aatext", "true");

        // 2. Kích hoạt giao diện hệ thống trước khi mở MainFrame
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 3. Tải font ngoài nếu có
        UIStyle.loadCustomFont();

        SwingUtilities.invokeLater(() -> {
            QuanLyThuVien ql = new QuanLyThuVien();   // duy nhất một đối tượng cho cả chương trình
            taiDuLieu(ql);
            hienDangNhap(ql);
        });
    }

    /** Đọc dữ liệu (sách, độc giả, phiếu, tài khoản) trước khi đăng nhập. Lần đầu thì nạp sách mẫu. */
    private static void taiDuLieu(QuanLyThuVien ql) {
        boolean lanDauChay = !new File(QuanLyThuVien.FILE_DU_LIEU).exists();
        try {
            ql.docFile(QuanLyThuVien.FILE_DU_LIEU);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Không đọc được dữ liệu: " + ex.getMessage()
                    + "\nNếu file thuvien.dat bị lỗi, hãy xóa nó rồi chạy lại.");
        }
        if (lanDauChay) {
            try {
                DuLieuMau.nap(ql);
                ql.luuFile();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Không nạp được dữ liệu mẫu: " + ex.getMessage());
            }
        }
    }

    /** Hiện màn hình đăng nhập rồi mở cửa sổ chính; đăng xuất thì quay lại đây. */
    private static void hienDangNhap(QuanLyThuVien ql) {
        TaiKhoan taiKhoan = new DangNhapDialog(null, ql).hienThi();
        if (taiKhoan == null) {   // đóng cửa sổ đăng nhập
            System.exit(0);
        }
        new MainFrame(ql, taiKhoan, () -> hienDangNhap(ql)).setVisible(true);
    }
}
