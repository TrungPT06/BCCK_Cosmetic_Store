#feature: từ khóa
Feature: kiểm tra và quản lý tồn kho mỹ phẩm theo lô
#mô tả ngắn gọn cho tính năng này
Như là hệ thống bán mỹ phẩm trực tuyến cần bảo đảm chỉ bán hàng còn trong kho và còn hạn dùng
Tôi muốn kiểm tra tồn khả dụng, giữ chỗ, chọn lô xuất theo FEFO và cảnh báo lô sắp hết hạn
Để không bán quá số lượng hiện có và không giao mỹ phẩm hết hạn cho khách

#mô tả giá trị kinh doanh (business value) tính năng bằng các kịch bản
Scenario Outline: kiểm tra, giữ chỗ và nhả tồn kho hợp lệ
  Given sản phẩm SERUM_VITC_30 có lô L001 số lượng <l001_qty> hạn sử dụng <l001_expiry> và lô L002 số lượng <l002_qty> hạn sử dụng <l002_expiry>
  And số lượng đang giữ chỗ là <reserved_qty> và ngưỡng hạn sử dụng tối thiểu khi giao là 6 tháng
  When người dùng T có vai trò <role> thực hiện thao tác <action> với số lượng <request_qty>
  Then T được thông báo kết quả là <check_result> với tồn kho khả dụng là <available_qty> và màu <color>
  And lô được chọn xuất theo nguyên tắc FEFO là <picked_batches>
  And kết quả thay đổi tồn kho được lưu lại <store>
  And tồn kho được kiểm tra vào ngày <current_day_vn>

  Examples:
    | current_day_vn | role     | l001_qty | l001_expiry | l002_qty | l002_expiry | reserved_qty | action            | request_qty | check_result   | available_qty | picked_batches | store | color  | reason                                             |
    | 04/11/2026     | customer | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 4           | DU_TON         | 10            | L001:4         | false | green  | ton_kha_dung_bang_ton_thuc_te_tru_giu_cho          |
    | 04/11/2026     | customer | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 10          | DU_TON         | 10            | L001:5+L002:5  | false | green  | yeu_cau_dung_bang_ton_kha_dung_van_du              |
    | 04/11/2026     | customer | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 7           | DU_TON         | 10            | L001:5+L002:2  | false | green  | xuat_het_lo_het_han_truoc_roi_lay_tiep_lo_sau_fefo |
    | 04/11/2026     | customer | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | GIU_CHO           | 4           | DA_GIU_CHO     | 6             | L001:4         | true  | green  | giu_cho_khi_dat_don_giam_ton_kha_dung              |
    | 04/11/2026     | he_thong | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | NHA_GIU_CHO       | 3           | DA_NHA_GIU_CHO | 13            | KHONG_AP_DUNG  | true  | yellow | don_huy_hoac_het_han_thanh_toan_nha_ton_kho        |
    | 04/11/2026     | customer | 5        | 01/10/2026  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 2           | DU_TON         | 5             | L002:2         | false | green  | lo_da_het_han_bi_loai_khoi_ton_kha_dung            |
    | 04/11/2026     | customer | 5        | 15/01/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 2           | DU_TON         | 5             | L002:2         | false | green  | lo_con_duoi_6_thang_khi_giao_bi_loai               |
    | 04/11/2026     | guest    | 0        | 01/09/2027  | 0        | 01/12/2027  | 0            | XEM_SAN_PHAM      | 0           | HET_HANG       | 0             | KHONG_AP_DUNG  | false | yellow | ton_kha_dung_bang_0_hien_nhan_het_hang             |


Scenario Outline: kiểm tra, giữ chỗ tồn kho không hợp lệ
  Given sản phẩm SERUM_VITC_30 có lô L001 số lượng <l001_qty> hạn sử dụng <l001_expiry> và lô L002 số lượng <l002_qty> hạn sử dụng <l002_expiry>
  And số lượng đang giữ chỗ là <reserved_qty> và ngưỡng hạn sử dụng tối thiểu khi giao là 6 tháng
  When người dùng T có vai trò customer thực hiện thao tác <action> với số lượng <request_qty>
  Then T được thông báo lỗi là <error_message> với màu red
  And tồn kho khả dụng vẫn là <available_qty> và không có thay đổi nào được lưu lại
  And tồn kho được kiểm tra vào ngày <current_day_vn>

  Examples:
    | current_day_vn | l001_qty | l001_expiry | l002_qty | l002_expiry | reserved_qty | action            | request_qty | error_message                       | available_qty | reason                                      |
    | 04/11/2026     | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 11          | VUOT_QUA_TON_KHO_KHA_DUNG           | 10            | so_luong_vuot_ton_kha_dung                  |
    | 04/11/2026     | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | 0           | SO_LUONG_PHAI_LA_SO_NGUYEN_DUONG    | 10            | so_luong_bang_0_khong_hop_le_khi_kiem_tra   |
    | 04/11/2026     | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | KIEM_TRA_SO_LUONG | -1          | SO_LUONG_PHAI_LA_SO_NGUYEN_KHONG_AM | 10            | so_luong_nhap_vao_la_so_am                  |
    | 04/11/2026     | 5        | 01/09/2027  | 8        | 01/12/2027  | 3            | GIU_CHO           | 11          | VUOT_QUA_TON_KHO_KHA_DUNG           | 10            | khong_giu_cho_vuot_ton_kha_dung             |
    | 04/11/2026     | 5        | 01/10/2026  | 8        | 15/01/2027  | 0            | KIEM_TRA_SO_LUONG | 1           | KHONG_CON_LO_DAT_DIEU_KIEN_BAN      | 0             | tat_ca_lo_het_han_hoac_can_han_duoi_6_thang |


Scenario Outline: quản trị viên nhập kho và điều chỉnh tồn kho hợp lệ
  Given người dùng T có vai trò admin và lô <batch_id> của sản phẩm SERUM_VITC_30 đang có <qty_before> sản phẩm
  When T thực hiện thao tác <action> với số lượng thay đổi <change_qty> và lý do <reason_input>
  Then T được thông báo số lượng lô sau thay đổi là <qty_after> với màu <color>
  And log thay đổi tồn kho (ai, khi nào, giá trị cũ và mới) và nhật ký kiểm toán được ghi
  And kết quả thay đổi tồn kho được lưu lại

  Examples:
    | batch_id | qty_before | action                 | change_qty | reason_input              | qty_after | color  | reason                                         |
    | L003     | 0          | NHAP_KHO               | 20         | NHAP_LO_MOI               | 20        | green  | admin_nhap_lo_moi_ghi_log_va_kiem_toan         |
    | L002     | 8          | DIEU_CHINH_SAU_KIEM_KE | -2         | KIEM_KE_PHAT_HIEN_HU_HONG | 6         | yellow | dieu_chinh_giam_sau_kiem_ke_ghi_gia_tri_cu_moi |


Scenario Outline: quản trị viên nhập kho và điều chỉnh tồn kho không hợp lệ
  Given người dùng T có vai trò <role> và lô <batch_id> của sản phẩm SERUM_VITC_30 đang có <qty_before> sản phẩm, hạn sử dụng <expiry_date>
  When T thực hiện thao tác <action> với số lượng thay đổi <change_qty> vào ngày 04/11/2026
  Then T được thông báo lỗi là <error_message> với màu red
  And số lượng lô giữ nguyên và không có thay đổi nào được lưu lại

  Examples:
    | role     | batch_id | qty_before | expiry_date | action                 | change_qty | error_message                    | reason                               |
    | customer | L003     | 0          | 01/01/2028  | NHAP_KHO               | 20         | KHONG_CO_QUYEN_QUAN_TRI          | customer_khong_duoc_nhap_kho         |
    | admin    | L002     | 8          | 01/12/2027  | DIEU_CHINH_SAU_KIEM_KE | -10        | SO_LUONG_LO_KHONG_DUOC_AM        | dieu_chinh_lam_so_luong_lo_am        |
    | admin    | L004     | 0          | 01/10/2026  | NHAP_KHO               | 5          | KHONG_NHAP_LO_DA_HET_HAN_SU_DUNG | khong_nhap_lo_da_het_han_vao_kho_ban |


Scenario Outline: cảnh báo lô sắp hết hạn cho quản trị viên
  Given một lô của sản phẩm SERUM_VITC_30 có hạn sử dụng <expiry_date> và ngưỡng cảnh báo là 90 ngày
  When hệ thống thực hiện thao tác QUET_CANH_BAO_LO_SAP_HET_HAN vào ngày 04/11/2026
  Then admin nhận cảnh báo <warning_status> với số ngày còn lại <days_left> và màu <color>

  Examples:
    | expiry_date | days_left | warning_status       | color  | reason                               |
    | 03/01/2027  | 60        | CANH_BAO_SAP_HET_HAN | yellow | lo_con_60_ngay_duoi_nguong_90_ngay   |
    | 02/02/2027  | 90        | CANH_BAO_SAP_HET_HAN | yellow | lo_con_dung_90_ngay_van_canh_bao     |
    | 01/09/2027  | 301       | KHONG_CANH_BAO       | green  | lo_con_han_dai_khong_canh_bao        |
    | 01/10/2026  | -34       | CANH_BAO_DA_HET_HAN  | red    | lo_da_het_han_can_xu_ly_khoi_kho_ban |
