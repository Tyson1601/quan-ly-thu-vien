package model;

import java.io.Serializable;

public class DocGia implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ma;
    private String ten;
    private String sdt;

    public DocGia(String ma, String ten, String sdt) {
        this.ma = ma;
        this.ten = ten;
        this.sdt = sdt;
    }

    public String getMa() { return ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
}