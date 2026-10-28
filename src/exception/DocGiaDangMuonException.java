package exception;

public class DocGiaDangMuonException extends Exception {
    public DocGiaDangMuonException(String ten) {
        super("Độc giả \"" + ten + "\" đang mượn sách chưa trả, không thể xóa");
    }
}