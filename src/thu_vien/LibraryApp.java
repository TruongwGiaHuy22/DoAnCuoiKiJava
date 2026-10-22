package thu_vien;

import java.awt.BorderLayout; //giúp chia không gian của một cửa sổ hoặc khung (JFrame, JPanel) thành 5 vùng riêng biệt: Bắc (NORTH), Nam (SOUTH), Đông (EAST), Tây (WEST), và Trung tâm (CENTER)
import java.awt.FlowLayout;//rình quản lý bố cục sắp xếp các thành phần (nút bấm, nhãn chữ) lần lượt theo hàng ngang từ trái qua phải, khi hết chỗ sẽ tự động xuống hàng
import java.sql.SQLException;//Lớp ngoại lệ (Exception) chuyên dùng để bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu (như mất kết nối file library.db

import javax.swing.JButton;//Tạo nút bấm tương tác cho người dùng (ví dụ: nút Đăng nhập, Thêm, Sửa, Xóa).   
import javax.swing.JFrame; //ạo cửa sổ ứng dụng chính (có thanh tiêu đề, nút thu nhỏ, phóng to, đóng [ - □ X ])
import javax.swing.JLabel;//Tạo các dòng chữ nhãn tĩnh hướng dẫn (ví dụ: "Tên đăng nhập:", "Mã sách:").   
import javax.swing.JOptionPane;//Tạo các hộp thoại popup thông báo nhỏ (báo lỗi sai mật khẩu, xác nhận xóa, thông báo thành công
import javax.swing.JPanel;//Tạo các khung chứa (panel) nhỏ bên trong cửa sổ để gom nhóm các ô nhập liệu hoặc nút bấm
import javax.swing.JTabbedPane;//Tạo thanh tab chuyển đổi qua lại giữa các màn hình chức năng (như Sách, Độc giả, Thống kê).
import javax.swing.SwingUtilities;//Đảm bảo các luồng khởi tạo giao diện chạy an toàn (Thread-safe) trong Java.

public class LibraryApp extends JFrame {

    public LibraryApp(TaiKhoan taiKhoan) {
        setTitle("Quản lý thư viện");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        // Cả hai vai trò đều được xem danh sách sách.
        SachPanel sachPanel = new SachPanel(taiKhoan.laThuThu());
        tabs.addTab("Sách", sachPanel);

        if (taiKhoan.laThuThu()) {
            // Chỉ tạo các panel quản lý khi đăng nhập thủ thư.
            DocGiaPanel docGiaPanel = new DocGiaPanel();
            PhieuMuonPanel phieuMuonPanel =
                new PhieuMuonPanel();
            ThongKePanel thongKePanel =
                new ThongKePanel();

            tabs.addTab("Độc giả", docGiaPanel);
            tabs.addTab("Mượn / Trả", phieuMuonPanel);
            tabs.addTab("Thống kê", thongKePanel);

            tabs.addChangeListener(e -> {
                if (tabs.getSelectedComponent() == sachPanel) {
                    sachPanel.lamMoiDanhSach();
                }

                if (tabs.getSelectedComponent() == thongKePanel) {
                    thongKePanel.lamMoiThongKe();
                }
            });

        } else {
            // Chỉ xem phiếu gắn với mã độc giả đang đăng nhập.
            PhieuCuaToiPanel phieuCuaToiPanel =
                new PhieuCuaToiPanel(
                    taiKhoan.getMaDocGia()
                );

            tabs.addTab(
                "Phiếu mượn của tôi",
                phieuCuaToiPanel
            );

            tabs.addChangeListener(e -> {
                if (tabs.getSelectedComponent() == sachPanel) {
                    sachPanel.lamMoiDanhSach();
                }

                if (tabs.getSelectedComponent()
                        == phieuCuaToiPanel) {
                    phieuCuaToiPanel.lamMoiDanhSach();
                }
            });
        }

        // Thanh thông tin tài khoản và nút đăng xuất.
        JPanel thanhTren = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        JLabel lblTaiKhoan = new JLabel(
            "Đang đăng nhập: "
                + taiKhoan.getTenDangNhap()
                + " (" + taiKhoan.getVaiTro() + ")"
        );

        JButton btnDangXuat =
            new JButton("Đăng xuất");

        btnDangXuat.addActionListener(e -> {
            dispose();

            DangNhapFrame dangNhap =
                new DangNhapFrame();

            dangNhap.setVisible(true);
        });

        thanhTren.add(lblTaiKhoan);
        thanhTren.add(btnDangXuat);

        add(thanhTren, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            // Tạo bảng và tài khoản demo trước khi mở giao diện.
            Database.initialize();

            SwingUtilities.invokeLater(() -> {
                DangNhapFrame dangNhap = new DangNhapFrame();
                dangNhap.setVisible(true);
            });

        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    null,
                    "Không thể khởi tạo cơ sở dữ liệu: "
                        + e.getMessage(),
                    "Lỗi khởi động",
                    JOptionPane.ERROR_MESSAGE
                );
            });
        }
    }
}