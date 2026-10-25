package service;

import exception.*;
import model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuanLyThuVien {
    private final List<Sach> dsSach = new ArrayList<>();
    private final List<DocGia> dsDocGia = new ArrayList<>();
    private final List<PhieuMuon> dsPhieu = new ArrayList<>();

    // ===== SÁCH =====
    public void them(Sach s) throws TrungMaException {
        if (timSachTheoMa(s.getMa()) != null) throw new TrungMaException(s.getMa());
        dsSach.add(s);
    }

    public void sua(String ma, String tenMoi, int soLuongMoi) throws KhongTimThayException {
        Sach s = laySach(ma);
        s.setTen(tenMoi);
        s.setSoLuong(soLuongMoi);
    }

    public void xoa(String ma) throws KhongTimThayException {
        dsSach.remove(laySach(ma));
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