package thu_vien;

public class TaiKhoan {

    private final String tenDangNhap;
    private final String vaiTro;
    private final Integer maDocGia;

    public TaiKhoan(
            String tenDangNhap,
            String vaiTro,
            Integer maDocGia) {

        this.tenDangNhap = tenDangNhap;
        this.vaiTro = vaiTro;
        this.maDocGia = maDocGia;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public Integer getMaDocGia() {
        return maDocGia;
    }

    public boolean laThuThu() {
        return "THU_THU".equals(vaiTro);
    }

    public boolean laDocGia() {
        return "DOC_GIA".equals(vaiTro);
    }
}