package thu_vien;

import java.awt.BorderLayout; // Giúp chia không gian của một cửa sổ hoặc khung thành 5 vùng riêng biệt: NORTH, SOUTH, EAST, WEST, CENTER
import java.awt.Color; // Quản lý và định nghĩa các mã màu sắc dùng để tô màu nền, màu chữ hoặc vẽ biểu đồ
import java.awt.Dimension; // Quản lý kích thước chiều rộng và chiều cao cố định của các thành phần giao diện
import java.awt.Font; // Định nghĩa kiểu phông chữ, kích thước chữ và định dạng (chữ đậm, chữ thường)
import java.awt.Graphics; // Cung cấp các công cụ đồ họa cơ bản để vẽ hình khối, chữ viết thủ công lên Panel
import java.awt.Graphics2D; // Phiên bản nâng cao của Graphics, hỗ trợ các tính năng vẽ đồ họa sắc nét và xử lý nâng cao
import java.awt.GridLayout; // Sắp xếp các thành phần giao diện theo dạng lưới chia đều các hàng và cột
import java.awt.RenderingHints; // Thiết lập các tùy chọn chất lượng vẽ đồ họa (như bật chế độ khử răng cưa KEY_ANTIALIASING giúp biểu đồ mượt mà)
import java.sql.Connection; // Quản lý phiên kết nối vật lý giữa ứng dụng Java và cơ sở dữ liệu SQLite
import java.sql.PreparedStatement; // Thực thi câu lệnh SQL có tham số (dùng dấu ?), giúp chống SQL Injection và tăng hiệu suất
import java.sql.ResultSet; // Bảng tạm lưu trữ tập hợp dữ liệu kết quả trả về sau khi thực hiện câu lệnh truy vấn SELECT
import java.sql.SQLException; // Lớp ngoại lệ chuyên bắt các lỗi phát sinh liên quan đến cơ sở dữ liệu
import java.sql.Statement; // Dùng để thực thi các câu lệnh SQL tĩnh không có tham số
import java.time.LocalDate; // Lấy và xử lý ngày tháng năm hiện tại (năm-tháng-ngày)
import java.time.YearMonth; // Quản lý định dạng kết hợp cả Năm và Tháng (dùng để lọc mốc thời gian 6 tháng gần nhất trên biểu đồ)
import javax.swing.BorderFactory; // Cung cấp các phương thức tiện ích để tạo viền khung (border) và khoảng cách lề cho các Panel, Nhãn
import javax.swing.JLabel; // Tạo các dòng chữ nhãn hiển thị thông tin số liệu thống kê
import javax.swing.JOptionPane; // Tạo các hộp thoại popup thông báo nhỏ (báo lỗi khi đọc dữ liệu thống kê)
import javax.swing.JPanel; // Tạo các khung chứa nhỏ bên trong giao diện (dùng làm khung thẻ số liệu hoặc vùng tự vẽ biểu đồ)
import javax.swing.SwingConstants; // Cung cấp các hằng số căn chỉnh vị trí chữ (ví dụ: căn giữa SwingConstants.CENTER)
public class ThongKePanel extends JPanel {

	private static final int SO_THANG_HIEN_THI = 6;
	
    private JLabel lblDauSach = taoNhan();
    private JLabel lblTongSoBan = taoNhan();
    private JLabel lblDangMuon = taoNhan();
    private JLabel lblDocGia = taoNhan();
    private JLabel lblLuotMuon = taoNhan();
    private JLabel lblQuaHan = taoNhan();

    private BieuDoMuonSach bieuDo = new BieuDoMuonSach();

    public ThongKePanel() {
        taoGiaoDien();
        lamMoiThongKe();
    }

    private JLabel taoNhan() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        );
        return label;
    }

    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelSoLieu = new JPanel(new GridLayout(2, 3, 10, 10));
        panelSoLieu.add(lblDauSach);
        panelSoLieu.add(lblTongSoBan);
        panelSoLieu.add(lblDangMuon);
        panelSoLieu.add(lblDocGia);
        panelSoLieu.add(lblLuotMuon);
        panelSoLieu.add(lblQuaHan);

        panelSoLieu.setPreferredSize(new Dimension(700, 130));

        add(panelSoLieu, BorderLayout.NORTH);
        add(bieuDo, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // LibraryApp gọi lại phương thức này khi mở tab Thống kê.
    public void lamMoiThongKe() {
        try (Connection connection = Database.getConnection()) {
            int dauSach = dem(
                connection,
                "SELECT COUNT(*) FROM sach"
            );

            int tongSoBan = dem(
                connection,
                "SELECT COALESCE(SUM(so_luong), 0) FROM sach"
            );

            int docGia = dem(
                connection,
                "SELECT COUNT(*) FROM doc_gia"
            );

            int luotMuon = dem(
                connection,
                "SELECT COUNT(*) FROM phieu_muon"
            );

            int dangMuon = dem(
                connection,
                "SELECT COUNT(*) FROM phieu_muon "
                    + "WHERE ngay_tra IS NULL"
            );

            int quaHan;
            String sqlQuaHan = """
                SELECT COUNT(*)
                FROM phieu_muon
                WHERE ngay_tra IS NULL
                  AND han_tra < ?
                """;

            try (PreparedStatement statement =
                    connection.prepareStatement(sqlQuaHan)) {

                statement.setString(
                    1, LocalDate.now().toString()
                );

                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    quaHan = result.getInt(1);
                }
            }

            lblDauSach.setText("Đầu sách: " + dauSach);
            lblTongSoBan.setText(
                "Tổng số bản: " + tongSoBan
            );
            lblDangMuon.setText(
                "Bản đang mượn: " + dangMuon
            );
            lblDocGia.setText("Độc giả: " + docGia);
            lblLuotMuon.setText(
                "Tổng lượt mượn: " + luotMuon
            );
            lblQuaHan.setText(
                "Phiếu quá hạn: " + quaHan
            );

            taiDuLieuBieuDo(connection);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi đọc thống kê: " + e.getMessage()
            );
        }
    }

    private int dem(Connection connection, String sql)
            throws SQLException {

        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            result.next();
            return result.getInt(1);
        }
    }

    private void taiDuLieuBieuDo(Connection connection)
            throws SQLException {

        String[] thang = new String[SO_THANG_HIEN_THI];
        int[] soLuot = new int[SO_THANG_HIEN_THI];

        YearMonth thangDau = YearMonth.now()
        	    .minusMonths(SO_THANG_HIEN_THI - 1);

        for (int i = 0; i < SO_THANG_HIEN_THI; i++) {
            thang[i] = thangDau.plusMonths(i).toString();
        }

        String sql = """
            SELECT SUBSTR(ngay_muon, 1, 7) AS thang,
                   COUNT(*) AS so_luot
            FROM phieu_muon
            WHERE ngay_muon >= ?
            GROUP BY SUBSTR(ngay_muon, 1, 7)
            ORDER BY thang
            """;

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setString(
                1, thangDau.atDay(1).toString()
            );

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String thangTrongDb =
                        result.getString("thang");

                    for (int i = 0; i < SO_THANG_HIEN_THI; i++) {
                        if (thang[i].equals(thangTrongDb)) {
                            soLuot[i] = result.getInt("so_luot");
                            break;
                        }
                    }
                }
            }
        }

        bieuDo.capNhat(thang, soLuot);
    }

    // JPanel tự vẽ biểu đồ cột.
    private static class BieuDoMuonSach extends JPanel {

    	private String[] thang = new String[SO_THANG_HIEN_THI];
    	private int[] soLuot = new int[SO_THANG_HIEN_THI];

        public BieuDoMuonSach() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(700, 320));
        }

        public void capNhat(String[] thangMoi, int[] soLuotMoi) {
            thang = thangMoi;
            soLuot = soLuotMoi;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();

            try {
                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.setColor(Color.DARK_GRAY);
                g.drawString(
                	    "Lượt mượn theo tháng (" + SO_THANG_HIEN_THI + " tháng gần nhất)",
                	    20, 30
                	);
                
                
                int trai = 55;
                int phai = getWidth() - 20;
                int tren = 65;
                int duoi = getHeight() - 45;

                if (phai <= trai || duoi <= tren) {
                    return;
                }

                g.setColor(Color.GRAY);
                g.drawLine(trai, duoi, phai, duoi);

                int caoNhat = 1;
                for (int so : soLuot) {
                    caoNhat = Math.max(caoNhat, so);
                }

                int khoangMoiCot = (phai - trai) / SO_THANG_HIEN_THI;
                int rongCot = Math.max(12, khoangMoiCot / 2);
                int chieuCaoKhaDung = duoi - tren;

                for (int i = 0; i < SO_THANG_HIEN_THI; i++) {
                    int chieuCaoCot =
                        soLuot[i] * chieuCaoKhaDung / caoNhat;

                    int x = trai + i * khoangMoiCot
                        + (khoangMoiCot - rongCot) / 2;

                    int y = duoi - chieuCaoCot;

                    g.setColor(new Color(65, 120, 210));
                    g.fillRect(x, y, rongCot, chieuCaoCot);

                    g.setColor(Color.DARK_GRAY);
                    g.drawString(
                        String.valueOf(soLuot[i]),
                        x + 5,
                        Math.max(tren, y - 5)
                    );

                    String nhanThang = thang[i] == null
                        ? ""
                        : thang[i].substring(5) + "/"
                            + thang[i].substring(2, 4);

                    g.drawString(
                        nhanThang,
                        x,
                        duoi + 20
                    );
                }

            } finally {
                g.dispose();
            }
        }
    }
}