@dat-hang
Feature: đặt hàng mỹ phẩm từ giỏ hàng
  Như là một khách hàng đã có tài khoản và đã chọn sẵn mỹ phẩm trong giỏ
  Tôi muốn chốt giỏ hàng thành đơn hàng với thông tin giao hàng, voucher và phương thức thanh toán
  Để tôi nhận được mỹ phẩm chính hãng, còn hạn dùng tại địa chỉ của mình

  Background:
    Given hệ thống xử lý các giao dịch vào ngày 04/11/2026

  Scenario Outline: đặt hàng hợp lệ với chính sách phí vận chuyển và voucher
    Given người dùng T có vai trò <role> đã xác thực tài khoản
    And giỏ hàng của T có <qty> sản phẩm <sku> với đơn giá <unit_price>, còn hạn sử dụng trên 6 tháng và tồn kho khả dụng <stock_qty>
    And thông tin giao hàng của T hợp lệ và khu vực giao hàng có phí vận chuyển 30000
    And T áp dụng voucher <voucher>
    When T đặt hàng với phương thức thanh toán <payment_method>
    Then T được thông báo trạng thái đơn hàng là <order_status> với tổng thanh toán là <total_amount>
    And phí vận chuyển áp dụng là <shipping_fee> và tồn kho khả dụng của sản phẩm <sku> còn <remaining_stock>
    And đơn hàng được lưu lại
    And đơn hàng được xử lý vào ngày <current_day_vn>

    Examples:
      | current_day_vn | role     | sku            | qty | unit_price | stock_qty | voucher    | payment_method | order_status   | shipping_fee | total_amount | remaining_stock |
      | 04/11/2026     | customer | SERUM_VITC_30  | 2   | 450000     | 12        | KHONG_DUNG | COD            | DA_XAC_NHAN    | 0            | 900000       | 10              |
      | 04/11/2026     | customer | SON_3CE_TAUPE  | 1   | 350000     | 15        | KHONG_DUNG | VNPAY          | CHO_THANH_TOAN | 30000        | 380000       | 14              |
      | 04/11/2026     | admin    | PHAN_NUOC_CLIO | 2   | 600000     | 20        | GIAM50K    | COD            | DA_XAC_NHAN    | 0            | 1150000      | 18              |
      | 04/11/2026     | customer | KEM_CHONG_NANG | 1   | 500000     | 8         | KHONG_DUNG | MOMO           | CHO_THANH_TOAN | 0            | 500000       | 7               |
      | 04/11/2026     | customer | KEM_CHONG_NANG | 1   | 499000     | 8         | KHONG_DUNG | MOMO           | CHO_THANH_TOAN | 30000        | 529000       | 7               |
      | 04/11/2026     | customer | SON_3CE_TAUPE  | 1   | 350000     | 15        | GIAM500K   | VNPAY          | CHO_THANH_TOAN | 30000        | 30000        | 14              |


  Scenario Outline: từ chối đặt hàng do tài khoản hoặc giỏ hàng không hợp lệ
    Given người dùng T có vai trò <role> đã xác thực tài khoản là <verified>
    And giỏ hàng của T có <qty> sản phẩm <sku> với đơn giá <unit_price>, hạn sử dụng 15/08/2027 và tồn kho khả dụng <stock_qty>
    And thông tin giao hàng của T là HOP_LE
    And T áp dụng voucher KHONG_DUNG
    When T đặt hàng với phương thức thanh toán COD
    Then T được thông báo lỗi là <error_message>
    And không có đơn hàng nào được tạo và tồn kho khả dụng giữ nguyên
    And đơn hàng được xử lý vào ngày 04/11/2026

    Examples:
      | role     | verified | sku           | qty | unit_price | stock_qty | error_message                  |
      | guest    | false    | SON_3CE_TAUPE | 1   | 350000     | 15        | YEU_CAU_DANG_NHAP_HOAC_DANG_KY |
      | customer | false    | SERUM_VITC_30 | 1   | 450000     | 12        | TAI_KHOAN_CHUA_XAC_THUC        |
      | customer | true     | KHONG_CO      | 0   | 0          | 0         | GIO_HANG_TRONG                 |
      | customer | true     | SON_3CE_TAUPE | 5   | 350000     | 3         | VUOT_QUA_TON_KHO_KHA_DUNG      |


  Scenario Outline: từ chối đặt hàng do hạn sử dụng hoặc thông tin giao hàng
    Given người dùng T có vai trò customer đã xác thực tài khoản là true
    And giỏ hàng của T có 1 sản phẩm <sku> với đơn giá <unit_price>, hạn sử dụng <expiry_date> và tồn kho khả dụng 10
    And thông tin giao hàng của T là <shipping_info>
    And T áp dụng voucher KHONG_DUNG
    When T đặt hàng với phương thức thanh toán COD
    Then T được thông báo lỗi là <error_message>
    And không có đơn hàng nào được tạo và tồn kho khả dụng giữ nguyên
    And đơn hàng được xử lý vào ngày 04/11/2026

    Examples:
      | sku            | unit_price | expiry_date | shipping_info    | error_message                 |
      | MAT_NA_LANEIGE | 600000     | 15/01/2027  | HOP_LE           | SAN_PHAM_CAN_HAN_DUOI_6_THANG |
      | TAY_TRANG_BIO  | 400000     | 01/10/2026  | HOP_LE           | SAN_PHAM_DA_HET_HAN_SU_DUNG   |
      | SERUM_VITC_30  | 450000     | 01/12/2027  | THIEU_DIA_CHI    | THIEU_THONG_TIN_GIAO_HANG     |
      | SERUM_VITC_30  | 450000     | 01/12/2027  | SDT_KHONG_HOP_LE | SO_DIEN_THOAI_KHONG_HOP_LE    |


  Scenario Outline: từ chối đặt hàng do điều kiện voucher hoặc hạn mức thanh toán
    Given người dùng T có vai trò customer đã xác thực tài khoản là true
    And giỏ hàng của T có <qty> sản phẩm SERUM_VITC_30 với đơn giá 450000, hạn sử dụng 01/12/2027 và tồn kho khả dụng 12
    And thông tin giao hàng của T là HOP_LE
    And T áp dụng voucher <voucher>
    When T đặt hàng với phương thức thanh toán <payment_method>
    Then T được thông báo lỗi là <error_message>
    And không có đơn hàng nào được tạo và tồn kho khả dụng giữ nguyên
    And đơn hàng được xử lý vào ngày 04/11/2026

    Examples:
      | qty | voucher    | payment_method | error_message                                 |
      | 1   | HETHAN     | VNPAY          | VOUCHER_DA_HET_HAN                            |
      | 1   | HETLUOT    | VNPAY          | VOUCHER_HET_LUOT_SU_DUNG                      |
      | 1   | DONLON     | VNPAY          | VOUCHER_CHUA_DAT_GIA_TRI_TOI_THIEU            |
      | 10  | KHONG_DUNG | COD            | COD_VUOT_NGUONG_3000000_PHAI_THANH_TOAN_TRUOC |


  Scenario Outline: đặt hàng có sản phẩm đặc thù và quà tặng kèm
    Given người dùng T có vai trò customer có <qty> sản phẩm <sku> trong giỏ hàng hợp lệ
    And chương trình tặng quà <gift_sku> có tồn kho riêng là <gift_stock>
    When T đặt hàng với phương thức thanh toán COD
    Then đơn hàng được gắn cờ <order_flag> với dòng quà tặng có đơn giá <gift_line_price>
    And tồn kho riêng của quà tặng còn <gift_stock_after> và đơn hàng được lưu lại

    Examples:
      | sku              | qty | gift_sku          | gift_stock | order_flag   | gift_line_price | gift_stock_after |
      | NUOC_HOA_ROSE_50 | 1   | KHONG_CO          | 0          | HANG_HAN_CHE | 0               | 0                |
      | SERUM_VITC_30    | 1   | MAU_THU_SERUM_5ML | 20         | KHONG_CO     | 0               | 19               |


  Scenario: từ chối đặt hàng khi voucher đã hết lượt sử dụng sau lần đặt trước (Bug 3)
    Given người dùng T có vai trò customer đã xác thực tài khoản
    And giỏ hàng của T có 1 sản phẩm SERUM_VITC_30 với đơn giá 450000, còn hạn sử dụng trên 6 tháng và tồn kho khả dụng 10
    And voucher VOUCHER_1_LAN có mức giảm 50000 và chỉ còn 1 lượt dùng
    When T đặt hàng với voucher VOUCHER_1_LAN và phương thức thanh toán COD
    Then đơn hàng lần 1 thành công và voucher VOUCHER_1_LAN còn 0 lượt dùng
    When T tiếp tục đặt đơn thứ 2 với voucher VOUCHER_1_LAN
    Then T được thông báo lỗi là VOUCHER_HET_LUOT_SU_DUNG
