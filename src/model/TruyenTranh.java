package model;

public class TruyenTranh extends Sach {
    public TruyenTranh(String ma, String ten, int soLuong) {
        super(ma, ten, soLuong);
    }

    @Override public double tinhPhiMuon(int soNgay) { return soNgay * 1000; }
    @Override public String getLoai() { return "Truyện tranh"; }
}