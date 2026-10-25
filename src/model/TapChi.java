package model;

public class TapChi extends Sach {
    private int soPhatHanh;

    public TapChi(String ma, String ten, int soLuong, int soPhatHanh) {
        super(ma, ten, soLuong);
        this.soPhatHanh = soPhatHanh;
    }

    public int getSoPhatHanh() { return soPhatHanh; }

    @Override public double tinhPhiMuon(int soNgay) { return soNgay * 500; }
    @Override public String getLoai() { return "Tạp chí"; }
}