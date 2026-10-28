package gui;

import model.Sach;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý ảnh bìa sách. Ảnh nằm trong thư mục "bia" cạnh file thuvien.dat, đặt tên theo mã sách
 * (ví dụ bia/TT01.png). Sách chưa có ảnh thì tự vẽ một bìa mặc định (màu theo loại + tên sách).
 */
public class AnhBia {
    public static final String THU_MUC = "bia";
    public static final int RONG = 120;
    public static final int CAO = 170;
    private static final String[] DUOI = {"png", "jpg", "jpeg", "gif"};

    private static final Map<String, ImageIcon> BO_NHO = new HashMap<>();

    private AnhBia() {}

    /** Ảnh bìa (đã co về RONG x CAO) của sách: ảnh thật nếu có, không thì bìa mặc định. */
    public static ImageIcon lay(Sach s) {
        String khoa = s.getMa() + "|" + s.getTen();
        ImageIcon icon = BO_NHO.get(khoa);
        if (icon == null) {
            BufferedImage goc = docAnhThat(s.getMa());
            icon = new ImageIcon(goc != null ? vuaKhung(goc) : veMacDinh(s));
            BO_NHO.put(khoa, icon);
        }
        return icon;
    }

    public static boolean coAnhThat(String ma) { return timFile(ma) != null; }

    /** Sao chép ảnh người dùng chọn vào thư mục bia/ với tên là mã sách (thay ảnh cũ nếu có). */
    public static void datAnh(String ma, File nguon) throws IOException {
        if (ImageIO.read(nguon) == null) throw new IOException("File không phải ảnh hợp lệ (dùng png, jpg, gif)");
        File thuMuc = new File(THU_MUC);
        if (!thuMuc.exists() && !thuMuc.mkdirs()) throw new IOException("Không tạo được thư mục " + THU_MUC);
        for (String d : DUOI) new File(thuMuc, ma + "." + d).delete();
        String ten = nguon.getName().toLowerCase();
        String duoi = ten.endsWith(".png") ? "png" : ten.endsWith(".gif") ? "gif" : "jpg";
        Files.copy(nguon.toPath(), new File(thuMuc, ma + "." + duoi).toPath(), StandardCopyOption.REPLACE_EXISTING);
        BO_NHO.clear();
    }

    /** Gỡ ảnh bìa riêng của sách (quay về bìa mặc định). */
    public static void xoaAnh(String ma) {
        for (String d : DUOI) new File(THU_MUC, ma + "." + d).delete();
        BO_NHO.clear();
    }

    private static File timFile(String ma) {
        for (String d : DUOI) {
            File f = new File(THU_MUC, ma + "." + d);
            if (f.isFile()) return f;
        }
        return null;
    }

    private static BufferedImage docAnhThat(String ma) {
        File f = timFile(ma);
        if (f == null) return null;
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            return null;
        }
    }

    /** Co ảnh cho vừa khung RONG x CAO, giữ tỉ lệ, nền xám nhạt. */
    private static BufferedImage vuaKhung(BufferedImage goc) {
        BufferedImage kq = new BufferedImage(RONG, CAO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = kq.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setColor(new Color(238, 238, 238));
        g.fillRect(0, 0, RONG, CAO);
        double tiLe = Math.min((double) RONG / goc.getWidth(), (double) CAO / goc.getHeight());
        int w = Math.max(1, (int) (goc.getWidth() * tiLe));
        int h = Math.max(1, (int) (goc.getHeight() * tiLe));
        g.drawImage(goc, (RONG - w) / 2, (CAO - h) / 2, w, h, null);
        g.dispose();
        return kq;
    }

    private static BufferedImage veMacDinh(Sach s) {
        Color mau;
        switch (s.getLoai()) {
            case "Truyện tranh": mau = new Color(0xE0, 0x7A, 0x1F); break;
            case "Giáo trình":   mau = new Color(0x2E, 0x6F, 0xB7); break;
            case "Tạp chí":      mau = new Color(0x2E, 0x9E, 0x6B); break;
            default:             mau = new Color(0x70, 0x70, 0x80);
        }
        BufferedImage kq = new BufferedImage(RONG, CAO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = kq.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, mau.brighter(), 0, CAO, mau.darker()));
        g.fillRect(0, 0, RONG, CAO);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(0, 0, 8, CAO);   // gáy sách

        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.drawString(s.getLoai().toUpperCase(), 16, 18);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int y = 48;
        for (String dong : tachDong(s.getTen(), fm, RONG - 28)) {
            if (y > CAO - 12) break;
            g.drawString(dong, 16, y);
            y += fm.getHeight();
        }
        g.dispose();
        return kq;
    }

    /** Ngắt tên sách thành nhiều dòng vừa chiều rộng cho trước (tối đa 6 dòng). */
    private static java.util.List<String> tachDong(String ten, FontMetrics fm, int rong) {
        java.util.List<String> ds = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String tu : ten.split("\\s+")) {
            String thu = cur.length() == 0 ? tu : cur + " " + tu;
            if (fm.stringWidth(thu) <= rong || cur.length() == 0) {
                cur = new StringBuilder(thu);
            } else {
                ds.add(cur.toString());
                cur = new StringBuilder(tu);
            }
            if (ds.size() >= 6) break;
        }
        if (cur.length() > 0 && ds.size() < 6) ds.add(cur.toString());
        return ds;
    }
}