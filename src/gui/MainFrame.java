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

        SachPanel sachPanel = new SachPanel(ql);
        DocGiaPanel docGiaPanel = new DocGiaPanel(ql);
        MuonTraPanel muonTraPanel = new MuonTraPanel(ql);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sách", sachPanel);
        tabs.addTab("Độc giả", docGiaPanel);
        tabs.addTab("Mượn/Trả", muonTraPanel);
        tabs.addTab("Thống kê", taoTabThongKe());

        // Dữ liệu dùng chung nên mỗi lần chuyển tab thì nạp lại tab đó
        tabs.addChangeListener(e -> {
            Component tab = tabs.getSelectedComponent();
            if (tab == sachPanel) sachPanel.napLai();
            else if (tab == docGiaPanel) docGiaPanel.napLai();
            else if (tab == muonTraPanel) muonTraPanel.napLai();
        });
        add(tabs, BorderLayout.CENTER);
    }

    // Tab Thống kê: làm sau, chờ thêm hàm vào service
    private JPanel taoTabThongKe() {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel("Chức năng thống kê sẽ được bổ sung sau."));
        return p;
    }
}
