package service;

import exception.TrungMaException;
import model.*;

/** Dữ liệu mẫu để chương trình có sẵn sách và độc giả ngay lần chạy đầu tiên. */
public class DuLieuMau {

    public static void nap(QuanLyThuVien ql) throws TrungMaException {
        // ===== TRUYỆN TRANH =====
        ql.them(new TruyenTranh("TT01", "Doraemon - Tập 1", 5));
        ql.them(new TruyenTranh("TT02", "Doraemon - Tập 2", 4));
        ql.them(new TruyenTranh("TT03", "Thám tử lừng danh Conan - Tập 1", 5));
        ql.them(new TruyenTranh("TT04", "Thám tử lừng danh Conan - Tập 2", 3));
        ql.them(new TruyenTranh("TT05", "Dragon Ball - Tập 1", 4));
        ql.them(new TruyenTranh("TT06", "One Piece - Tập 1", 5));
        ql.them(new TruyenTranh("TT07", "Naruto - Tập 1", 3));
        ql.them(new TruyenTranh("TT08", "Slam Dunk - Tập 1", 2));
        ql.them(new TruyenTranh("TT09", "Thần đồng đất Việt - Tập 1", 4));
        ql.them(new TruyenTranh("TT10", "Kuroko - Tập 1", 2));

        // ===== SÁCH GIÁO TRÌNH =====
        ql.them(new SachGiaoTrinh("GT01", "Lập trình hướng đối tượng với Java", 6, "Lập trình hướng đối tượng"));
        ql.them(new SachGiaoTrinh("GT02", "Cấu trúc dữ liệu và giải thuật", 5, "Cấu trúc dữ liệu"));
        ql.them(new SachGiaoTrinh("GT03", "Cơ sở dữ liệu", 4, "Cơ sở dữ liệu"));
        ql.them(new SachGiaoTrinh("GT04", "Mạng máy tính", 4, "Mạng máy tính"));
        ql.them(new SachGiaoTrinh("GT05", "Hệ điều hành", 3, "Hệ điều hành"));
        ql.them(new SachGiaoTrinh("GT06", "Giải tích 1", 5, "Toán cao cấp"));
        ql.them(new SachGiaoTrinh("GT07", "Đại số tuyến tính", 5, "Toán cao cấp"));
        ql.them(new SachGiaoTrinh("GT08", "Kỹ thuật lập trình C", 4, "Lập trình cơ bản"));

        // ===== TẠP CHÍ =====
        ql.them(new TapChi("TC01", "Tạp chí Tin học và Đời sống", 3, 245));
        ql.them(new TapChi("TC02", "Tạp chí Khoa học và Công nghệ", 3, 118));
        ql.them(new TapChi("TC03", "Tạp chí Bưu chính Viễn thông", 2, 76));
        ql.them(new TapChi("TC04", "Tạp chí Thế giới Vi tính", 2, 310));
        ql.them(new TapChi("TC05", "Tạp chí Khám phá", 3, 97));
        ql.them(new TapChi("TC06", "Tạp chí Tia Sáng", 2, 52));

        // ===== ĐỘC GIẢ MẪU =====
        ql.themDocGia(new DocGia("DG01", "Nguyễn Văn An", "0901234567"));
        ql.themDocGia(new DocGia("DG02", "Trần Thị Bình", "0912345678"));
        ql.themDocGia(new DocGia("DG03", "Lê Minh Cường", "0923456789"));
    }
}
