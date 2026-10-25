package exception;

public class TrungMaException extends Exception {
    public TrungMaException(String ma) {
        super("Mã \"" + ma + "\" đã tồn tại");
    }
}