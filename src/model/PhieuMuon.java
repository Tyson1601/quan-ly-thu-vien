package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PhieuMuon {
    public static final int SO_NGAY_TOI_DA = 14;

    private String maPhieu;
    private Sach sach;
    private DocGia docGia;
    private LocalDate ngayMuon;
    private LocalDate ngayTra;   // null nếu chưa trả

    public PhieuMuon(String maPhieu, Sach sach, DocGia docGia) {
        this.maPhieu = maPhieu;
        this.sach = sach;
        this.docGia = docGia;
        this.ngayMuon = LocalDate.now();
    }

    public void traSach() { this.ngayTra = LocalDate.now(); }
    public boolean daTra() { return ngayTra != null; }

    public int soNgayTre() {
        LocalDate den = (ngayTra != null) ? ngayTra : LocalDate.now();
        long soNgay = ChronoUnit.DAYS.between(ngayMuon, den);
        return (int) Math.max(0, soNgay - SO_NGAY_TOI_DA);
    }

    public double tinhTienPhat() { return sach.tinhPhiMuon(soNgayTre()); }

    public String getMaPhieu() { return maPhieu; }
    public Sach getSach() { return sach; }
    public DocGia getDocGia() { return docGia; }
    public LocalDate getNgayMuon() { return ngayMuon; }
    public LocalDate getNgayTra() { return ngayTra; }
}