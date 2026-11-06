#feature: từ khóa
Feature: kiểm tra và theo dõi đơn hàng mỹ phẩm
#mô tả ngắn gọn cho tính năng này
Như là một khách hàng hoặc quản trị viên của cửa hàng mỹ phẩm trực tuyến
Tôi muốn xem trạng thái, lịch sử, hủy hoặc yêu cầu trả hàng và quản lý đơn hàng đúng vòng đời
Để biết đơn đang ở đâu, xử lý kịp thời và không làm sai lệch dữ liệu đơn hàng

#mô tả giá trị kinh doanh (business value) tính năng bằng các kịch bản
Scenario Outline: xem, hủy, trả hàng và cập nhật trạng thái đơn hàng hợp lệ
  Given người dùng T có vai trò <role> và đơn của khách an đang ở trạng thái <current_status>, tổng tiền 650000, tình trạng thanh toán <payment_status>
  And đơn được giao cách đây <days_since_delivered> ngày với thời hạn đổi trả 7 ngày và sản phẩm đang ở tình trạng <seal_status>
  When T thực hiện thao tác <action> sang trạng thái <target_status> với lý do <reason_input> và bằng chứng <evidence>
  Then T được thông báo trạng thái đơn mới là <new_status> với màu <color>
  And số tiền hoàn lại là <refund_amount> và tồn kho giữ chỗ được nhả là <stock_released>
  And lịch sử trạng thái được ghi thêm khi trạng thái thay đổi
  And đơn hàng được xử lý vào ngày 04/11/2026

  Examples:
    | role     | current_status | payment_status  | days_since_delivered | seal_status   | action            | target_status | reason_input                 | evidence                 | new_status       | refund_amount | stock_released | color  | reason                                             |
    | customer | DA_XAC_NHAN    | DA_THANH_TOAN   | KHONG_AP_DUNG        | KHONG_AP_DUNG | XEM_DON_HANG      | KHONG_AP_DUNG | KHONG_AP_DUNG                | KHONG_AP_DUNG            | DA_XAC_NHAN      | 0.0           | false          | green  | customer_xem_don_cua_chinh_minh                    |
    | customer | CHO_THANH_TOAN | CHUA_THANH_TOAN | KHONG_AP_DUNG        | KHONG_AP_DUNG | HUY_DON           | KHONG_AP_DUNG | KHONG_AP_DUNG                | KHONG_AP_DUNG            | DA_HUY           | 0.0           | true           | yellow | customer_huy_don_chua_thanh_toan_nha_ton_kho       |
    | customer | DA_XAC_NHAN    | DA_THANH_TOAN   | KHONG_AP_DUNG        | KHONG_AP_DUNG | HUY_DON           | KHONG_AP_DUNG | KHONG_AP_DUNG                | KHONG_AP_DUNG            | DA_HUY           | 650000.0      | true           | yellow | customer_huy_don_da_thanh_toan_tao_hoan_tien       |
    | customer | DA_GIAO        | DA_THANH_TOAN   | 3                    | DA_MO_SEAL    | YEU_CAU_TRA_HANG  | KHONG_AP_DUNG | GIAO_SAI_SAN_PHAM            | ANH_MO_HOP               | YEU_CAU_TRA_HANG | 0.0           | false          | yellow | tra_hang_trong_han_do_giao_sai_co_bang_chung       |
    | customer | DA_GIAO        | DA_THANH_TOAN   | 2                    | CHUA_MO_SEAL  | YEU_CAU_TRA_HANG  | KHONG_AP_DUNG | HU_HONG_KHI_VAN_CHUYEN       | VIDEO_MO_HOP             | YEU_CAU_TRA_HANG | 0.0           | false          | yellow | tra_hang_hu_hong_khi_van_chuyen_co_video           |
    | admin    | DA_XAC_NHAN    | DA_THANH_TOAN   | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DANG_CHUAN_BI | KHONG_AP_DUNG                | KHONG_AP_DUNG            | DANG_CHUAN_BI    | 0.0           | false          | green  | admin_chuyen_trang_thai_dung_chieu                 |
    | admin    | DANG_CHUAN_BI  | DA_THANH_TOAN   | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DANG_GIAO     | KHONG_AP_DUNG                | KHONG_AP_DUNG            | DANG_GIAO        | 0.0           | false          | green  | admin_ban_giao_cho_don_vi_van_chuyen               |
    | admin    | DANG_GIAO      | DA_THANH_TOAN   | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DA_GIAO       | KHONG_AP_DUNG                | XAC_NHAN_GIAO_THANH_CONG | DA_GIAO          | 0.0           | false          | green  | chi_chuyen_da_giao_khi_co_xac_nhan_giao_thanh_cong |
    | admin    | DA_XAC_NHAN    | DA_THANH_TOAN   | KHONG_AP_DUNG        | KHONG_AP_DUNG | HUY_DON           | KHONG_AP_DUNG | KHACH_YEU_CAU_QUA_DIEN_THOAI | KHONG_AP_DUNG            | DA_HUY           | 650000.0      | true           | yellow | admin_huy_don_co_ly_do_tao_hoan_tien               |


Scenario Outline: xem, hủy, trả hàng và cập nhật trạng thái đơn hàng không hợp lệ
  Given người dùng T là khách an có vai trò <role> và đơn thuộc về khách <owner> đang ở trạng thái <current_status>
  And đơn được giao cách đây <days_since_delivered> ngày với thời hạn đổi trả 7 ngày và sản phẩm đang ở tình trạng <seal_status>
  When T thực hiện thao tác <action> sang trạng thái <target_status> với lý do <reason_input> và bằng chứng <evidence>
  Then T được thông báo lỗi là <error_message> với màu red
  And trạng thái đơn không thay đổi và không có thay đổi nào được lưu lại
  And đơn hàng được xử lý vào ngày 04/11/2026

  Examples:
    | role     | owner | current_status | days_since_delivered | seal_status   | action            | target_status | reason_input           | evidence      | error_message                                           | reason                                              |
    | guest    | an    | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | XEM_DON_HANG      | KHONG_AP_DUNG | KHONG_AP_DUNG          | KHONG_AP_DUNG | YEU_CAU_DANG_NHAP_HOAC_DANG_KY                          | guest_khong_xem_duoc_don                            |
    | customer | binh  | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | XEM_DON_HANG      | KHONG_AP_DUNG | KHONG_AP_DUNG          | KHONG_AP_DUNG | DON_KHONG_THUOC_VE_NGUOI_DUNG                           | customer_khong_xem_duoc_don_cua_nguoi_khac          |
    | customer | an    | DANG_GIAO      | KHONG_AP_DUNG        | KHONG_AP_DUNG | HUY_DON           | KHONG_AP_DUNG | KHONG_AP_DUNG          | KHONG_AP_DUNG | KHONG_THE_HUY_DON_DA_CHUYEN_SANG_DANG_GIAO              | khong_huy_don_dang_giao                             |
    | customer | an    | DA_GIAO        | 1                    | CHUA_MO_SEAL  | HUY_DON           | KHONG_AP_DUNG | KHONG_AP_DUNG          | KHONG_AP_DUNG | KHONG_THE_HUY_DON_DA_CHUYEN_SANG_DANG_GIAO              | khong_huy_don_da_giao                               |
    | customer | an    | DA_GIAO        | 10                   | CHUA_MO_SEAL  | YEU_CAU_TRA_HANG  | KHONG_AP_DUNG | GIAO_SAI_SAN_PHAM      | ANH_MO_HOP    | QUA_THOI_HAN_DOI_TRA                                    | qua_7_ngay_ke_tu_khi_nhan_hang                      |
    | customer | an    | DA_GIAO        | 2                    | DA_MO_SEAL    | YEU_CAU_TRA_HANG  | KHONG_AP_DUNG | KHONG_HOP_DA           | ANH_MO_HOP    | SAN_PHAM_DA_MO_SEAL_KHONG_THUOC_TRUONG_HOP_DUOC_DOI_TRA | da_mo_seal_khong_phai_loi_nha_san_xuat_hay_giao_sai |
    | customer | an    | DA_GIAO        | 2                    | CHUA_MO_SEAL  | YEU_CAU_TRA_HANG  | KHONG_AP_DUNG | HU_HONG_KHI_VAN_CHUYEN | KHONG_CO      | THIEU_BANG_CHUNG_ANH_VIDEO_MO_HOP                       | thieu_anh_video_mo_hop                              |
    | customer | an    | DA_GIAO        | 2                    | CHUA_MO_SEAL  | YEU_CAU_TRA_HANG  | KHONG_AP_DUNG | TRA_RIENG_QUA_TANG     | ANH_MO_HOP    | QUA_TANG_KHONG_DUOC_DOI_TRA_RIENG                       | qua_tang_va_mau_thu_khong_doi_tra_rieng             |
    | customer | an    | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DANG_CHUAN_BI | KHONG_AP_DUNG          | KHONG_AP_DUNG | KHONG_CO_QUYEN_QUAN_TRI                                 | customer_khong_dung_chuc_nang_quan_tri_don          |
    | customer | an    | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | TIM_KIEM_DON_HANG | KHONG_AP_DUNG | KHONG_AP_DUNG          | KHONG_AP_DUNG | KHONG_CO_QUYEN_QUAN_TRI                                 | customer_khong_tim_kiem_don_toan_he_thong           |
    | admin    | an    | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DANG_GIAO     | KHONG_AP_DUNG          | KHONG_AP_DUNG | CHUYEN_TRANG_THAI_KHONG_HOP_LE                          | nhay_coc_bo_qua_buoc_dang_chuan_bi                  |
    | admin    | an    | DANG_GIAO      | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DANG_CHUAN_BI | KHONG_AP_DUNG          | KHONG_AP_DUNG | CHUYEN_TRANG_THAI_KHONG_HOP_LE                          | quay_nguoc_trang_thai_khong_duoc_phep               |
    | admin    | an    | DANG_GIAO      | KHONG_AP_DUNG        | KHONG_AP_DUNG | CHUYEN_TRANG_THAI | DA_GIAO       | KHONG_AP_DUNG          | KHONG_AP_DUNG | CHUA_CO_XAC_NHAN_GIAO_THANH_CONG                        | chua_co_xac_nhan_giao_thanh_cong                    |
    | admin    | an    | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | HUY_DON           | KHONG_AP_DUNG | KHONG_CO               | KHONG_AP_DUNG | THIEU_LY_DO_HUY_DON                                     | admin_huy_don_bat_buoc_co_ly_do                     |
    | admin    | an    | DA_XAC_NHAN    | KHONG_AP_DUNG        | KHONG_AP_DUNG | SUA_DONG_HANG     | KHONG_AP_DUNG | DOI_SO_LUONG_HOAC_GIA  | KHONG_AP_DUNG | KHONG_DUOC_SUA_DONG_HANG_CUA_DON_DA_XAC_NHAN            | khong_sua_dong_hang_va_gia_cua_don_da_xac_nhan      |


Scenario Outline: quản trị viên tìm kiếm đơn hàng
  Given người dùng T có vai trò admin và hệ thống có 25 đơn mẫu tạo trong tháng 11/2026 gồm 5 đơn DA_XAC_NHAN của an, 7 đơn DA_XAC_NHAN của binh và 13 đơn DA_GIAO của binh, trong đó DH001 là đơn của an
  When T thực hiện thao tác TIM_KIEM_DON_HANG theo mã đơn <filter_order_id>, khách hàng <filter_customer>, trạng thái <filter_status> và khoảng ngày <filter_date_range> với kích thước trang 10
  Then T được thông báo có <matched_count> đơn thỏa điều kiện chia thành <total_pages> trang với màu <color>

  Examples:
    | filter_order_id | filter_customer | filter_status | filter_date_range     | matched_count | total_pages | color  | reason                                        |
    | KHONG_AP_DUNG   | KHONG_AP_DUNG   | KHONG_AP_DUNG | KHONG_AP_DUNG         | 25            | 3           | green  | khong_loc_hien_tat_ca_don_co_phan_trang       |
    | KHONG_AP_DUNG   | KHONG_AP_DUNG   | DA_XAC_NHAN   | KHONG_AP_DUNG         | 12            | 2           | green  | loc_theo_trang_thai_da_xac_nhan               |
    | KHONG_AP_DUNG   | an              | KHONG_AP_DUNG | KHONG_AP_DUNG         | 5             | 1           | green  | loc_theo_khach_hang                           |
    | DH001           | KHONG_AP_DUNG   | KHONG_AP_DUNG | KHONG_AP_DUNG         | 1             | 1           | green  | tim_theo_ma_don_chinh_xac                     |
    | KHONG_AP_DUNG   | binh            | DA_GIAO       | KHONG_AP_DUNG         | 13            | 2           | green  | ket_hop_khach_hang_va_trang_thai              |
    | KHONG_AP_DUNG   | KHONG_AP_DUNG   | KHONG_AP_DUNG | 01/11/2026-30/11/2026 | 25            | 3           | green  | loc_theo_khoang_ngay_trong_thang_11           |
    | KHONG_AP_DUNG   | KHONG_AP_DUNG   | KHONG_AP_DUNG | 15/10/2026-31/10/2026 | 0             | 0           | yellow | khong_co_don_trong_khoang_ngay_thong_bao_rong |
