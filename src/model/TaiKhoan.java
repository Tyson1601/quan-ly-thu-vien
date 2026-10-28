package model;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Tài khoản đăng nhập. Mật khẩu KHÔNG được lưu dạng chữ: chỉ lưu bản băm (PBKDF2)
 * cùng một chuỗi "muối" ngẫu nhiên riêng cho từng tài khoản, nên không ai (kể cả
 * người mở file dữ liệu) đọc lại được mật khẩu gốc.
 * Tài khoản người dùng gắn với một độc giả qua maDocGia (admin thì maDocGia = null).
 */
public class TaiKhoan implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final int SO_VONG_LAP = 65536;
    private static final int DO_DAI_KHOA_BIT = 256;

    public enum VaiTro { ADMIN, NGUOI_DUNG }

    private final String tenDangNhap;
    private final VaiTro vaiTro;
    private final String maDocGia;      // null với admin
    private String email;
    private String muoi;                // Base64
    private String bamMatKhau;          // Base64

    public TaiKhoan(String tenDangNhap, String matKhau, VaiTro vaiTro, String maDocGia, String email) {
        this.tenDangNhap = tenDangNhap;
        this.vaiTro = vaiTro;
        this.maDocGia = maDocGia;
        this.email = (email == null) ? "" : email;
        datMatKhau(matKhau);
    }

    /** Đặt (hoặc đổi) mật khẩu: tạo muối mới rồi băm. */
    public final void datMatKhau(String matKhau) {
        byte[] m = new byte[16];
        new SecureRandom().nextBytes(m);
        this.muoi = Base64.getEncoder().encodeToString(m);
        this.bamMatKhau = Base64.getEncoder().encodeToString(bam(matKhau, m));
    }

    public boolean khopMatKhau(String matKhau) {
        if (matKhau == null) return false;
        byte[] m = Base64.getDecoder().decode(muoi);
        byte[] daLuu = Base64.getDecoder().decode(bamMatKhau);
        return MessageDigest.isEqual(daLuu, bam(matKhau, m));
    }

    private static byte[] bam(String matKhau, byte[] muoi) {
        try {
            PBEKeySpec spec = new PBEKeySpec(matKhau.toCharArray(), muoi, SO_VONG_LAP, DO_DAI_KHOA_BIT);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không băm được mật khẩu", e);
        }
    }

    public String getTenDangNhap() { return tenDangNhap; }
    public VaiTro getVaiTro() { return vaiTro; }
    public boolean laAdmin() { return vaiTro == VaiTro.ADMIN; }
    public String getMaDocGia() { return maDocGia; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = (email == null) ? "" : email; }
}