package exception;

public class VuotGioiHanMuonException extends Exception {
    public VuotGioiHanMuonException(String ten, int gioiHan) {
        super("Độc giả \"" + ten + "\" đã mượn tối đa " + gioiHan + " cuốn, hãy trả bớt trước khi mượn thêm");
    }
}