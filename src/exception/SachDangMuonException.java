package exception;

public class SachDangMuonException extends Exception {
    public SachDangMuonException(String ten) {
        super("Sách \"" + ten + "\" đang có người mượn, không thể xóa");
    }
}