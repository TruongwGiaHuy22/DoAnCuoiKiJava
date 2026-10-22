package thu_vien;

import java.awt.BorderLayout; // Giúp chia không gian cửa sổ thành 5 vùng riêng biệt: NORTH, SOUTH, EAST, WEST, CENTER
import java.awt.GridLayout; // Sắp xếp các thành phần giao diện theo dạng lưới chia đều hàng và cột (dùng cho form nhập mã và họ tên độc giả)
import java.sql.Connection; // Quản lý phiên kết nối vật lý giữa ứng dụng Java và cơ sở dữ liệu SQLite
import java.sql.PreparedStatement; // Thực thi câu lệnh SQL có tham số (dùng dấu ?), giúp chống SQL Injection và tăng hiệu suất
import java.sql.ResultSet; // Bảng tạm lưu trữ tập hợp dữ liệu kết quả trả về sau khi thực hiện câu lệnh truy vấn SELECT
import java.sql.SQLException; // Lớp ngoại lệ chuyên bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu
import javax.swing.JButton; // Tạo các nút bấm tương tác (Thêm, Sửa, Xóa, Làm mới, Tìm kiếm độc giả)
import javax.swing.JLabel; // Tạo các dòng chữ nhãn hướng dẫn (Mã độc giả, Họ tên, Tìm theo họ tên)
import javax.swing.JOptionPane; // Tạo hộp thoại popup thông báo nhỏ (báo thành công, cảnh báo lỗi, xác nhận xóa độc giả)
import javax.swing.JPanel; // Tạo các khung chứa nhỏ bên trong giao diện để gom nhóm form nhập liệu, nút bấm hoặc khung tìm kiếm
import javax.swing.JScrollPane; // Tạo thanh cuộn chứa bảng danh sách độc giả giúp cuộn xem danh sách dài dễ dàng
import javax.swing.JTable; // Tạo bảng lưới trực quan hiển thị danh sách độc giả gồm mã độc giả và họ tên
import javax.swing.JTextField; // Tạo các ô nhập văn bản một dòng (cho ô mã độc giả, họ tên và ô nhập từ khóa tìm kiếm)
import javax.swing.ListSelectionModel; // Quản lý chế độ chọn dòng trên bảng (ví dụ: chọn đơn lẻ từng dòng độc giả SINGLE_SELECTION)
import javax.swing.table.DefaultTableModel; // Mô hình quản lý dữ liệu hàng, cột và tên cột gắn liền với JTable của quản lý độc giả

public class SachPanel extends JPanel {

    // true: thủ thư; false: độc giả.
    private final boolean duocChinhSua;

    private JTextField txtId = new JTextField();
    private JTextField txtTenSach = new JTextField();
    private JTextField txtTacGia = new JTextField();
    private JTextField txtSoLuong = new JTextField();
    private JTextField txtTimKiem = new JTextField(20);

    private JButton btnThem = new JButton("Thêm");
    private JButton btnSua = new JButton("Sửa");
    private JButton btnXoa = new JButton("Xóa");
    private JButton btnLamMoi = new JButton("Làm mới");
    private JButton btnTim = new JButton("Tìm kiếm");

    private DefaultTableModel model = new DefaultTableModel(
        new String[]{
            "Mã sách", "Tên sách", "Tác giả",
            "Tổng số bản", "Còn có thể mượn"
        },
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private JTable table = new JTable(model);

    public SachPanel(boolean duocChinhSua) {
        this.duocChinhSua = duocChinhSua;

        taoGiaoDien();
        ganSuKien();
        apDungQuyen();
        hienThiSach();
    }

    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelNhap =
            new JPanel(new GridLayout(4, 2, 5, 5));

        panelNhap.add(new JLabel("Mã sách:"));
        panelNhap.add(txtId);

        panelNhap.add(new JLabel("Tên sách:"));
        panelNhap.add(txtTenSach);

        panelNhap.add(new JLabel("Tác giả:"));
        panelNhap.add(txtTacGia);

        panelNhap.add(new JLabel("Tổng số bản:"));
        panelNhap.add(txtSoLuong);

        JPanel panelNut = new JPanel();
        panelNut.add(btnThem);
        panelNut.add(btnSua);
        panelNut.add(btnXoa);
        panelNut.add(btnLamMoi);

        JPanel panelTren = new JPanel(new BorderLayout());
        panelTren.add(panelNhap, BorderLayout.CENTER);
        panelTren.add(panelNut, BorderLayout.SOUTH);

        JPanel panelTim = new JPanel();
        panelTim.add(
            new JLabel("Tìm theo mã, tên hoặc tác giả:")
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
        btnThem.addActionListener(e -> themSach());
        btnSua.addActionListener(e -> suaSach());
        btnXoa.addActionListener(e -> xoaSach());

        btnTim.addActionListener(e ->
            hienThiSach(txtTimKiem.getText())
        );

        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            xoaForm();
            hienThiSach();
        });

        table.getSelectionModel().addListSelectionListener(
            e -> {
                if (!e.getValueIsAdjusting()) {
                    chonSach();
                }
            }
        );
    }

    private void apDungQuyen() {
        // Độc giả không thấy các nút thay đổi dữ liệu.
        btnThem.setVisible(duocChinhSua);
        btnSua.setVisible(duocChinhSua);
        btnXoa.setVisible(duocChinhSua);

        // Độc giả vẫn thấy thông tin sách khi chọn trên bảng,
        // nhưng không thể sửa các ô thông tin.
        txtId.setEditable(duocChinhSua);
        txtTenSach.setEditable(duocChinhSua);
        txtTacGia.setEditable(duocChinhSua);
        txtSoLuong.setEditable(duocChinhSua);
    }

    private void hienThiSach() {
        hienThiSach("");
    }

    private void hienThiSach(String tuKhoa) {
        String sql = """
            SELECT s.id, s.ten_sach, s.tac_gia,
                   s.so_luong,
                   s.so_luong - (
                       SELECT COUNT(*)
                       FROM phieu_muon p
                       WHERE p.sach_id = s.id
                         AND p.ngay_tra IS NULL
                   ) AS con_lai
            FROM sach s
            WHERE CAST(s.id AS TEXT) LIKE ?
               OR s.ten_sach LIKE ?
               OR s.tac_gia LIKE ?
            ORDER BY s.id
            """;

        try (
            Connection connection =
                Database.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            String mauTim =
                "%" + tuKhoa.trim() + "%";

            statement.setString(1, mauTim);
            statement.setString(2, mauTim);
            statement.setString(3, mauTim);

            try (ResultSet result =
                    statement.executeQuery()) {

                model.setRowCount(0);

                while (result.next()) {
                    model.addRow(new Object[]{
                        result.getInt("id"),
                        result.getString("ten_sach"),
                        result.getString("tac_gia"),
                        result.getInt("so_luong"),
                        result.getInt("con_lai")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi đọc danh sách sách: "
                    + e.getMessage()
            );
        }
    }

    // LibraryApp gọi khi mở lại tab Sách.
    public void lamMoiDanhSach() {
        xoaForm();
        hienThiSach(txtTimKiem.getText());
    }

    private int docIdSach() {
        try {
            int id = Integer.parseInt(
                txtId.getText().trim()
            );

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Mã sách phải là số nguyên lớn hơn 0."
            );
        }
    }

    private int docSoLuong() {
        if (txtTenSach.getText().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Bạn chưa nhập tên sách."
            );
        }

        if (txtTacGia.getText().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Bạn chưa nhập tác giả."
            );
        }

        try {
            int soLuong = Integer.parseInt(
                txtSoLuong.getText().trim()
            );

            if (soLuong < 0) {
                throw new NumberFormatException();
            }

            return soLuong;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Tổng số bản phải là số nguyên từ 0 trở lên."
            );
        }
    }

    private void xoaForm() {
        table.clearSelection();

        txtId.setText("");
        txtTenSach.setText("");
        txtTacGia.setText("");
        txtSoLuong.setText("");

        // Chỉ thủ thư được nhập mã sách mới.
        txtId.setEditable(duocChinhSua);
    }

    private void themSach() {
        if (!duocChinhSua) {
            thongBaoKhongCoQuyen();
            return;
        }

        try {
            int id = docIdSach();
            int soLuong = docSoLuong();

            String sql = """
                INSERT INTO sach
                    (id, ten_sach, tac_gia, so_luong)
                VALUES (?, ?, ?, ?)
                """;

            try (
                Connection connection =
                    Database.getConnection();

                PreparedStatement statement =
                    connection.prepareStatement(sql)
            ) {
                statement.setInt(1, id);
                statement.setString(
                    2,
                    txtTenSach.getText().trim()
                );
                statement.setString(
                    3,
                    txtTacGia.getText().trim()
                );
                statement.setInt(4, soLuong);

                statement.executeUpdate();
            }

            xoaForm();
            hienThiSach(txtTimKiem.getText());

            JOptionPane.showMessageDialog(
                this,
                "Thêm sách thành công!"
            );

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage()
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Không thể thêm sách. Kiểm tra mã "
                    + "đã tồn tại chưa.\n"
                    + e.getMessage()
            );
        }
    }

    private void chonSach() {
        int dong = table.getSelectedRow();

        if (dong == -1) {
            return;
        }

        txtId.setText(
            table.getValueAt(dong, 0).toString()
        );
        txtTenSach.setText(
            table.getValueAt(dong, 1).toString()
        );
        txtTacGia.setText(
            table.getValueAt(dong, 2).toString()
        );
        txtSoLuong.setText(
            table.getValueAt(dong, 3).toString()
        );

        // Mã sách đang có không được sửa.
        txtId.setEditable(false);
    }

    private void suaSach() {
        if (!duocChinhSua) {
            thongBaoKhongCoQuyen();
            return;
        }

        int dong = table.getSelectedRow();

        if (dong == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn sách cần sửa trên bảng."
            );
            return;
        }

        try {
            int id = Integer.parseInt(
                table.getValueAt(dong, 0).toString()
            );
            int soLuongMoi = docSoLuong();

            try (Connection connection =
                    Database.getConnection()) {

                String sqlDem = """
                    SELECT COUNT(*)
                    FROM phieu_muon
                    WHERE sach_id = ?
                      AND ngay_tra IS NULL
                    """;

                int soBanDangMuon;

                try (PreparedStatement statement =
                        connection.prepareStatement(sqlDem)) {

                    statement.setInt(1, id);

                    try (ResultSet result =
                            statement.executeQuery()) {

                        result.next();
                        soBanDangMuon = result.getInt(1);
                    }
                }

                if (soLuongMoi < soBanDangMuon) {
                    throw new IllegalArgumentException(
                        "Tổng số bản không thể nhỏ hơn "
                            + soBanDangMuon
                            + " bản đang được mượn."
                    );
                }

                String sqlSua = """
                    UPDATE sach
                    SET ten_sach = ?,
                        tac_gia = ?,
                        so_luong = ?
                    WHERE id = ?
                    """;

                try (PreparedStatement statement =
                        connection.prepareStatement(
                            sqlSua
                        )) {

                    statement.setString(
                        1,
                        txtTenSach.getText().trim()
                    );
                    statement.setString(
                        2,
                        txtTacGia.getText().trim()
                    );
                    statement.setInt(3, soLuongMoi);
                    statement.setInt(4, id);

                    int soDong =
                        statement.executeUpdate();

                    if (soDong == 0) {
                        throw new SQLException(
                            "Sách không còn tồn tại. "
                                + "Hãy bấm Làm mới."
                        );
                    }
                }
            }

            xoaForm();
            hienThiSach(txtTimKiem.getText());

            JOptionPane.showMessageDialog(
                this,
                "Sửa sách thành công!"
            );

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage()
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi khi sửa sách: "
                    + e.getMessage()
            );
        }
    }

    private void xoaSach() {
        if (!duocChinhSua) {
            thongBaoKhongCoQuyen();
            return;
        }

        int dong = table.getSelectedRow();

        if (dong == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn sách cần xóa trên bảng."
            );
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xóa sách này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        int id = Integer.parseInt(
            table.getValueAt(dong, 0).toString()
        );

        String sql =
            "DELETE FROM sach WHERE id = ?";

        try (
            Connection connection =
                Database.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            int soDong =
                statement.executeUpdate();

            if (soDong == 0) {
                throw new SQLException(
                    "Sách không còn tồn tại. "
                        + "Hãy bấm Làm mới."
                );
            }

            xoaForm();
            hienThiSach(txtTimKiem.getText());

            JOptionPane.showMessageDialog(
                this,
                "Xóa sách thành công!"
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Không thể xóa sách: "
                    + e.getMessage()
            );
        }
    }

    private void thongBaoKhongCoQuyen() {
        JOptionPane.showMessageDialog(
            this,
            "Tài khoản độc giả chỉ được xem sách."
        );
    }
}