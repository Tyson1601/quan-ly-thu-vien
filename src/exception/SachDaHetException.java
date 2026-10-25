package exception;

public class SachDaHetException extends Exception {
    public SachDaHetException(String ten) {
        super("Sách \"" + ten + "\" đã hết");
    }
}