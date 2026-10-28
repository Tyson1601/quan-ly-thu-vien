package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PhieuMuon implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SO_NGAY_TOI_DA = 14;
    public static final int SO_NGAY_GIA_HAN = 7;
    public static final int SO_LAN_GIA_HAN_TOI_DA = 2;

    private String maPhieu;
    private Sach sach;
    private DocGia docGia;
    private LocalDate ngayMuon;
    private LocalDate ngayTra;
    private int soLanGiaHan;

    public PhieuMuon(String maPhieu, Sach sach, DocGia docGia) {
        this.maPhieu = maPhieu;
        this.sach = sach;
        this.docGia = docGia;
        this.ngayMuon = DongHo.homNay();
    }

    public void traSach() { this.ngayTra = DongHo.homNay(); }
    public boolean daTra() { return ngayTra != null; }

    public int getSoNgayDuocMuon() { return SO_NGAY_TOI_DA + soLanGiaHan * SO_NGAY_GIA_HAN; }
    public LocalDate getHanTra() { return ngayMuon.plusDays(getSoNgayDuocMuon()); }

    public int soNgayTre() {
        LocalDate den = (ngayTra != null) ? ngayTra : DongHo.homNay();
        long soNgay = ChronoUnit.DAYS.between(ngayMuon, den);
        return (int) Math.max(0, soNgay - getSoNgayDuocMuon());
    }

    public double tinhTienPhat() { return sach.tinhPhiMuon(soNgayTre()); }

    public boolean coTheGiaHan() { return !daTra() && soLanGiaHan < SO_LAN_GIA_HAN_TOI_DA && soNgayTre() == 0; }
    public void giaHan() { soLanGiaHan++; }

    public String getMaPhieu() { return maPhieu; }
    public Sach getSach() { return sach; }
    public DocGia getDocGia() { return docGia; }
    public LocalDate getNgayMuon() { return ngayMuon; }
    public LocalDate getNgayTra() { return ngayTra; }
    public int getSoLanGiaHan() { return soLanGiaHan; }
}