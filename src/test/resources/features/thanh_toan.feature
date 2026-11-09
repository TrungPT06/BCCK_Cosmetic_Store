@thanh-toan
Feature: thanh toán đơn hàng mỹ phẩm
  Như là một khách hàng đã đặt đơn và chọn phương thức thanh toán
  Tôi muốn thanh toán đơn hàng qua cổng thanh toán hoặc COD và được hệ thống ghi nhận chính xác
  Để đơn hàng của tôi được xác nhận và không bị trừ tiền hai lần

  Background:
    Given hệ thống xử lý thanh toán vào ngày 04/11/2026

  Scenario Outline: xử lý thanh toán hợp lệ
    Given người dùng T có vai trò <role> đang xử lý đơn của khách an với phương thức <payment_method>, tổng thanh toán 700000 và trạng thái <order_status_before>
    And đơn được tạo cách đây <minutes_since_created> phút và đã có <recorded_before> giao dịch thành công
    When T thực hiện thao tác <action> và cổng thanh toán trả kết quả <gateway_result> với chữ ký hợp lệ và số tiền khớp với đơn
    Then T được thông báo trạng thái đơn là <order_status_after> và tình trạng thanh toán là <payment_status>
    And số tiền hoàn lại là <refund_amount> và tồn kho giữ chỗ được nhả là <stock_released>
    And đơn hàng được xử lý vào ngày 04/11/2026

    Examples:
      | role     | payment_method | order_status_before | minutes_since_created | recorded_before | action                         | gateway_result | order_status_after | payment_status  | refund_amount | stock_released |
      | customer | VNPAY          | CHO_THANH_TOAN      | 5                     | 0               | THANH_TOAN_ONLINE              | THANH_CONG     | DA_XAC_NHAN        | DA_THANH_TOAN   | 0             | false          |
      | customer | VNPAY          | DA_XAC_NHAN         | 10                    | 1               | THANH_TOAN_ONLINE              | THANH_CONG     | DA_XAC_NHAN        | DA_THANH_TOAN   | 0             | false          |
      | customer | VNPAY          | CHO_THANH_TOAN      | 8                     | 0               | THANH_TOAN_ONLINE              | THAT_BAI       | CHO_THANH_TOAN     | CHUA_THANH_TOAN | 0             | false          |
      | customer | MOMO           | CHO_THANH_TOAN      | 12                    | 0               | THANH_TOAN_LAI_DOI_PHUONG_THUC | THANH_CONG     | DA_XAC_NHAN        | DA_THANH_TOAN   | 0             | false          |
      | admin    | COD            | DA_GIAO             | 4320                  | 0               | XAC_NHAN_THU_TIEN_COD          | KHONG_AP_DUNG  | DA_GIAO            | DA_THANH_TOAN   | 0             | false          |
      | he_thong | VNPAY          | CHO_THANH_TOAN      | 31                    | 0               | HUY_QUA_HAN_THANH_TOAN         | KHONG_AP_DUNG  | DA_HUY             | CHUA_THANH_TOAN | 0             | true           |
      | customer | VNPAY          | DA_XAC_NHAN         | 60                    | 1               | HUY_DON                        | KHONG_AP_DUNG  | DA_HUY             | DA_HOAN_TIEN    | 700000        | true           |
      | customer | COD            | DA_XAC_NHAN         | 10                    | 0               | HUY_DON                        | KHONG_AP_DUNG  | DA_HUY             | CHUA_THANH_TOAN | 0             | true           |


  Scenario Outline: xử lý thanh toán không hợp lệ
    Given người dùng T là khách an có vai trò <role> đang xử lý đơn thuộc về khách <owner> với phương thức <payment_method>, tổng thanh toán 700000 và trạng thái <order_status_before>
    When T thực hiện thao tác <action> và cổng thanh toán trả kết quả <gateway_result> với số tiền <gateway_amount> và chữ ký <signature>
    Then T được thông báo lỗi là <error_message>
    And trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận
    And đơn hàng được xử lý vào ngày 04/11/2026

    Examples:
      | role     | owner | payment_method | order_status_before | action                | gateway_result | gateway_amount | signature     | error_message                                |
      | guest    | an    | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | YEU_CAU_DANG_NHAP_HOAC_DANG_KY               |
      | customer | binh  | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_KHONG_THUOC_VE_NGUOI_DUNG                |
      | customer | an    | VNPAY          | DA_XAC_NHAN         | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN        |
      | customer | an    | VNPAY          | DA_HUY              | THANH_TOAN_ONLINE     | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN        |
      | customer | an    | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | THANH_CONG     | 700000         | KHONG_HOP_LE  | CHU_KY_CALLBACK_KHONG_HOP_LE                 |
      | customer | an    | VNPAY          | CHO_THANH_TOAN      | THANH_TOAN_ONLINE     | THANH_CONG     | 500000         | HOP_LE        | SO_TIEN_KHONG_KHOP_VOI_DON                   |
      | customer | an    | COD            | DA_GIAO             | XAC_NHAN_THU_TIEN_COD | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | KHONG_CO_QUYEN_QUAN_TRI                      |
      | admin    | an    | COD            | DANG_GIAO           | XAC_NHAN_THU_TIEN_COD | KHONG_AP_DUNG  | 0              | KHONG_AP_DUNG | DON_COD_CHUA_GIAO_CHUA_THE_XAC_NHAN_THU_TIEN |


  Scenario: từ chối hủy đơn khi thời gian chưa quá hạn 30 phút
    Given người dùng T có vai trò he_thong đang xử lý đơn của khách an với phương thức VNPAY, tổng thanh toán 700000 và trạng thái CHO_THANH_TOAN
    And đơn được tạo cách đây 15 phút và đã có 0 giao dịch thành công
    When T thực hiện thao tác HUY_QUA_HAN_THANH_TOAN và cổng thanh toán trả kết quả KHONG_AP_DUNG với chữ ký hợp lệ và số tiền khớp với đơn
    Then T được thông báo lỗi là CHUA_QUA_HAN_THANH_TOAN
    And trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận


  Scenario: từ chối hủy đơn hàng khi đơn đã giao thành công 
    Given người dùng T có vai trò customer đang xử lý đơn của khách an với phương thức COD, tổng thanh toán 700000 và trạng thái DA_GIAO
    And đơn được tạo cách đây 100 phút và đã có 0 giao dịch thành công
    When T thực hiện thao tác HUY_DON và cổng thanh toán trả kết quả KHONG_AP_DUNG với chữ ký hợp lệ và số tiền khớp với đơn
    Then T được thông báo lỗi là KHONG_THE_HUY_DON_DA_GIAO
    And trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận

  Scenario: từ chối hủy quá hạn khi đơn COD đã được xác nhận
    Given người dùng T có vai trò he_thong đang xử lý đơn của khách an với phương thức COD, tổng thanh toán 700000 và trạng thái DA_XAC_NHAN
    And đơn được tạo cách đây 60 phút và đã có 0 giao dịch thành công
    When T thực hiện thao tác HUY_QUA_HAN_THANH_TOAN và cổng thanh toán trả kết quả KHONG_AP_DUNG với chữ ký hợp lệ và số tiền khớp với đơn
    Then T được thông báo lỗi là DON_KHONG_O_TRANG_THAI_CHO_THANH_TOAN
    And trạng thái đơn không thay đổi và không có giao dịch nào được ghi nhận
