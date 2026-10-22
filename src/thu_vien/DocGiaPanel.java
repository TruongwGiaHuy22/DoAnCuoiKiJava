package thu_vien;

import java.awt.BorderLayout; // Giúp chia không gian cửa sổ thành 5 vùng riêng biệt: NORTH, SOUTH, EAST, WEST, CENTER
import java.awt.GridLayout; // Sắp xếp các thành phần giao diện theo dạng lưới chia đều hàng và cột (dùng cho form nhập mã, tên sách, tác giả, số lượng)
import java.sql.Connection; // Quản lý phiên kết nối vật lý giữa ứng dụng Java và cơ sở dữ liệu SQLite
import java.sql.PreparedStatement; // Thực thi câu lệnh SQL có tham số (dùng dấu ?), giúp chống SQL Injection và tăng hiệu suất
import java.sql.ResultSet; // Bảng tạm lưu trữ tập hợp dữ liệu kết quả trả về sau khi thực hiện câu lệnh truy vấn SELECT
import java.sql.SQLException; // Lớp ngoại lệ chuyên bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu
import javax.swing.JButton; // Tạo các nút bấm tương tác (Thêm, Sửa, Xóa, Làm mới, Tìm kiếm sách)
import javax.swing.JLabel; // Tạo các dòng chữ nhãn hướng dẫn (Mã sách, Tên sách, Tác giả, Tổng số bản)
import javax.swing.JOptionPane; // Tạo hộp thoại popup thông báo nhỏ (báo thành công, cảnh báo lỗi, xác nhận xóa sách)
import javax.swing.JPanel; // Tạo các khung chứa nhỏ bên trong giao diện để gom nhóm form nhập liệu, nút bấm hoặc khung tìm kiếm
import javax.swing.JScrollPane; // Tạo thanh cuộn chứa bảng danh sách sách giúp cuộn xem danh sách dài dễ dàng
import javax.swing.JTable; // Tạo bảng lưới trực quan hiển thị danh sách sách gồm mã, tên, tác giả, tổng số bản và số lượng còn có thể mượn
import javax.swing.JTextField; // Tạo các ô nhập văn bản một dòng (cho mã sách, tên sách, tác giả, số lượng và ô tìm kiếm)
import javax.swing.ListSelectionModel; // Quản lý chế độ chọn dòng trên bảng (ví dụ: chọn đơn lẻ từng dòng sách SINGLE_SELECTION)
import javax.swing.table.DefaultTableModel; // Mô hình quản lý dữ liệu hàng, cột và tên cột gắn liền với JTable của quản lý sách

public class DocGiaPanel extends JPanel {

    // Các ô nhập
    private JTextField txtMaDocGia = new JTextField();
    private JTextField txtHoTen = new JTextField();
    private JTextField txtTimDocGia = new JTextField(20);
    // chưa 20 ký tự
    // Các nút
    private JButton btnThemDocGia = new JButton("Thêm");
    private JButton btnSuaDocGia = new JButton("Sửa");
    private JButton btnXoaDocGia = new JButton("Xóa");
    private JButton btnLamMoiDocGia = new JButton("Làm mới");
    private JButton btnTimDocGia = new JButton("Tìm kiếm");

    // Dữ liệu hiển thị trên bảng
    private DefaultTableModel modelDocGia = new DefaultTableModel(
        new String[]{"Mã độc giả", "Họ tên"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
// ko cho độc giả vô sửa chi cho xem
    private JTable tableDocGia = new JTable(modelDocGia);

    // Constructor: chạy khi LibraryApp tạo một DocGiaPanel
    public DocGiaPanel() {
        taoGiaoDien();
        ganSuKien();
        hienThiDocGia();
    }
//Tạo một bảng độc giả rỗng, chỉ đọc; rồi khi mở màn hình thì tự dựng giao diện, gắn sự kiện và nạp danh sách độc giả vào bảng.
    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        // Khu vực nhập mã và họ tên
        JPanel panelNhapDocGia = new JPanel(new GridLayout(2, 2, 5, 5));

        txtMaDocGia.setEditable(true);

        panelNhapDocGia.add(new JLabel("Mã độc giả:"));
        panelNhapDocGia.add(txtMaDocGia);

        panelNhapDocGia.add(new JLabel("Họ tên:"));
        panelNhapDocGia.add(txtHoTen);

        // Khu vực các nút
        JPanel panelNutDocGia = new JPanel();
        panelNutDocGia.add(btnThemDocGia);
        panelNutDocGia.add(btnSuaDocGia);
        panelNutDocGia.add(btnXoaDocGia);
        panelNutDocGia.add(btnLamMoiDocGia);

        JPanel panelTrenDocGia = new JPanel(new BorderLayout());
        panelTrenDocGia.add(panelNhapDocGia, BorderLayout.CENTER);
        panelTrenDocGia.add(panelNutDocGia, BorderLayout.SOUTH);

        // Khu vực tìm kiếm
        JPanel panelTimKiemDocGia = new JPanel();
        panelTimKiemDocGia.add(new JLabel("Tìm theo họ tên:"));
        panelTimKiemDocGia.add(txtTimDocGia);
        panelTimKiemDocGia.add(btnTimDocGia);

        // Đưa các khu vực lên chính DocGiaPanel
        add(panelTrenDocGia, BorderLayout.NORTH);
        add(new JScrollPane(tableDocGia), BorderLayout.CENTER);
        add(panelTimKiemDocGia, BorderLayout.SOUTH);

        tableDocGia.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
    }

    private void ganSuKien() {
        btnThemDocGia.addActionListener(e -> themDocGia());
        btnSuaDocGia.addActionListener(e -> suaDocGia());
        btnXoaDocGia.addActionListener(e -> xoaDocGia());

        btnTimDocGia.addActionListener(e -> {
            hienThiDocGia(txtTimDocGia.getText());
        });

        btnLamMoiDocGia.addActionListener(e -> {
            txtTimDocGia.setText("");
            xoaFormDocGia();
            hienThiDocGia();
        });

        tableDocGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                chonDocGia();
            }
        });
    }

    // Hiển thị toàn bộ độc giả
    private void hienThiDocGia() {
        hienThiDocGia("");
    }

    // Tìm và hiển thị độc giả theo họ tên
    private void hienThiDocGia(String tuKhoa) {
    	String sql = """
    		    SELECT id, ho_ten
    		    FROM doc_gia
    		    WHERE CAST(id AS TEXT) LIKE ?
    		       OR ho_ten LIKE ?
    		    ORDER BY id
    		    """;

        try (
            Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            String mauTim = "%" + tuKhoa.trim() + "%";
            statement.setString(1, mauTim); // ID
            statement.setString(2, mauTim); // Họ tên

            try (ResultSet result = statement.executeQuery()) {
                modelDocGia.setRowCount(0);

                while (result.next()) {
                    modelDocGia.addRow(new Object[]{
                        result.getInt("id"),
                        result.getString("ho_ten")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi đọc danh sách độc giả: " + e.getMessage()
            );
        }
    }

    private void xoaFormDocGia() {
        tableDocGia.clearSelection();
        txtMaDocGia.setText("");
        txtHoTen.setText("");
        txtMaDocGia.setEditable(true);
    }

    private int docIdDocGia() {
        try {
            int id = Integer.parseInt(txtMaDocGia.getText().trim());

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Mã độc giả phải là số nguyên lớn hơn 0."
            );
        }
    }
    
    private void themDocGia() {
        try {
            int maDocGia = docIdDocGia();
            String hoTen = txtHoTen.getText().trim();
            
            if (hoTen.isEmpty()) {
                throw new IllegalArgumentException(
                    "Bạn chưa nhập họ tên độc giả."
                );
            }
                    
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được để trống.");
                return;
            }
            
            String sql = "INSERT INTO doc_gia (id, ho_ten) VALUES (?, ?)";

            try (
                Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                statement.setInt(1, maDocGia);
                statement.setString(2, hoTen);
                statement.executeUpdate();
            }

            hienThiDocGia();
            xoaFormDocGia();

            JOptionPane.showMessageDialog(
                this,
                "Thêm độc giả thành công!"
            );

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Không thể thêm độc giả. Kiểm tra mã đã tồn tại chưa.\n"
                    + e.getMessage()
            );
        }
    }

    
    private void chonDocGia() {
        int dong = tableDocGia.getSelectedRow();

        if (dong == -1) {
            return;
        }

        txtMaDocGia.setText(
            tableDocGia.getValueAt(dong, 0).toString()
        );
        txtHoTen.setText(
            tableDocGia.getValueAt(dong, 1).toString()
        );
        
        txtMaDocGia.setEditable(false);
    }

    private void suaDocGia() {
        if (txtMaDocGia.getText().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn độc giả cần sửa."
            );
            return;
        }

        String hoTen = txtHoTen.getText().trim();

        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Họ tên không được để trống."
            );
            return;
        }

        int maDocGia = Integer.parseInt(
            txtMaDocGia.getText()
        );

        String sql = "UPDATE doc_gia SET ho_ten = ? WHERE id = ?";

        try (
            Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, hoTen);
            statement.setInt(2, maDocGia);
            statement.executeUpdate();

            hienThiDocGia();
            xoaFormDocGia();

            JOptionPane.showMessageDialog(
                this,
                "Sửa độc giả thành công!"
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi khi sửa độc giả: " + e.getMessage()
            );
        }
    }

    private void xoaDocGia() {
        if (txtMaDocGia.getText().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy chọn độc giả cần xóa."
            );
            return;
        }

        int luaChon = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xóa độc giả này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        int maDocGia = Integer.parseInt(
            txtMaDocGia.getText()
        );

        String sql = "DELETE FROM doc_gia WHERE id = ?";

        try (
            Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, maDocGia);
            statement.executeUpdate();

            hienThiDocGia();
            xoaFormDocGia();

            JOptionPane.showMessageDialog(
                this,
                "Xóa độc giả thành công!"
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Không thể xóa độc giả: " + e.getMessage()
            );
        }
    }
}