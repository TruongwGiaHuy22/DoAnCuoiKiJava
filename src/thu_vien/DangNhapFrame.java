package thu_vien;

import java.awt.GridLayout; // Trình quản lý bố cục dạng lưới (chia giao diện thành các hàng và cột đều nhau để đặt các thành phần như nhãn, ô nhập liệu).
import java.sql.Connection; //Quản lý kết nối đến cơ sở dữ liệu.
import java.sql.PreparedStatement;//Dùng để thực thi các câu lệnh SQL có tham số
import java.sql.ResultSet;//Bảng chứa kết quả trả về từ cơ sở dữ liệu.
import java.sql.SQLException; // dùng để bắt lỗi cơ sở dữ liệu (như trùng khóa, mất kết nối), giúp phần mềm không bị văng (crash) mà hiện thông báo lỗi lịch sự cho người dùng qua

import javax.swing.JButton;//Tạo nút bấm tương tác (ví dụ: nút "Đăng nhập").
import javax.swing.JFrame; //Toàn bộ cái khung cửa sổ bự có thanh tiêu đề chữ "Đăng nhập thư viện" và mấy nút thu nhỏ, phóng to, dấu nhân [ - □ X ] ở góc trên bên phải chính là JFrame.
import javax.swing.JLabel;//là dòng chữ chú thích xuất hiện trên giao diện mà người dùng không thể bấm vào để sửa hay gõ chữ vào đó được.
import javax.swing.JOptionPane; //Tạo các hộp thoại pop-up thông báo nhỏ (như báo lỗi sai mật khẩu hoặc nhắc nhở khi để trống ô nhập).
import javax.swing.JPanel; // Tạo khung cửa sổ chính của ứng dụng. Phần khoảng trống bên trong (nơi dán các ô chữ, nút bấm lên) =
import javax.swing.JPasswordField; //Tạo ô nhập mật khẩu (tự động ẩn ký tự thành dấu chấm tròn • để bảo mật).
import javax.swing.JTextField; //Tạo ô nhập văn bản một dòng (dùng cho ô nhập tên đăng nhập).

public class DangNhapFrame extends JFrame {

    private final JTextField txtTenDangNhap =
        new JTextField();

    private final JPasswordField txtMatKhau =
        new JPasswordField();

    public DangNhapFrame() {
        setTitle("Đăng nhập thư viện");
        setSize(360, 180); // chiều dài 360 cao 180
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8)); //3 hàng 2 cột

        panel.add(new JLabel("Tên đăng nhập:"));
        panel.add(txtTenDangNhap); // gõ tk

        panel.add(new JLabel("Mật khẩu:"));
        panel.add(txtMatKhau); // gõ mật khẩu

        JButton btnDangNhap = new JButton("Đăng nhập");
        panel.add(new JLabel());
        panel.add(btnDangNhap);

        add(panel);
        btnDangNhap.addActionListener(e -> dangNhap());
        getRootPane().setDefaultButton(btnDangNhap);
    }

    private void dangNhap() {
        String tenDangNhap =
            txtTenDangNhap.getText().trim();

        String matKhau =
            new String(txtMatKhau.getPassword());
//Vì ô mật khẩu trả về kiểu mảng ký tự (char[]) để bảo mật bộ nhớ, ta phải bọc nó vào new String(...) 
      //để chuyển thành chuỗi String bình thường.
        
        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Hãy nhập tên đăng nhập và mật khẩu."
            );
            return;
        }

        String sql = """
            SELECT ten_dang_nhap, vai_tro, doc_gia_id
            FROM tai_khoan
            WHERE ten_dang_nhap = ?
              AND mat_khau_hash = ?
            """;

        try (
            Connection connection = Database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {
            statement.setString(1, tenDangNhap);
            statement.setString(
                2,
                BaoMat.bamMatKhau(matKhau)
            );

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Sai tên đăng nhập hoặc mật khẩu."
                    );
                    txtMatKhau.setText("");
                    return;
                }

                String vaiTro =
                    result.getString("vai_tro");

                int id = result.getInt("doc_gia_id");
                Integer maDocGia = result.wasNull()
                    ? null
                    : id;

                TaiKhoan taiKhoan = new TaiKhoan(
                    result.getString("ten_dang_nhap"),
                    vaiTro,
                    maDocGia
                );

                LibraryApp app = new LibraryApp(taiKhoan);
                app.setVisible(true);
                dispose();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi đăng nhập: " + e.getMessage()
            );
        }
    }
}