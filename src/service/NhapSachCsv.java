package service;

import exception.TrungMaException;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Nhập sách hàng loạt từ file CSV (UTF-8).
 *
 * Các cột (có hoặc không có dòng tiêu đề đều được):
 *   Mã, Tên, Loại, Số lượng, Thông tin thêm
 * - Loại: "Truyện tranh", "Giáo trình" hoặc "Tạp chí" (không phân biệt hoa thường, có/không dấu)
 * - Thông tin thêm: Giáo trình = môn học, Tạp chí = số phát hành (số nguyên), Truyện tranh = bỏ trống
 * Dấu phân cách là dấu phẩy "," hoặc chấm phẩy ";" (tự nhận biết, Excel tiếng Việt hay dùng ";").
 * Dòng nào lỗi thì bỏ qua và ghi lý do, các dòng hợp lệ vẫn được thêm.
 */
public class NhapSachCsv {

    /** Kết quả nhập: số sách thêm được và danh sách lỗi theo từng dòng. */
    public static class KetQua {
        public int soThem = 0;
        public final List<String> loi = new ArrayList<>();

        public String tomTat() {
            StringBuilder sb = new StringBuilder("Đã thêm " + soThem + " sách");
            if (!loi.isEmpty()) {
                sb.append(", bỏ qua ").append(loi.size()).append(" dòng lỗi:");
                int toiDa = 15;
                for (int i = 0; i < loi.size() && i < toiDa; i++) sb.append("\n- ").append(loi.get(i));
                if (loi.size() > toiDa) sb.append("\n... và ").append(loi.size() - toiDa).append(" lỗi khác");
            }
            return sb.toString();
        }
    }

    public static KetQua nhap(QuanLyThuVien ql, String duongDan) throws IOException {
        String noiDung = new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        if (noiDung.startsWith("﻿")) noiDung = noiDung.substring(1);   // bỏ BOM do Excel thêm vào

        char phanCach = noiDung.split("\r?\n", 2)[0].contains(";") ? ';' : ',';
        List<String[]> dong = docCsv(noiDung, phanCach);
        KetQua kq = new KetQua();

        for (int i = 0; i < dong.size(); i++) {
            String[] c = dong.get(i);
            int soDong = i + 1;
            if (c.length == 1 && c[0].trim().isEmpty()) continue;   // dòng trống

            // Dòng tiêu đề: cột "Số lượng" không phải số
            if (i == 0 && c.length >= 4 && !laSoNguyen(c[3])) continue;

            try {
                Sach s = taoSach(c);
                ql.them(s);
                kq.soThem++;
            } catch (TrungMaException ex) {
                kq.loi.add("Dòng " + soDong + ": " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                kq.loi.add("Dòng " + soDong + ": " + ex.getMessage());
            }
        }
        return kq;
    }

    private static Sach taoSach(String[] c) {
        if (c.length < 4) throw new IllegalArgumentException("thiếu cột (cần ít nhất: Mã, Tên, Loại, Số lượng)");
        String ma = c[0].trim();
        String ten = c[1].trim();
        String loai = chuanHoa(c[2]);
        String soLuongStr = c[3].trim();
        String them = c.length > 4 ? c[4].trim() : "";

        if (ma.isEmpty()) throw new IllegalArgumentException("mã trống");
        if (ten.isEmpty()) throw new IllegalArgumentException("tên trống");
        if (!laSoNguyen(soLuongStr)) throw new IllegalArgumentException("số lượng \"" + soLuongStr + "\" không phải số nguyên");
        int soLuong = Integer.parseInt(soLuongStr);
        if (soLuong < 0) throw new IllegalArgumentException("số lượng không được âm");

        switch (loai) {
            case "truyen tranh":
                return new TruyenTranh(ma, ten, soLuong);
            case "giao trinh":
                return new SachGiaoTrinh(ma, ten, soLuong, them);
            case "tap chi":
                if (!laSoNguyen(them)) throw new IllegalArgumentException("tạp chí cần số phát hành là số nguyên, nhận \"" + them + "\"");
                return new TapChi(ma, ten, soLuong, Integer.parseInt(them));
            default:
                throw new IllegalArgumentException("loại \"" + c[2].trim() + "\" không hợp lệ (Truyện tranh / Giáo trình / Tạp chí)");
        }
    }

    private static boolean laSoNguyen(String s) {
        return s != null && s.trim().matches("-?\\d{1,9}");
    }

    /** Bỏ dấu tiếng Việt, về chữ thường, gộp khoảng trắng: "Giáo  Trình" -> "giao trinh". */
    private static String chuanHoa(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D');
        return n.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /** Đọc CSV có hỗ trợ ô đặt trong dấu nháy kép (chứa dấu phẩy, xuống dòng, hoặc "" thay cho "). */
    static List<String[]> docCsv(String text, char phanCach) {
        List<String[]> ketQua = new ArrayList<>();
        List<String> hang = new ArrayList<>();
        StringBuilder o = new StringBuilder();
        boolean trongNhay = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (trongNhay) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') { o.append('"'); i++; }
                    else trongNhay = false;
                } else {
                    o.append(ch);
                }
            } else if (ch == '"') {
                trongNhay = true;
            } else if (ch == phanCach) {
                hang.add(o.toString());
                o.setLength(0);
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                hang.add(o.toString());
                o.setLength(0);
                ketQua.add(hang.toArray(new String[0]));
                hang = new ArrayList<>();
            } else {
                o.append(ch);
            }
        }
        if (o.length() > 0 || !hang.isEmpty()) {   // dòng cuối không có xuống dòng
            hang.add(o.toString());
            ketQua.add(hang.toArray(new String[0]));
        }
        return ketQua;
    }
}