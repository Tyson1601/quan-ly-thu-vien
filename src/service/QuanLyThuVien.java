package service;

import exception.*;
import model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class QuanLyThuVien {
    public static final int GIOI_HAN_MUON = 3;
    public static final String FILE_DU_LIEU = "thuvien.dat";

    private final List<Sach> dsSach = new ArrayList<>();
    private final List<DocGia> dsDocGia = new ArrayList<>();
    private final List<PhieuMuon> dsPhieu = new ArrayList<>();
    private final List<TaiKhoan> dsTaiKhoan = new ArrayList<>();

    public QuanLyThuVien() {
        damBaoCoAdmin();
    }

    // ===== SÁCH =====
    public void them(Sach s) throws TrungMaException {
        if (s == null) throw new IllegalArgumentException("Sách không hợp lệ");
        kiemTraMaTen(s.getMa(), s.getTen());
        kiemTraSoLuong(s.getSoLuong());
        if (timSachTheoMa(s.getMa()) != null) throw new TrungMaException(s.getMa());
        dsSach.add(s);
    }

    public void sua(String ma, String tenMoi, int soLuongMoi) throws KhongTimThayException {
        kiemTraMaTen(ma, tenMoi);
        kiemTraSoLuong(soLuongMoi);
        Sach s = laySach(ma);
        s.setTen(tenMoi);
        s.setSoLuong(soLuongMoi);
    }

    public void xoa(String ma) throws KhongTimThayException, SachDangMuonException {
        Sach s = laySach(ma);
        boolean dangMuon = dsPhieu.stream().anyMatch(p -> p.getSach() == s && !p.daTra());
        if (dangMuon) throw new SachDangMuonException(s.getTen());
        dsSach.remove(s);
    }

    public List<Sach> layTatCa() { return new ArrayList<>(dsSach); }

    public List<Sach> tim(String tuKhoa) {
        String k = tuKhoa.toLowerCase();
        return dsSach.stream().filter(s -> s.getTen().toLowerCase().contains(k)).collect(Collectors.toList());
    }

    // ===== ĐỘC GIẢ =====
    public void themDocGia(DocGia d) throws TrungMaException {
        if (d == null) throw new IllegalArgumentException("Độc giả không hợp lệ");
        kiemTraMaTen(d.getMa(), d.getTen());
        if (timDocGiaTheoMa(d.getMa()) != null) throw new TrungMaException(d.getMa());
        dsDocGia.add(d);
    }

    public List<DocGia> layTatCaDocGia() { return new ArrayList<>(dsDocGia); }

    public void suaDocGia(String ma, String tenMoi, String sdtMoi) throws KhongTimThayException {
        kiemTraMaTen(ma, tenMoi);
        DocGia d = layDocGia(ma);
        d.setTen(tenMoi);
        d.setSdt(sdtMoi);
    }

    public void xoaDocGia(String ma) throws KhongTimThayException, DocGiaDangMuonException {
        DocGia d = layDocGia(ma);
        if (demSachDangMuon(ma) > 0) throw new DocGiaDangMuonException(d.getTen());
        dsDocGia.remove(d);
        dsTaiKhoan.removeIf(t -> ma.equals(t.getMaDocGia()));
    }

    public int demSachDangMuon(String maDocGia) {
        return (int) dsPhieu.stream()
                .filter(p -> !p.daTra() && p.getDocGia().getMa().equals(maDocGia)).count();
    }

    // ===== MƯỢN / TRẢ =====
    public PhieuMuon muon(String maSach, String maDocGia)
            throws SachDaHetException, KhongTimThayException, VuotGioiHanMuonException {
        Sach s = laySach(maSach);
        DocGia d = layDocGia(maDocGia);
        if (demSachDangMuon(maDocGia) >= GIOI_HAN_MUON)
            throw new VuotGioiHanMuonException(d.getTen(), GIOI_HAN_MUON);
        if (s.getSoLuong() <= 0) throw new SachDaHetException(s.getTen());

        s.setSoLuong(s.getSoLuong() - 1);
        PhieuMuon p = new PhieuMuon("P" + (dsPhieu.size() + 1), s, d);
        dsPhieu.add(p);
        return p;
    }

    public double tra(String maPhieu) throws KhongTimThayException {
        PhieuMuon p = layPhieu(maPhieu);
        if (p.daTra()) return 0;
        p.traSach();
        p.getSach().setSoLuong(p.getSach().getSoLuong() + 1);
        return p.tinhTienPhat();
    }

    public List<PhieuMuon> layTatCaPhieu() { return new ArrayList<>(dsPhieu); }

    // ===== THỐNG KÊ =====
    public int tongSoSachConLai() { return dsSach.stream().mapToInt(Sach::getSoLuong).sum(); }

    public List<PhieuMuon> layPhieuChuaTra() {
        return dsPhieu.stream().filter(p -> !p.daTra()).collect(Collectors.toList());
    }

    public Sach sachMuonNhieuNhat() {
        return dsPhieu.stream()
                .collect(Collectors.groupingBy(PhieuMuon::getSach, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ===== GIA HẠN / QUÁ HẠN / LỊCH SỬ =====
    public void giaHan(String maPhieu) throws KhongTimThayException, KhongGiaHanDuocException {
        PhieuMuon p = layPhieu(maPhieu);
        if (p.daTra()) throw new KhongGiaHanDuocException("phiếu đã trả");
        if (p.soNgayTre() > 0) throw new KhongGiaHanDuocException("phiếu đã quá hạn");
        if (!p.coTheGiaHan())
            throw new KhongGiaHanDuocException("đã gia hạn tối đa " + PhieuMuon.SO_LAN_GIA_HAN_TOI_DA + " lần");
        p.giaHan();
    }

    public List<PhieuMuon> layPhieuQuaHan() {
        return dsPhieu.stream().filter(p -> !p.daTra() && p.soNgayTre() > 0).collect(Collectors.toList());
    }

    public List<PhieuMuon> layPhieuCuaDocGia(String maDocGia) {
        return dsPhieu.stream().filter(p -> p.getDocGia().getMa().equals(maDocGia)).collect(Collectors.toList());
    }

    // ===== LỌC / THỐNG KÊ THEO LOẠI =====
    public List<Sach> locTheoLoai(String loai) {
        return dsSach.stream().filter(s -> s.getLoai().equals(loai)).collect(Collectors.toList());
    }

    public Map<String, Long> thongKeTheoLoai() {
        return dsSach.stream().collect(Collectors.groupingBy(Sach::getLoai, TreeMap::new, Collectors.counting()));
    }

    // ===== XUẤT CSV =====
    public void xuatPhieuCsv(String duongDan) throws IOException {
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(duongDan), StandardCharsets.UTF_8))) {
            w.write('\uFEFF');
            w.println("Mã phiếu,Sách,Độc giả,Ngày mượn,Hạn trả,Ngày trả,Số ngày trễ,Tiền phạt");
            for (PhieuMuon p : dsPhieu) {
                w.println(String.join(",",
                        csv(p.getMaPhieu()), csv(p.getSach().getTen()), csv(p.getDocGia().getTen()),
                        p.getNgayMuon().toString(), p.getHanTra().toString(),
                        p.getNgayTra() == null ? "" : p.getNgayTra().toString(),
                        String.valueOf(p.soNgayTre()), String.valueOf((long) p.tinhTienPhat())));
            }
        }
    }

    private String csv(String v) { return "\"" + v.replace("\"", "\"\"") + "\""; }

    // ===== ĐĂNG NHẬP =====
    /** Đăng nhập. Tên đăng nhập không phân biệt hoa thường, mật khẩu thì có. */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SaiDangNhapException {
        String ten = (tenDangNhap == null) ? "" : tenDangNhap.trim();
        for (TaiKhoan t : dsTaiKhoan) {
            if (t.getTenDangNhap().equalsIgnoreCase(ten) && t.khopMatKhau(matKhau)) return t;
        }
        throw new SaiDangNhapException();
    }

    // ===== THỜI GIAN HỆ THỐNG (admin chỉnh để thuyết trình) =====
    public LocalDate ngayHienTai() { return DongHo.homNay(); }

    public boolean dangGiaLapNgay() { return DongHo.dangGiaLap(); }

    public void datNgayHienTai(LocalDate ngay) { DongHo.datNgay(ngay); }

    public void tuaNhanhNgay(int soNgay) { DongHo.tuaNhanh(soNgay); }

    public void veNgayThat() { DongHo.veThoiGianThat(); }

    /** Tổng tiền phạt tạm tính của các phiếu đang quá hạn (chưa trả). */
    public double tongTienPhatTamTinh() {
        return layPhieuQuaHan().stream().mapToDouble(PhieuMuon::tinhTienPhat).sum();
    }

    /** Tiền phạt tạm tính của riêng một độc giả (các phiếu của họ đang quá hạn). */
    public double tongTienPhatTamTinh(String maDocGia) {
        return layPhieuQuaHan().stream()
                .filter(p -> p.getDocGia().getMa().equals(maDocGia))
                .mapToDouble(PhieuMuon::tinhTienPhat).sum();
    }

    // ===== TÀI KHOẢN: ĐĂNG KÝ / HỒ SƠ / ĐỔI MẬT KHẨU =====
    /**
     * Đăng ký tài khoản mới như mạng xã hội: tạo luôn hồ sơ độc giả gắn với tài khoản.
     * Tài khoản tự đăng ký luôn có quyền NGƯỜI DÙNG. Họ tên, tên đăng nhập, mật khẩu là bắt buộc;
     * số điện thoại và email có thể để trống.
     */
    public TaiKhoan dangKy(String hoTen, String sdt, String email, String tenDangNhap, String matKhau)
            throws TenDangNhapTonTaiException {
        String ho = chuan(hoTen), dt = chuan(sdt), mail = chuan(email), ten = chuan(tenDangNhap);
        kiemTraHoSo(ho, dt, mail);
        if (!ten.matches("[A-Za-z0-9_]{3,20}"))
            throw new IllegalArgumentException(
                    "Tên đăng nhập gồm 3-20 ký tự: chữ không dấu, số hoặc dấu gạch dưới");
        kiemTraMatKhau(matKhau);
        for (TaiKhoan t : dsTaiKhoan) {
            if (t.getTenDangNhap().equalsIgnoreCase(ten)) throw new TenDangNhapTonTaiException(ten);
        }

        String ma = taoMaDocGiaMoi();
        dsDocGia.add(new DocGia(ma, ho, dt));
        TaiKhoan moi = new TaiKhoan(ten, matKhau, TaiKhoan.VaiTro.NGUOI_DUNG, ma, mail);
        dsTaiKhoan.add(moi);
        return moi;
    }

    /** Hồ sơ độc giả của tài khoản (null nếu là admin hoặc hồ sơ đã bị xóa). */
    public DocGia layDocGiaCuaTaiKhoan(TaiKhoan t) {
        if (t == null || t.getMaDocGia() == null) return null;
        return timDocGiaTheoMa(t.getMaDocGia());
    }

    public void capNhatThongTin(TaiKhoan t, String hoTen, String sdt, String email) {
        DocGia d = layDocGiaCuaTaiKhoan(t);
        if (d == null) throw new IllegalArgumentException("Tài khoản này không có hồ sơ độc giả");
        String ho = chuan(hoTen), dt = chuan(sdt), mail = chuan(email);
        kiemTraHoSo(ho, dt, mail);
        d.setTen(ho);
        d.setSdt(dt);
        t.setEmail(mail);
    }

    public void doiMatKhau(TaiKhoan t, String matKhauCu, String matKhauMoi) {
        if (!t.khopMatKhau(matKhauCu)) throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        kiemTraMatKhau(matKhauMoi);
        if (matKhauMoi.equals(matKhauCu))
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        t.datMatKhau(matKhauMoi);
    }

    private void damBaoCoAdmin() {
        boolean co = dsTaiKhoan.stream().anyMatch(t -> t.getTenDangNhap().equalsIgnoreCase("admin"));
        if (!co) dsTaiKhoan.add(new TaiKhoan("admin", "admin123", TaiKhoan.VaiTro.ADMIN, null, ""));
    }

    private String taoMaDocGiaMoi() {
        int n = dsDocGia.size() + 1;
        String ma;
        do {
            ma = String.format("DG%02d", n++);
        } while (timDocGiaTheoMa(ma) != null);
        return ma;
    }

    private static String chuan(String v) { return (v == null) ? "" : v.trim(); }

    private void kiemTraHoSo(String hoTen, String sdt, String email) {
        if (hoTen.length() < 2 || hoTen.length() > 50)
            throw new IllegalArgumentException("Họ tên phải từ 2 đến 50 ký tự");
        if (!sdt.isEmpty() && !sdt.matches("\\d{9,11}"))
            throw new IllegalArgumentException("Số điện thoại gồm 9-11 chữ số");
        if (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            throw new IllegalArgumentException("Email không hợp lệ");
    }

    private void kiemTraMatKhau(String matKhau) {
        if (matKhau == null || matKhau.length() < 6)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
    }

    // ===== LƯU / ĐỌC FILE =====
    /** Lưu vào file mặc định (thuvien.dat). */
    public void luuFile() throws IOException { luuFile(FILE_DU_LIEU); }

    public void luuFile(String duongDan) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(duongDan))) {
            out.writeObject(dsSach);
            out.writeObject(dsDocGia);
            out.writeObject(dsPhieu);
            out.writeObject(dsTaiKhoan);
        }
    }

    @SuppressWarnings("unchecked")
    public void docFile(String duongDan) throws IOException, ClassNotFoundException {
        File f = new File(duongDan);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            List<Sach> s = (List<Sach>) in.readObject();
            List<DocGia> d = (List<DocGia>) in.readObject();
            List<PhieuMuon> p = (List<PhieuMuon>) in.readObject();
            List<TaiKhoan> tk = null;
            try {
                tk = (List<TaiKhoan>) in.readObject();
            } catch (EOFException e) {
                // file lưu từ bản cũ, chưa có tài khoản
            }
            dsSach.clear();   dsSach.addAll(s);
            dsDocGia.clear(); dsDocGia.addAll(d);
            dsPhieu.clear();  dsPhieu.addAll(p);
            if (tk != null) { dsTaiKhoan.clear(); dsTaiKhoan.addAll(tk); }
            damBaoCoAdmin();
        }
    }

    // ===== KIỂM TRA DỮ LIỆU =====
    private void kiemTraMaTen(String ma, String ten) {
        if (ma == null || ma.isBlank()) throw new IllegalArgumentException("Mã không được để trống");
        if (ten == null || ten.isBlank()) throw new IllegalArgumentException("Tên không được để trống");
    }

    private void kiemTraSoLuong(int soLuong) {
        if (soLuong < 0) throw new IllegalArgumentException("Số lượng không được âm");
    }

    // ===== HÀM DÙNG CHUNG =====
    private Sach timSachTheoMa(String ma) {
        for (Sach s : dsSach) if (s.getMa().equals(ma)) return s;
        return null;
    }
    private Sach laySach(String ma) throws KhongTimThayException {
        Sach s = timSachTheoMa(ma);
        if (s == null) throw new KhongTimThayException(ma);
        return s;
    }
    private DocGia timDocGiaTheoMa(String ma) {
        for (DocGia d : dsDocGia) if (d.getMa().equals(ma)) return d;
        return null;
    }
    private DocGia layDocGia(String ma) throws KhongTimThayException {
        DocGia d = timDocGiaTheoMa(ma);
        if (d == null) throw new KhongTimThayException(ma);
        return d;
    }
    private PhieuMuon layPhieu(String ma) throws KhongTimThayException {
        for (PhieuMuon p : dsPhieu) if (p.getMaPhieu().equals(ma)) return p;
        throw new KhongTimThayException(ma);
    }
}