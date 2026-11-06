@dat-hang
#feature: từ khóa
Feature: đặt hàng mỹ phẩm từ giỏ hàng
#mô tả ngắn gọn cho tính năng này
Như là một khách hàng đã có tài khoản và đã chọn sẵn mỹ phẩm trong giỏ
Tôi muốn chốt giỏ hàng thành đơn hàng với thông tin giao hàng, voucher và phương thức thanh toán
Để tôi nhận được mỹ phẩm chính hãng, còn hạn dùng tại địa chỉ của mình

#mô tả giá trị kinh doanh (business value) tính năng bằng các kịch bản
Scenario Outline: đặt hàng hợp lệ
  Given người dùng T có vai trò <role> đã xác thực tài khoản
  And giỏ hàng của T có <qty> sản phẩm <sku> với đơn giá <unit_price>, còn hạn sử dụng trên 6 tháng và tồn kho khả dụng <stock_qty>
  And thông tin giao hàng của T hợp lệ và khu vực giao hàng có phí vận chuyển 30000
  And T áp dụng voucher <voucher> có giá trị giảm <discount>
  When T thực hiện thao tác DAT_HANG với phương thức thanh toán <payment_method>
  Then T được thông báo trạng thái đơn hàng là <order_status> với tổng thanh toán là <total_amount> và màu <color>
  And phí vận chuyển áp dụng là <shipping_fee> và tồn kho khả dụng của sản phẩm <sku> còn <remaining_stock>
  And đơn hàng được lưu lại và sản phẩm <sku> được xóa khỏi giỏ hàng
  And đơn hàng được xử lý vào ngày <current_day_vn>

  Examples:
    | current_day_vn | role     | sku            | qty | unit_price | stock_qty | voucher    | discount | payment_method | order_status   | shipping_fee | total_amount | remaining_stock | color  | reason                                                |
    | 04/11/2026     | customer | SERUM_VITC_30  | 2   | 450000     | 12        | KHONG_DUNG | 0        | COD            | DA_XAC_NHAN    | 0            | 900000.0     | 10              | green  | dat_cod_hop_le_mien_phi_van_chuyen_tu_500k            |
    | 04/11/2026     | customer | SON_3CE_TAUPE  | 1   | 350000     | 15        | KHONG_DUNG | 0        | VNPAY          | CHO_THANH_TOAN | 30000        | 380000.0     | 14              | yellow | dat_online_cho_thanh_toan_giu_cho_ton_kho             |
    | 04/11/2026     | admin    | PHAN_NUOC_CLIO | 2   | 600000     | 20        | GIAM50K    | 50000    | COD            | DA_XAC_NHAN    | 0            | 1150000.0    | 18              | green  | admin_dat_hang_nhu_customer_co_voucher_hop_le         |
    | 04/11/2026     | customer | KEM_CHONG_NANG | 1   | 500000     | 8         | KHONG_DUNG | 0        | MOMO           | CHO_THANH_TOAN | 0            | 500000.0     | 7               | yellow | tien_hang_dung_500k_duoc_mien_phi_van_chuyen          |
    | 04/11/2026     | customer | KEM_CHONG_NANG | 1   | 499000     | 8         | KHONG_DUNG | 0        | MOMO           | CHO_THANH_TOAN | 30000        | 529000.0     | 7               | yellow | tien_hang_499k_chua_du_mien_phi_van_chuyen            |
    | 04/11/2026     | customer | SON_3CE_TAUPE  | 1   | 350000     | 15        | GIAM500K   | 500000   | VNPAY          | CHO_THANH_TOAN | 30000        | 30000.0      | 14              | yellow | giam_gia_toi_da_bang_tien_hang_khong_lam_tien_hang_am |


Scenario Outline: đặt hàng không hợp lệ
  Given người dùng T có vai trò <role> đã xác thực tài khoản là <verified>
  And giỏ hàng của T có <qty> sản phẩm <sku> với đơn giá <unit_price>, hạn sử dụng <expiry_date> và tồn kho khả dụng <stock_qty>
  And thông tin giao hàng của T là <shipping_info>
  And T áp dụng voucher <voucher>
  When T thực hiện thao tác DAT_HANG với phương thức thanh toán <payment_method>
  Then T được thông báo lỗi là <error_message> với màu red
  And không có đơn hàng nào được tạo, tồn kho khả dụng và giỏ hàng giữ nguyên
  And đơn hàng được xử lý vào ngày <current_day_vn>

  Examples:
    | current_day_vn | role     | verified | sku            | qty | unit_price | expiry_date   | stock_qty | shipping_info    | voucher    | payment_method | error_message                                 | reason                                          |
    | 04/11/2026     | guest    | false    | SON_3CE_TAUPE  | 1   | 350000     | 15/08/2027    | 15        | HOP_LE           | KHONG_DUNG | COD            | YEU_CAU_DANG_NHAP_HOAC_DANG_KY                | guest_khong_duoc_dat_hang                       |
    | 04/11/2026     | customer | false    | SERUM_VITC_30  | 1   | 450000     | 01/12/2027    | 12        | HOP_LE           | KHONG_DUNG | COD            | TAI_KHOAN_CHUA_XAC_THUC                       | tai_khoan_chua_xac_thuc_email                   |
    | 04/11/2026     | customer | true     | KHONG_CO       | 0   | 0          | KHONG_AP_DUNG | 0         | HOP_LE           | KHONG_DUNG | COD            | GIO_HANG_TRONG                                | gio_hang_khong_co_dong_hang_hop_le              |
    | 04/11/2026     | customer | true     | SON_3CE_TAUPE  | 5   | 350000     | 15/08/2027    | 3         | HOP_LE           | KHONG_DUNG | COD            | VUOT_QUA_TON_KHO_KHA_DUNG                     | so_luong_vuot_ton_kho_kha_dung_bao_dong_loi     |
    | 04/11/2026     | customer | true     | MAT_NA_LANEIGE | 1   | 600000     | 15/01/2027    | 8         | HOP_LE           | KHONG_DUNG | COD            | SAN_PHAM_CAN_HAN_DUOI_6_THANG                 | my_pham_can_han_duoi_6_thang_khi_giao           |
    | 04/11/2026     | customer | true     | TAY_TRANG_BIO  | 1   | 400000     | 01/10/2026    | 4         | HOP_LE           | KHONG_DUNG | COD            | SAN_PHAM_DA_HET_HAN_SU_DUNG                   | my_pham_da_het_han_su_dung                      |
    | 04/11/2026     | customer | true     | SERUM_VITC_30  | 1   | 450000     | 01/12/2027    | 12        | THIEU_DIA_CHI    | KHONG_DUNG | COD            | THIEU_THONG_TIN_GIAO_HANG                     | thieu_dia_chi_nhan_hang                         |
    | 04/11/2026     | customer | true     | SERUM_VITC_30  | 1   | 450000     | 01/12/2027    | 12        | SDT_KHONG_HOP_LE | KHONG_DUNG | COD            | SO_DIEN_THOAI_KHONG_HOP_LE                    | so_dien_thoai_nhan_hang_sai_dinh_dang           |
    | 04/11/2026     | customer | true     | SERUM_VITC_30  | 1   | 450000     | 01/12/2027    | 12        | HOP_LE           | HETHAN     | VNPAY          | VOUCHER_DA_HET_HAN                            | voucher_het_han                                 |
    | 04/11/2026     | customer | true     | SERUM_VITC_30  | 1   | 450000     | 01/12/2027    | 12        | HOP_LE           | HETLUOT    | VNPAY          | VOUCHER_HET_LUOT_SU_DUNG                      | voucher_het_luot_dung                           |
    | 04/11/2026     | customer | true     | SERUM_VITC_30  | 1   | 450000     | 01/12/2027    | 12        | HOP_LE           | DONLON     | VNPAY          | VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU            | don_hang_chua_dat_gia_tri_toi_thieu_cua_voucher |
    | 04/11/2026     | customer | true     | SERUM_VITC_30  | 10  | 450000     | 01/12/2027    | 12        | HOP_LE           | KHONG_DUNG | COD            | COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC | don_cod_gia_tri_lon_yeu_cau_thanh_toan_truoc    |


Scenario Outline: đặt hàng có sản phẩm đặc thù và quà tặng kèm
  Given người dùng T có vai trò customer có <qty> sản phẩm <sku> trong giỏ hàng hợp lệ
  And chương trình tặng quà <gift_sku> có tồn kho riêng là <gift_stock>
  When T thực hiện thao tác DAT_HANG với phương thức thanh toán COD
  Then đơn hàng được gắn cờ <order_flag> với dòng quà tặng có đơn giá <gift_line_price>
  And tồn kho riêng của quà tặng còn <gift_stock_after> và đơn hàng được lưu lại

  Examples:
    | sku              | qty | gift_sku          | gift_stock | order_flag   | gift_line_price | gift_stock_after | reason                                             |
    | NUOC_HOA_ROSE_50 | 1   | KHONG_CO          | 0          | HANG_HAN_CHE | KHONG_AP_DUNG   | 0                | nuoc_hoa_duoc_gan_co_hang_han_che                  |
    | SERUM_VITC_30    | 1   | MAU_THU_SERUM_5ML | 20         | KHONG_CO     | 0.0             | 19               | qua_tang_kem_don_khong_tinh_tien_tru_ton_kho_rieng |
