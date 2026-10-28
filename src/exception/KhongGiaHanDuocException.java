package exception;

public class KhongGiaHanDuocException extends Exception {
    public KhongGiaHanDuocException(String lyDo) {
        super("Không thể gia hạn: " + lyDo);
    }
}