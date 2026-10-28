package exception;

public class SaiDangNhapException extends Exception {
    public SaiDangNhapException() {
        super("Sai tên đăng nhập hoặc mật khẩu");
    }
}