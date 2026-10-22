package thu_vien;

import java.sql.Connection; // Quản lý phiên kết nối vật lý giữa ứng dụng Java và file cơ sở dữ liệu SQLite (library.db)
import java.sql.DriverManager; // Trình điều khiển quản lý việc tạo ra các kết nối đến cơ sở dữ liệu thông qua chuỗi URL
import java.sql.ResultSet; // Bảng tạm lưu trữ tập hợp dữ liệu kết quả trả về sau khi thực hiện câu lệnh truy vấn SELECT
import java.sql.SQLException; // Lớp ngoại lệ chuyên bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu (mất kết nối, sai câu lệnh, trùng khóa)
import java.sql.Statement; // Dùng để thực thi các câu lệnh SQL tĩnh (như tạo bảng CREATE TABLE)
import java.sql.PreparedStatement; // Dùng để thực thi các câu lệnh SQL có tham số (dùng dấu ?), giúp chống SQL Injection và tăng hiệu suất

public class Database {

    private static final String URL = "jdbc:sqlite:library.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
//Kết nối tới tệp cơ sở dữ liệu library.db nằm trong thư mục dự án
        try {
            // Bật kiểm tra khóa ngoại cho kết nối này.
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }

            return connection;
//không cho phép xóa độc giả hoặc xóa sách nếu vẫn còn phiếu mượn đang liên kết tới họ).
        } catch (SQLException e) {
            // Nếu thiết lập kết nối thất bại thì đóng kết nối.
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }

            throw e;
        }
    }

    public static void initialize() throws SQLException {
        String sqlSach = """
            CREATE TABLE IF NOT EXISTS sach (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ten_sach TEXT NOT NULL,
                tac_gia TEXT NOT NULL,
                so_luong INTEGER NOT NULL CHECK (so_luong >= 0)
            )
            """;
// tạo bảng sách
        String sqlDocGia = """
            CREATE TABLE IF NOT EXISTS doc_gia (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ho_ten TEXT NOT NULL
            )
            """;
// tạo bảng độc giả
        String sqlPhieuMuon = """
            CREATE TABLE IF NOT EXISTS phieu_muon (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                doc_gia_id INTEGER NOT NULL,
                sach_id INTEGER NOT NULL,
                ngay_muon TEXT NOT NULL,
                han_tra TEXT NOT NULL,
                ngay_tra TEXT,
                da_xoa INTEGER NOT NULL DEFAULT 0
                    CHECK (da_xoa IN (0, 1)),
        		tien_phat INTEGER NOT NULL DEFAULT 0
        			CHECK (tien_phat >= 0),
                FOREIGN KEY (doc_gia_id) REFERENCES doc_gia(id),
                FOREIGN KEY (sach_id) REFERENCES sach(id)
            )
            """;
        String sqlTaiKhoan = """
        	    CREATE TABLE IF NOT EXISTS tai_khoan (
        	        ten_dang_nhap TEXT PRIMARY KEY,
        	        mat_khau_hash TEXT NOT NULL,
        	        vai_tro TEXT NOT NULL
        	            CHECK (vai_tro IN ('THU_THU', 'DOC_GIA')),
        	        doc_gia_id INTEGER,
        	        FOREIGN KEY (doc_gia_id) REFERENCES doc_gia(id),

        	        CHECK (
        	            (vai_tro = 'THU_THU' AND doc_gia_id IS NULL)
        	            OR
        	            (vai_tro = 'DOC_GIA' AND doc_gia_id IS NOT NULL)
        	        )
        	    )
        	    """;
// tạo bảng tài khoản 
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlSach);
                statement.executeUpdate(sqlDocGia);
                statement.executeUpdate(sqlPhieuMuon);
                statement.executeUpdate(sqlTaiKhoan);
            }
//Khi chạy ứng dụng, nó sẽ thực thi các câu lệnh SQL để tạo ra 4 bảng chính
            // Bổ sung cột cho library.db đã tồn tại từ trước.
            boSungCotDaXoa(connection);
            boSungCotTienPhat(connection);
            taoTaiKhoanDemo(connection);
        }
    }

    private static void boSungCotTienPhat(Connection connection)
            throws SQLException {

        boolean daCoCot = false;

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(
                "PRAGMA table_info(phieu_muon)"
            )
        ) {
            while (result.next()) {
                if ("tien_phat".equals(result.getString("name"))) {
                    daCoCot = true;
                    break;
                }
            }
        }

        if (!daCoCot) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                    ALTER TABLE phieu_muon
                    ADD COLUMN tien_phat INTEGER NOT NULL DEFAULT 0
                        CHECK (tien_phat >= 0)
                    """);
            }
        }
    }
    
    private static void boSungCotDaXoa(Connection connection)
            throws SQLException {

        boolean daCoCot = false;

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(
                "PRAGMA table_info(phieu_muon)"
            )
        ) {
            while (result.next()) {
                if ("da_xoa".equalsIgnoreCase(
                        result.getString("name"))) {
                    daCoCot = true;
                    break;
                }
            }
        }

        // Chạy nhiều lần cũng không thêm trùng cột.
        if (!daCoCot) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("""
                    ALTER TABLE phieu_muon
                    ADD COLUMN da_xoa INTEGER NOT NULL DEFAULT 0
                        CHECK (da_xoa IN (0, 1))
                    """);
            }
        }
    }

    private static void taoTaiKhoanDemo(Connection connection)
            throws SQLException {

        // Tạo độc giả demo nếu tài khoản docgia_demo chưa tồn tại.
        int maDocGiaDemo;

        String sqlTim = """
            SELECT doc_gia_id
            FROM tai_khoan
            WHERE ten_dang_nhap = 'docgia_demo'
            """;

        try (
            PreparedStatement statement =
                connection.prepareStatement(sqlTim);
            ResultSet result = statement.executeQuery()
        ) {
            if (result.next()) {
                maDocGiaDemo = result.getInt("doc_gia_id");

            } else {
                String sqlThemDocGia = """
                    INSERT INTO doc_gia (ho_ten)
                    VALUES ('Độc giả demo')
                    """;

                try (
                    PreparedStatement statementThem =
                        connection.prepareStatement(
                            sqlThemDocGia,
                            Statement.RETURN_GENERATED_KEYS
                        )
                ) {
                    statementThem.executeUpdate();

                    try (ResultSet keys =
                            statementThem.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                "Không lấy được mã độc giả demo."
                            );
                        }

                        maDocGiaDemo = keys.getInt(1);
                    }
                }

                String sqlThemTaiKhoan = """
                    INSERT INTO tai_khoan
                        (ten_dang_nhap, mat_khau_hash,
                         vai_tro, doc_gia_id)
                    VALUES (?, ?, 'DOC_GIA', ?)
                    """;

                try (PreparedStatement statementThem =
                        connection.prepareStatement(
                            sqlThemTaiKhoan
                        )) {

                    statementThem.setString(1, "docgia_demo");
                    statementThem.setString(
                        2,
                        BaoMat.bamMatKhau("Docgia@123")
                    );
                    statementThem.setInt(3, maDocGiaDemo);
                    statementThem.executeUpdate();
                }
            }
        }

        String sqlThemThuThu = """
            INSERT OR IGNORE INTO tai_khoan
                (ten_dang_nhap, mat_khau_hash,
                 vai_tro, doc_gia_id)
            VALUES (?, ?, 'THU_THU', NULL)
            """;

        try (PreparedStatement statement =
                connection.prepareStatement(sqlThemThuThu)) {

            statement.setString(1, "thuthu_demo");
            statement.setString(
                2,
                BaoMat.bamMatKhau("Thuthu@123")
            );
            statement.executeUpdate();
        }
    }
    
}