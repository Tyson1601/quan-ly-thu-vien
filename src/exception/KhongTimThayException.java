package exception;

public class KhongTimThayException extends Exception {
    public KhongTimThayException(String ma) {
        super("Không tìm thấy mã \"" + ma + "\"");
    }
}