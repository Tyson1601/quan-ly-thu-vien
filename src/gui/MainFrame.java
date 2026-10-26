package gui;

import service.QuanLyThuVien;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    // DUY NHẤT một QuanLyThuVien cho cả ứng dụng, truyền xuống các panel
    private final QuanLyThuVien ql = new QuanLyThuVien();

    public MainFrame() {
        super("Quản lý thư viện");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sách", new SachPanel(ql));
        tabs.addTab("Độc giả", new DocGiaPanel(ql));
        tabs.addTab("Mượn/Trả", new JPanel());
        tabs.addTab("Thống kê", taoTabThongKe());
        add(tabs, BorderLayout.CENTER);
    }

    // Tab Thống kê: làm sau, chờ thêm hàm vào service
    private JPanel taoTabThongKe() {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel("Chức năng thống kê sẽ được bổ sung sau."));
        return p;
    }
}
