#feature: từ khóa
Feature: thanh toán đơn hàng mỹ phẩm
#mô tả ngắn gọn cho tính năng này
Như là một khách hàng đã đặt đơn và chọn phương thức thanh toán
Tôi muốn thanh toán đơn hàng qua cổng thanh toán hoặc COD và được hệ thống ghi nhận chính xác
Để đơn hàng của tôi được xác nhận và không bị trừ tiền hai lần

#mô tả giá trị kinh doanh (business value) tính năng bằng các kịch bản
Scenario Outline: xử lý thanh toán hợp lệ
  Given người dùng T có vai trò <role> đang xử lý đơn của khách an với phương thức <payment_method>, tổng thanh toán 700000 và trạng thái <order_status_before>
  And đơn được tạo cách đây <minutes_since_created> phút (thời hạn thanh toán online là 30 phút) và đã có <recorded_before> giao dịch thành công
  When T thực hiện thao tác <action> và cổng thanh toán trả kết quả <gateway_result> (chữ ký hợp lệ, số tiền khớp với đơn)
  Then T được thông báo trạng thái đơn là <order_status_after> và tình trạng thanh toán là <payment_status> với màu <color>
  And số tiền hoàn lại là <refund_amount> và tồn kho giữ chỗ được nhả là <stock_released>
  And đơn hàng được xử lý vào ngày 04/11/2026

  Examples:
    | role     | payment_method | order_status_before | minutes_since_created | recorded_before | action                         | gateway_result | order_status_after | payment_status  | refund_amount | stock_released | color  | reason                                             |
    | customer | VNPAY          | CHO_THANH_TOAN      | 5                     | 0               | THANH_TOAN_ONLINE              | THANH_CONG     | DA_XAC_NHAN        | DA_THANH_TOAN   | 0.0           | false          | green  | thanh_toan_online_thanh_cong_xac_nhan_don          |
    | customer | VNPAY          | DA_XAC_NHAN         | 10                    | 1               | THANH_TOAN_ONLINE              | THANH_CONG     | DA_XAC_NHAN        | DA_THANH_TOAN   | 0.0           | false          | yellow | callback_lap_khong_ghi_nhan_hai_lan_idempotent     |
    | customer | VNPAY          | CHO_THANH_TOAN      | 8                     | 0               | THANH_TOAN_ONLINE              | THAT_BAI       | CHO_THANH_TOAN     | CHUA_THANH_TOAN | 0.0           | false          | yellow | thanh_toan_that_bai_giu_don_cho_thu_lai            |
    | customer | MOMO           | CHO_THANH_TOAN      | 12                    | 0               | THANH_TOAN_LAI_DOI_PHUONG_THUC | THANH_CONG     | DA_XAC_NHAN        | DA_THANH_TOAN   | 0.0           | false          | green  | doi_phuong_thuc_sau_khi_that_bai_khong_tao_don_moi |
    | admin    | COD            | DA_GIAO             | 4320                  | 0               | XAC_NHAN_THU_TIEN_COD          | KHONG_AP_DUNG  | DA_GIAO            | DA_THANH_TOAN   | 0.0           | false          | green  | admin_xac_nhan_da_thu_tien_cod_sau_khi_giao        |
    | he_thong | VNPAY          | CHO_THANH_TOAN      | 31                    | 0               | HUY_QUA_HAN_THANH_TOAN         | KHONG_AP_DUNG  | DA_HUY             | CHUA_THANH_TOAN | 0.0           | true           | yellow | qua_30_phut_chua_thanh_toan_tu_huy_va_nha_ton_kho  |
    | customer | VNPAY          | DA_XAC_NHAN         | 60                    | 1               | HUY_DON                        | KHONG_AP_DUNG  | DA_HUY             | DA_HOAN_TIEN    | 700000.0      | true           | yellow | huy_don_da_thanh_toan_hoan_tien_ve_vnpay           |


Scenario Outline: xử lý thanh toán không hợp lệ
  Given người dùng T là khách an có vai trò <role> đang xử lý đơn thuộc về khách <owner> với phương thức <payment_method>, tổng thanh toán 700000 và trạng thái <order_status_before>
  When T thực hiện thao tác <action> và cổng thanh toán trả kết quả <gateway_result> với số tiền <gateway_amount> và chữ ký <signature>
  Then T được thông báo lỗi là <error_message> với màu red
  And trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận
  And đơn hàng được xử lý vào ngày 04/11/2026

  Examples:
    | role     | owner | payment_method | order_status_before | action                | gateway_result | gateway_amount | signature     | error_message                                | reason                                         |
    | guest    | an    | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | YEU_CAU_DANG_NHAP_HOAC_DANG_KY               | guest_khong_duoc_thanh_toan                    |
    | customer | binh  | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_KHONG_THUOC_VE_NGUOI_DUNG                | khong_thanh_toan_don_cua_nguoi_khac            |
    | customer | an    | VNPAY          | DA_XAC_NHAN         | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN        | don_da_xac_nhan_khong_thanh_toan_lai           |
    | customer | an    | VNPAY          | DA_HUY              | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN        | don_da_huy_khong_the_thanh_toan                |
    | customer | an    | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | THANH_CONG     | 700000         | KHONG_HOP_LE  | CHU_KY_CALLBACK_KHONG_HOP_LE                 | chu_ky_callback_sai_bo_qua_ket_qua             |
    | customer | an    | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | THANH_CONG     | 500000         | HOP_LE        | SO_TIEN_KHONG_KHOP_VOI_DON                   | so_tien_cong_bao_khac_tong_thanh_toan_cua_don  |
    | customer | an    | COD            | DA_GIAO             | XAC_NHAN_THU_TIEN_COD | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | KHONG_CO_QUYEN_QUAN_TRI                      | customer_khong_duoc_xac_nhan_thu_tien_cod      |
    | admin    | an    | COD            | DANG_GIAO           | XAC_NHAN_THU_TIEN_COD | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_COD_CHUA_GIAO_CHUA_THE_XAC_NHAN_THU_TIEN | cod_chi_ghi_nhan_da_thanh_toan_khi_da_thu_tien |
