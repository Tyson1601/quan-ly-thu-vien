package model;

public class SachGiaoTrinh extends Sach {
    private String monHoc;

    public SachGiaoTrinh(String ma, String ten, int soLuong, String monHoc) {
        super(ma, ten, soLuong);
        this.monHoc = monHoc;
    }

    public String getMonHoc() { return monHoc; }

    @Override public double tinhPhiMuon(int soNgay) { return soNgay * 2000; }
    @Override public String getLoai() { return "Giáo trình"; }
}