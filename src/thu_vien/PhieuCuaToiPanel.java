package thu_vien;

import java.awt.BorderLayout; // Giúp chia không gian cửa sổ thành 5 vùng: NORTH, SOUTH, EAST, WEST, CENTER
import java.sql.Connection; // Quản lý phiên kết nối vật lý giữa ứng dụng Java và cơ sở dữ liệu SQLite
import java.sql.PreparedStatement; // Thực thi câu lệnh SQL có tham số (dùng dấu ?), giúp chống SQL Injection và tăng hiệu suất
import java.sql.ResultSet; // Bảng tạm lưu trữ tập hợp dữ liệu kết quả trả về sau khi thực hiện truy vấn SELECT
import java.sql.SQLException; // Lớp ngoại lệ chuyên bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu
import javax.swing.JButton; // Tạo các nút bấm tương tác (như nút "Thanh toán tiền phạt", "Làm mới")
import javax.swing.JOptionPane; // Tạo các hộp thoại popup thông báo nhỏ (báo thanh toán thành công hoặc nhắc nhở chọn phiếu)
import javax.swing.JPanel; // Tạo khung chứa nhỏ bên trong giao diện để gom nhóm các thành phần (như cụm nút bấm ở dưới)
import javax.swing.JScrollPane; // Tạo thanh cuộn chứa bảng dữ liệu giúp cuộn xem danh sách phiếu mượn dài
import javax.swing.JTable; // Tạo bảng lưới trực quan hiển thị danh sách phiếu mượn và thông tin tiền phạt của cá nhân
import javax.swing.table.DefaultTableModel; // Mô hình quản lý dữ liệu (hàng, cột, tên cột) gắn liền với JTable của phiếu mượn

public class PhieuCuaToiPanel extends JPanel {

    private final int maDocGia;

    private final DefaultTableModel model =
        new DefaultTableModel(
            new String[]{
                "Mã phiếu",
                "Tên sách",
                "Ngày mượn",
                "Hạn trả",
                "Ngày trả",
                "Tiền phạt (đồng)"
            },
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

    private final JTable table = new JTable(model);

    public PhieuCuaToiPanel(int maDocGia) {
        this.maDocGia = maDocGia;

        setLayout(new BorderLayout(8, 8));

        JButton btnThanhToan =
            new JButton("Thanh toán tiền phạt");

        JButton btnLamMoi =
            new JButton("Làm mới");

        JPanel panelNut = new JPanel();
        panelNut.add(btnThanhToan);
        panelNut.add(btnLamMoi);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(panelNut, BorderLayout.SOUTH);

        btnThanhToan.addActionListener(e ->
            thanhToanTienPhat()
        );

        btnLamMoi.addActionListener(e ->
            lamMoiDanhSach()
        );

        lamMoiDanhSach();
    }

    public void lamMoiDanhSach() {
        String sql = """
            SELECT p.id, s.ten_sach,
                   p.ngay_muon, p.han_tra,
                   p.ngay_tra, p.tien_phat
            FROM phieu_muon p
            JOIN sach s ON s.id = p.sach_id
            WHERE p.doc_gia_id = ?
              AND p.da_xoa = 0
            ORDER BY p.id DESC
            """;

        try (
            Connection connection = Database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setInt(1, maDocGia);

            try (ResultSet result =
                    statement.executeQuery()) {

                model.setRowCount(0);

                while (result.next()) {
                    String ngayTra =
                        result.getString("ngay_tra");

                    model.addRow(new Object[]{
                        result.getInt("id"),
                        result.getString("ten_sach"),
                        result.getString("ngay_muon"),
                        result.getString("han_tra"),
                        ngayTra == null
                            ? "Chưa trả"
                            : ngayTra,
                        result.getLong("tien_phat")
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

    private void thanhToanTienPhat() {
        int dong = table.getSelectedRow();

        if (dong == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn phiếu có tiền phạt."
            );
            return;
        }

        int maPhieu = Integer.parseInt(
            table.getValueAt(dong, 0).toString()
        );

        String sql = """
            UPDATE phieu_muon
            SET tien_phat = 0
            WHERE id = ?
              AND doc_gia_id = ?
              AND da_xoa = 0
              AND ngay_tra IS NOT NULL
              AND tien_phat > 0
            """;

        try (
            Connection connection = Database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setInt(1, maPhieu);
            statement.setInt(2, maDocGia);

            int soDong = statement.executeUpdate();

            if (soDong == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Phiếu không thuộc tài khoản này, "
                        + "chưa trả hoặc không còn tiền phạt."
                );
                lamMoiDanhSach();
                return;
            }

            lamMoiDanhSach();

            JOptionPane.showMessageDialog(
                this,
                "Đã thanh toán tiền phạt."
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi thanh toán: "
                    + e.getMessage()
            );
        }
    }
}