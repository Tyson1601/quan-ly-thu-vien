package exception;

public class TenDangNhapTonTaiException extends Exception {
    public TenDangNhapTonTaiException(String ten) {
        super("Tên đăng nhập \"" + ten + "\" đã có người dùng");
    }
}
