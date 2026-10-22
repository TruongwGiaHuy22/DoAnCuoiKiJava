package thu_vien;

import java.sql.SQLException;

public class TestDatabase {

    public static void main(String[] args) {
        try {
            Database.initialize();

            System.out.println("Kết nối SQLite thành công!");
            System.out.println(
                "Tệp dữ liệu: "
                + new java.io.File("library.db").getAbsolutePath()
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}