package thu_vien;

import java.awt.BorderLayout; // Giúp chia không gian cửa sổ thành 5 vùng riêng biệt: NORTH, SOUTH, EAST, WEST, CENTER
import java.awt.GridLayout; // Sắp xếp các thành phần giao diện theo dạng lưới chia đều các hàng và cột (dùng cho form nhập liệu mã phiếu, mã sách)
import java.sql.Connection; // Quản lý phiên kết nối vật lý giữa ứng dụng Java và cơ sở dữ liệu SQLite
import java.sql.PreparedStatement; // Thực thi câu lệnh SQL có tham số (dùng dấu ?), giúp chống SQL Injection và tăng hiệu suất
import java.sql.ResultSet; // Bảng tạm lưu trữ tập hợp dữ liệu kết quả trả về sau khi thực hiện câu lệnh truy vấn SELECT
import java.sql.SQLException; // Lớp ngoại lệ chuyên bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu
import java.time.DayOfWeek; // Đại diện cho các ngày trong tuần (thứ Hai, thứ Ba,...) hỗ trợ tính toán logic thời gian
import java.time.LocalDate; // Lấy và xử lý ngày tháng năm hiện tại (dùng để gán ngày mượn, hạn trả 7 ngày)
import java.time.temporal.ChronoUnit; // Tính khoảng cách giữa hai mốc thời gian (dùng để tính số ngày quá hạn khi trả sách)
import java.time.temporal.TemporalAdjusters; // Hỗ trợ điều chỉnh mốc thời gian (dùng để tìm ngày thứ Hai đầu tuần nhằm kiểm tra quy định tối đa 3 cuốn/tuần)
import javax.swing.JButton; // Tạo các nút bấm tương tác (Mượn sách, Trả phiếu, Xóa phiếu, Tìm kiếm, Làm mới)
import javax.swing.JLabel; // Tạo các dòng chữ nhãn hướng dẫn (Mã phiếu, Mã độc giả, Mã sách)
import javax.swing.JOptionPane; // Tạo hộp thoại popup thông báo nhỏ (báo thành công, cảnh báo mượn quá 3 cuốn, xác nhận xóa phiếu)
import javax.swing.JPanel; // Tạo các khung chứa nhỏ bên trong giao diện để gom nhóm các ô nhập liệu hoặc nút bấm
import javax.swing.JScrollPane; // Tạo thanh cuộn chứa bảng phiếu mượn giúp cuộn xem danh sách dài
import javax.swing.JTable; // Tạo bảng lưới trực quan hiển thị danh sách chi tiết các phiếu mượn và thông tin quá hạn, tiền phạt
import javax.swing.JTextField; // Tạo các ô nhập văn bản một dòng (cho mã phiếu, mã độc giả, mã sách và ô tìm kiếm)
import javax.swing.ListSelectionModel; // Quản lý chế độ chọn dòng trên bảng (ví dụ chọn đơn lẻ từng dòng phiếu mượn)
import javax.swing.table.DefaultTableModel; // Mô hình quản lý dữ liệu hàng, cột và tên cột gắn liền với JTable của quản lý mượn trả

public class PhieuMuonPanel extends JPanel {

    private JTextField txtMaPhieu = new JTextField();
    private JTextField txtMaDocGia = new JTextField();
    private JTextField txtMaSach = new JTextField();
    private JTextField txtTimKiem = new JTextField(22);

    private JButton btnMuon = new JButton("Mượn sách");
    private JButton btnTra = new JButton("Trả phiếu được chọn");
    private JButton btnXoa = new JButton("Xóa phiếu");
    private JButton btnTim = new JButton("Tìm kiếm");
    private JButton btnLamMoi = new JButton("Làm mới");

    private DefaultTableModel model = new DefaultTableModel(
        new String[]{
            "Mã phiếu",
            "Mã độc giả",
            "Họ tên độc giả",
            "Mã sách",
            "Tên sách",
            "Ngày mượn",
            "Ngày phải trả",
            "Tiền phạt (đồng)",
            "Ngày thực tế trả",
            "Ngày mở hạn mức 3 cuốn mới"
        },
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable table = new JTable(model);

    public PhieuMuonPanel() {
        taoGiaoDien();
        ganSuKien();
        hienThiPhieuMuon();
    }

    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelNhap =
            new JPanel(new GridLayout(3, 2, 5, 5));

        panelNhap.add(new JLabel("Mã phiếu:"));
        panelNhap.add(txtMaPhieu);

        panelNhap.add(new JLabel("Mã độc giả:"));
        panelNhap.add(txtMaDocGia);

        panelNhap.add(new JLabel("Mã sách:"));
        panelNhap.add(txtMaSach);

        JPanel panelNut = new JPanel();
        panelNut.add(btnMuon);
        panelNut.add(btnTra);
        panelNut.add(btnXoa);
        panelNut.add(btnLamMoi);

        JPanel panelTren = new JPanel(new BorderLayout());
        panelTren.add(panelNhap, BorderLayout.CENTER);
        panelTren.add(panelNut, BorderLayout.SOUTH);

        JPanel panelTim = new JPanel();
        panelTim.add(
            new JLabel("Tìm phiếu, độc giả hoặc sách:")
        );
        panelTim.add(txtTimKiem);
        panelTim.add(btnTim);

        add(panelTren, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(panelTim, BorderLayout.SOUTH);

        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
    }

    private void ganSuKien() {
        btnMuon.addActionListener(e -> muonSach());
        btnTra.addActionListener(e -> traSach());
        btnXoa.addActionListener(e -> xoaPhieuMuon());

        btnTim.addActionListener(e ->
            hienThiPhieuMuon(txtTimKiem.getText())
        );

        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            hienThiPhieuMuon();
        });
    }

    private void hienThiPhieuMuon() {
        hienThiPhieuMuon("");
    }

    private void hienThiPhieuMuon(String tuKhoa) {
        String sql = """
            SELECT p.id, p.doc_gia_id, d.ho_ten,
                   p.sach_id, s.ten_sach,
                   p.ngay_muon, p.han_tra,
                   p.tien_phat, p.ngay_tra
            FROM phieu_muon p
            JOIN doc_gia d ON d.id = p.doc_gia_id
            JOIN sach s ON s.id = p.sach_id
            WHERE p.da_xoa = 0
              AND (
                  CAST(p.id AS TEXT) LIKE ?
                  OR CAST(p.doc_gia_id AS TEXT) LIKE ?
                  OR CAST(p.sach_id AS TEXT) LIKE ?
                  OR d.ho_ten LIKE ?
                  OR s.ten_sach LIKE ?
              )
            ORDER BY p.id DESC
            """;

        try (
            Connection connection =
                Database.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            String mauTim =
                "%" + tuKhoa.trim() + "%";

            for (int i = 1; i <= 5; i++) {
                statement.setString(i, mauTim);
            }

            LocalDate thuHaiTuanSau =
                LocalDate.now()
                    .with(
                        TemporalAdjusters.previousOrSame(
                            DayOfWeek.MONDAY
                        )
                    )
                    .plusDays(7);

            try (ResultSet result =
                    statement.executeQuery()) {

                model.setRowCount(0);

                while (result.next()) {
                    String ngayTra =
                        result.getString("ngay_tra");

                    model.addRow(new Object[]{
                        result.getInt("id"),
                        result.getInt("doc_gia_id"),
                        result.getString("ho_ten"),
                        result.getInt("sach_id"),
                        result.getString("ten_sach"),
                        result.getString("ngay_muon"),
                        result.getString("han_tra"),
                        result.getLong("tien_phat"),
                        ngayTra == null
                            ? "Chưa trả"
                            : ngayTra,
                        thuHaiTuanSau.toString()
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi đọc phiếu mượn: "
                    + e.getMessage()
            );
        }
    }

    private int docId(
            JTextField oNhap,
            String tenId) {

        try {
            int id = Integer.parseInt(
                oNhap.getText().trim()
            );

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                tenId
                    + " phải là số nguyên lớn hơn 0."
            );
        }
    }

    private void muonSach() {
        try {
            int maPhieu =
                docId(txtMaPhieu, "Mã phiếu");

            int maDocGia =
                docId(txtMaDocGia, "Mã độc giả");

            int maSach =
                docId(txtMaSach, "Mã sách");

            LocalDate ngayMuon = LocalDate.now();
            LocalDate hanTra =
                ngayMuon.plusDays(7);

            LocalDate thuHai = ngayMuon.with(
                TemporalAdjusters.previousOrSame(
                    DayOfWeek.MONDAY
                )
            );

            LocalDate thuHaiTuanSau =
                thuHai.plusDays(7);

            try (Connection connection =
                    Database.getConnection()) {

                connection.setAutoCommit(false);

                try {
                    // 1. Kiểm tra độc giả tồn tại.
                    String sqlDocGia = """
                        SELECT COUNT(*)
                        FROM doc_gia
                        WHERE id = ?
                        """;

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                sqlDocGia
                            )) {

                        statement.setInt(
                            1, maDocGia
                        );

                        try (ResultSet result =
                                statement.executeQuery()) {

                            result.next();

                            if (result.getInt(1) == 0) {
                                throw new IllegalArgumentException(
                                    "Không tìm thấy độc giả mã "
                                        + maDocGia
                                );
                            }
                        }
                    }

                    // 2. Kiểm tra sách và tổng số bản.
                    int tongSoBan;

                    String sqlSach = """
                        SELECT so_luong
                        FROM sach
                        WHERE id = ?
                        """;

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                sqlSach
                            )) {

                        statement.setInt(1, maSach);

                        try (ResultSet result =
                                statement.executeQuery()) {

                            if (!result.next()) {
                                throw new IllegalArgumentException(
                                    "Không tìm thấy sách mã "
                                        + maSach
                                );
                            }

                            tongSoBan =
                                result.getInt(
                                    "so_luong"
                                );
                        }
                    }

                    // 3. Đếm mọi lượt mượn trong tuần:
                    // kể cả phiếu đã trả và đã xóa mềm.
                    int soCuonTrongTuan;

                    String sqlTrongTuan = """
                        SELECT COUNT(*)
                        FROM phieu_muon
                        WHERE doc_gia_id = ?
                          AND ngay_muon >= ?
                          AND ngay_muon < ?
                        """;

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                sqlTrongTuan
                            )) {

                        statement.setInt(
                            1, maDocGia
                        );
                        statement.setString(
                            2, thuHai.toString()
                        );
                        statement.setString(
                            3,
                            thuHaiTuanSau.toString()
                        );

                        try (ResultSet result =
                                statement.executeQuery()) {

                            result.next();

                            soCuonTrongTuan =
                                result.getInt(1);
                        }
                    }

                    if (soCuonTrongTuan >= 3) {
                        throw new IllegalArgumentException(
                            "Độc giả đã mượn đủ 3 cuốn "
                                + "trong tuần lịch này."
                        );
                    }

                    // 4. Kiểm tra sách còn bản để mượn.
                    int soBanDangMuon;

                    String sqlDangMuon = """
                        SELECT COUNT(*)
                        FROM phieu_muon
                        WHERE sach_id = ?
                          AND ngay_tra IS NULL
                        """;

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                sqlDangMuon
                            )) {

                        statement.setInt(1, maSach);

                        try (ResultSet result =
                                statement.executeQuery()) {

                            result.next();

                            soBanDangMuon =
                                result.getInt(1);
                        }
                    }

                    if (soBanDangMuon >= tongSoBan) {
                        throw new IllegalArgumentException(
                            "Sách hiện không còn bản "
                                + "để mượn."
                        );
                    }

                    // 5. Lưu phiếu mượn.
                    // tien_phat mặc định là 0.
                    String sqlThem = """
                        INSERT INTO phieu_muon
                            (id, doc_gia_id, sach_id,
                             ngay_muon, han_tra)
                        VALUES (?, ?, ?, ?, ?)
                        """;

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                sqlThem
                            )) {

                        statement.setInt(
                            1, maPhieu
                        );
                        statement.setInt(
                            2, maDocGia
                        );
                        statement.setInt(
                            3, maSach
                        );
                        statement.setString(
                            4, ngayMuon.toString()
                        );
                        statement.setString(
                            5, hanTra.toString()
                        );

                        statement.executeUpdate();
                    }

                    connection.commit();

                } catch (
                    SQLException | RuntimeException e
                ) {
                    connection.rollback();
                    throw e;
                }
            }

            hienThiPhieuMuon(
                txtTimKiem.getText()
            );

            txtMaPhieu.setText("");
            txtMaDocGia.setText("");
            txtMaSach.setText("");

            JOptionPane.showMessageDialog(
                this,
                "Mượn sách thành công. "
                    + "Ngày phải trả: "
                    + hanTra
            );

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage()
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Không thể lập phiếu. "
                    + "Kiểm tra mã phiếu đã tồn tại chưa.\n"
                    + e.getMessage()
            );
        }
    }

    private void traSach() {
        int dong = table.getSelectedRow();

        if (dong == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn phiếu cần trả trên bảng."
            );
            return;
        }

        int maPhieu = Integer.parseInt(
            table.getValueAt(dong, 0).toString()
        );

        LocalDate ngayTra = LocalDate.now();

        try (Connection connection =
                Database.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Đọc hạn trả của chính phiếu được chọn.
                String sqlDoc = """
                    SELECT han_tra
                    FROM phieu_muon
                    WHERE id = ?
                      AND ngay_tra IS NULL
                      AND da_xoa = 0
                    """;

                LocalDate hanTra;

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            sqlDoc
                        )) {

                    statement.setInt(
                        1, maPhieu
                    );

                    try (ResultSet result =
                            statement.executeQuery()) {

                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                "Phiếu không còn tồn tại "
                                    + "hoặc đã được trả."
                            );
                        }

                        hanTra = LocalDate.parse(
                            result.getString(
                                "han_tra"
                            )
                        );
                    }
                }

                long soNgayQuaHan = Math.max(
                    0,
                    ChronoUnit.DAYS.between(
                        hanTra,
                        ngayTra
                    )
                );

                // 1.000 đồng mỗi ngày quá hạn.
                long tienPhat =
                    soNgayQuaHan * 1_000L;

                // Lưu ngày trả và tiền phạt cùng lúc.
                String sqlTra = """
                    UPDATE phieu_muon
                    SET ngay_tra = ?,
                        tien_phat = ?
                    WHERE id = ?
                      AND ngay_tra IS NULL
                      AND da_xoa = 0
                    """;

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            sqlTra
                        )) {

                    statement.setString(
                        1, ngayTra.toString()
                    );
                    statement.setLong(
                        2, tienPhat
                    );
                    statement.setInt(
                        3, maPhieu
                    );

                    int soDongDaSua =
                        statement.executeUpdate();

                    if (soDongDaSua == 0) {
                        throw new IllegalArgumentException(
                            "Phiếu không còn tồn tại "
                                + "hoặc đã được trả."
                        );
                    }
                }

                connection.commit();

                hienThiPhieuMuon(
                    txtTimKiem.getText()
                );

                JOptionPane.showMessageDialog(
                    this,
                    "Trả sách thành công!\n"
                        + "Số ngày quá hạn: "
                        + soNgayQuaHan
                        + "\nTiền phạt: "
                        + tienPhat
                        + " đồng"
                );

            } catch (
                SQLException | RuntimeException e
            ) {
                connection.rollback();
                throw e;
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage()
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi khi trả sách: "
                    + e.getMessage()
            );
        }
    }

    private void xoaPhieuMuon() {
        int dong = table.getSelectedRow();

        if (dong == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn phiếu cần xóa trên bảng."
            );
            return;
        }

        int maPhieu = Integer.parseInt(
            table.getValueAt(dong, 0).toString()
        );

        int luaChon =
            JOptionPane.showConfirmDialog(
                this,
                "Xóa phiếu mã "
                    + maPhieu
                    + " khỏi danh sách?\n"
                    + "Phiếu đã trả và hết tiền phạt "
                    + "mới được xóa.\n"
                    + "Lịch sử mượn vẫn được giữ.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
            );

        if (luaChon
                != JOptionPane.YES_OPTION) {
            return;
        }

        // Xóa mềm: vẫn giữ bản ghi để tính
        // giới hạn 3 cuốn trong tuần.
        String sql = """
            UPDATE phieu_muon
            SET da_xoa = 1
            WHERE id = ?
              AND da_xoa = 0
              AND ngay_tra IS NOT NULL
              AND TRIM(ngay_tra) <> ''
              AND tien_phat = 0
            """;

        try (
            Connection connection =
                Database.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setInt(1, maPhieu);

            int soDongDaXoa =
                statement.executeUpdate();

            if (soDongDaXoa == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa phiếu.\n"
                        + "Phiếu phải được trả "
                        + "và đã thanh toán hết "
                        + "tiền phạt."
                );
                return;
            }

            hienThiPhieuMuon(
                txtTimKiem.getText()
            );

            JOptionPane.showMessageDialog(
                this,
                "Đã xóa phiếu mã "
                    + maPhieu
                    + " khỏi danh sách.\n"
                    + "Lịch sử mượn vẫn được giữ."
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi khi xóa phiếu: "
                    + e.getMessage()
            );
        }
    }
}