import exception.*;
import model.*;
import service.QuanLyThuVien;

public class TestConsole {
    public static void main(String[] args) {
        QuanLyThuVien ql = new QuanLyThuVien();
        try {
            ql.them(new TruyenTranh("S1", "Doraemon", 2));
            ql.them(new SachGiaoTrinh("S2", "OOP Java", 1, "Lập trình"));
            ql.themDocGia(new DocGia("D1", "Nguyễn Văn A", "0901234567"));

            PhieuMuon p = ql.muon("S2", "D1");
            System.out.println("Đã mượn, phiếu " + p.getMaPhieu());

            try { ql.muon("S2", "D1"); }                // hết sách
            catch (SachDaHetException e) { System.out.println("Lỗi: " + e.getMessage()); }

            try { ql.them(new TapChi("S1", "Trùng", 1, 5)); }   // trùng mã
            catch (TrungMaException e) { System.out.println("Lỗi: " + e.getMessage()); }

            try { ql.muon("S99", "D1"); }               // sách không tồn tại
            catch (KhongTimThayException e) { System.out.println("Lỗi: " + e.getMessage()); }

            System.out.println("Tiền phạt: " + ql.tra(p.getMaPhieu()));
            System.out.println("Tìm 'dora': " + ql.tim("dora").size() + " kết quả");
        } catch (Exception e) {
            System.out.println("Lỗi không mong đợi: " + e.getMessage());
        }
    }
}
