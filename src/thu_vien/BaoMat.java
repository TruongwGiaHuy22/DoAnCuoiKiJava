package thu_vien;

import java.nio.charset.StandardCharsets;
//Giúp chuyển đổi chuỗi mật khẩu văn bản tiếng Việt/tiếng Anh bình thường thành mảng các con số byte 
//để thuật toán băm có thể đọc và xử lý được.
import java.security.MessageDigest;//Đây chính là "cỗ máy" chính thực hiện việc băm mật khẩu theo chuẩn SHA-256.
import java.security.NoSuchAlgorithmException; //Thư viện bắt lỗi ngoại lệ khi không tìm thấy thuật toán
import java.util.HexFormat;
//Sau khi "xay" mật khẩu thành một dãy byte mã hóa ngoằn ngoèo, thư viện này giúp đổi nó thành một chuỗi ký tự thông thường 
//(gồm các số từ 0-9 và chữ từ a-f) cho gọn gàng để dễ dàng lưu vào cơ sở dữ liệu.
public class BaoMat {

    public static String bamMatKhau(String matKhau) {
        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");
//Dòng này dùng để khởi tạo bộ băm theo thuật toán chuẩn bảo mật quốc tế SHA-256.
            byte[] duLieu = matKhau.getBytes(
                StandardCharsets.UTF_8
            );

            return HexFormat.of().formatHex(
                digest.digest(duLieu)
            );
//Tiến hành "xay" mảng byte mật khẩu thành một dãy byte mã hóa kết quả.
//Chuyển dãy byte kết quả đó thành một chuỗi dạng ký tự Hex (gồm các chữ số từ 0-9 và chữ từ a-f) 
  //dài cố định để đem đi lưu vào cơ sở dữ liệu hoặc đi so sánh.
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "Không thể mã hóa mật khẩu.", e
            );
        }
    }
}
//dùng để bảo mật mật khẩu người dùng trước khi lưu xuống cơ sở dữ liệu. 
//Em sử dụng thuật toán chuẩn SHA-256 để băm chuỗi mật khẩu thành dạng mã hóa một chiều qua hàm bamMatKhau(), 
//giúp đảm bảo an toàn thông tin ngay cả khi cơ sở dữ liệu bị lộ ạ."