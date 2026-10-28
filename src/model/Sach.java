package model;

import java.io.Serializable;

public abstract class Sach implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ma;
    private String ten;
    private int soLuong;

    public Sach(String ma, String ten, int soLuong) {
        this.ma = ma;
        this.ten = ten;
        this.soLuong = soLuong;
    }

    public abstract double tinhPhiMuon(int soNgay);
    public abstract String getLoai();

    public String getMa() { return ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
}