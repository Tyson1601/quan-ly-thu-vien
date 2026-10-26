package service;

import exception.*;
import model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuanLyThuVien {
    private final List<Sach> dsSach = new ArrayList<>();
    private final List<DocGia> dsDocGia = new ArrayList<>();
    private final List<PhieuMuon> dsPhieu = new ArrayList<>();

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
        boolean dangMuon = dsPhieu.stream()
                .anyMatch(p -> p.getSach() == s && !p.daTra());
        if (dangMuon) throw new SachDangMuonException(s.getTen());
        dsSach.remove(s);
    }

    public List<Sach> layTatCa() { return new ArrayList<>(dsSach); }

    public List<Sach> tim(String tuKhoa) {
        String k = tuKhoa.toLowerCase();
        return dsSach.stream()
                .filter(s -> s.getTen().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    // ===== ĐỘC GIẢ =====
    public void themDocGia(DocGia d) throws TrungMaException {
        if (d == null) throw new IllegalArgumentException("Độc giả không hợp lệ");
        kiemTraMaTen(d.getMa(), d.getTen());
        if (timDocGiaTheoMa(d.getMa()) != null) throw new TrungMaException(d.getMa());
        dsDocGia.add(d);
    }

    public List<DocGia> layTatCaDocGia() { return new ArrayList<>(dsDocGia); }

    // ===== MƯỢN / TRẢ =====
    public PhieuMuon muon(String maSach, String maDocGia)
            throws SachDaHetException, KhongTimThayException {
        Sach s = laySach(maSach);
        DocGia d = layDocGia(maDocGia);
        if (s.getSoLuong() <= 0) throw new SachDaHetException(s.getTen());

        s.setSoLuong(s.getSoLuong() - 1);
        PhieuMuon p = new PhieuMuon("P" + (dsPhieu.size() + 1), s, d);
        dsPhieu.add(p);
        return p;
    }

    /** Trả sách, trả về tiền phạt (0 nếu không trễ). */
    public double tra(String maPhieu) throws KhongTimThayException {
        PhieuMuon p = layPhieu(maPhieu);
        if (p.daTra()) return 0;
        p.traSach();
        p.getSach().setSoLuong(p.getSach().getSoLuong() + 1);
        return p.tinhTienPhat();
    }

    public List<PhieuMuon> layTatCaPhieu() { return new ArrayList<>(dsPhieu); }

    // ===== THỐNG KÊ =====
    public int tongSoSachConLai() {
        return dsSach.stream().mapToInt(Sach::getSoLuong).sum();
    }

    public List<PhieuMuon> layPhieuChuaTra() {
        return dsPhieu.stream().filter(p -> !p.daTra()).collect(Collectors.toList());
    }

    /** Sách có nhiều phiếu mượn nhất, null nếu chưa ai mượn. */
    public Sach sachMuonNhieuNhat() {
        return dsPhieu.stream()
                .collect(Collectors.groupingBy(PhieuMuon::getSach, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ===== KIỂM TRA DỮ LIỆU =====
    private void kiemTraMaTen(String ma, String ten) {
        if (ma == null || ma.isBlank()) throw new IllegalArgumentException("Mã không được để trống");
        if (ten == null || ten.isBlank()) throw new IllegalArgumentException("Tên không được để trống");
    }

    private void kiemTraSoLuong(int soLuong) {
        if (soLuong < 0) throw new IllegalArgumentException("Số lượng không được âm");
    }

    // ===== HÀM DÙNG CHUNG (private) =====
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