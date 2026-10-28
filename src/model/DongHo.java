package model;

import java.time.LocalDate;

/**
 * Đồng hồ trung tâm của hệ thống. Mọi nơi cần "hôm nay" đều gọi DongHo.homNay()
 * thay vì LocalDate.now(), nhờ đó admin có thể chỉnh ngày để thuyết trình
 * (ví dụ tua nhanh 20 ngày để thấy phiếu quá hạn và tiền phạt).
 * Ngày giả lập KHÔNG được lưu: mỗi lần mở lại chương trình sẽ về ngày thật.
 */
public final class DongHo {
    private static LocalDate ngayGiaLap = null;   // null = dùng ngày thật của máy

    private DongHo() { }

    public static LocalDate homNay() {
        return (ngayGiaLap != null) ? ngayGiaLap : LocalDate.now();
    }

    public static boolean dangGiaLap() { return ngayGiaLap != null; }

    /** Đặt ngày hệ thống. Nếu trùng ngày thật thì coi như không giả lập. */
    public static void datNgay(LocalDate ngay) {
        if (ngay == null) throw new IllegalArgumentException("Ngày không hợp lệ");
        ngayGiaLap = ngay.equals(LocalDate.now()) ? null : ngay;
    }

    public static void tuaNhanh(int soNgay) { datNgay(homNay().plusDays(soNgay)); }

    public static void veThoiGianThat() { ngayGiaLap = null; }
}
