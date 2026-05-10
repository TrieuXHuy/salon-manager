# PHÂN TÍCH NGHIỆP VỤ HỆ THỐNG QUẢN LÝ SALON

## 1. Mục tiêu hệ thống

Xây dựng hệ thống quản lý salon tóc desktop bằng Java Swing hỗ trợ:

- quản lý khách hàng
- quản lý dịch vụ
- quản lý lịch hẹn
- quản lý nhân viên
- thanh toán
- báo cáo doanh thu
- nhắc lịch bằng SMS
- phân quyền người dùng

Hệ thống phải mô phỏng gần giống salon thực tế thay vì CRUD đơn giản.

---

# 2. Các vai trò trong hệ thống

# ADMIN

Quản trị hệ thống.

## Chức năng
- quản lý tài khoản
- phân quyền
- quản lý nhân viên
- quản lý dịch vụ
- xem tất cả lịch hẹn
- xem báo cáo doanh thu
- xem thống kê
- cấu hình hệ thống

---

# RECEPTIONIST (Lễ tân)

Người thao tác chính với khách.

## Chức năng
- tạo lịch hẹn
- chỉnh sửa lịch hẹn
- hủy lịch
- check-in khách
- tìm kiếm khách hàng
- tạo khách hàng mới
- thanh toán
- gửi SMS xác nhận

---

# EMPLOYEE / STYLIST

Nhân viên làm dịch vụ.

## Chức năng
- xem lịch cá nhân
- xem thông tin khách
- cập nhật trạng thái lịch
- đánh dấu hoàn thành dịch vụ

---

# CUSTOMER

Khách hàng của salon.

## Thông tin cần quản lý
- họ tên
- số điện thoại
- email
- giới tính
- lịch sử sử dụng dịch vụ

---

# 3. Workflow nghiệp vụ đặt lịch

# FLOW CHÍNH

```text
Khách hàng
    ↓
Tạo lịch hẹn
    ↓
Chọn dịch vụ
    ↓
Chọn nhân viên
    ↓
Kiểm tra trùng lịch
    ↓
Xác nhận lịch
    ↓
Khách check-in
    ↓
Đang thực hiện
    ↓
Hoàn thành
    ↓
Thanh toán
    ↓
Gửi SMS cảm ơn
```

---

# 4. Workflow chi tiết lịch hẹn

# Bước 1 - Tạo lịch

Lễ tân nhập:
- khách hàng
- dịch vụ
- nhân viên thực hiện
- thời gian
- ghi chú

Hệ thống:
- tính tổng thời gian
- kiểm tra nhân viên có bị trùng lịch không

Trạng thái:

```text
PENDING
```

---

# Bước 2 - Xác nhận lịch

Sau khi xác nhận:

```text
CONFIRMED
```

Hệ thống có thể:
- gửi SMS
- gửi thông báo

---

# Bước 3 - Khách đến salon

Lễ tân check-in:

```text
CHECKED_IN
```

---

# Bước 4 - Nhân viên bắt đầu làm

Nhân viên nhận khách:

```text
IN_PROGRESS
```

---

# Bước 5 - Hoàn thành

Sau khi làm xong:

```text
COMPLETED
```

---

# Bước 6 - Thanh toán

Các phương thức:
- tiền mặt
- momo
- chuyển khoản
- thẻ

Payment status:

```text
PAID
UNPAID
REFUNDED
```

---

# Bước 7 - Chăm sóc khách hàng

Hệ thống:
- gửi SMS cảm ơn
- lưu lịch sử
- nhắc lịch quay lại

---

# 5. Trạng thái lịch hẹn

Appointment status:

```text
PENDING
CONFIRMED
CHECKED_IN
IN_PROGRESS
COMPLETED
CANCELLED
NO_SHOW
```

Giải thích:

| Trạng thái | Ý nghĩa |
|---|---|
| PENDING | Mới tạo |
| CONFIRMED | Đã xác nhận |
| CHECKED_IN | Khách đã tới |
| IN_PROGRESS | Đang thực hiện |
| COMPLETED | Hoàn thành |
| CANCELLED | Đã hủy |
| NO_SHOW | Khách không tới |

---

# 6. Yêu cầu nghiệp vụ quan trọng

# 6.1 Không được trùng lịch nhân viên

Ví dụ:

```text
Stylist A:
09:00 - 11:00 đã có lịch
```

=> không cho đặt tiếp.

Hệ thống phải:
- tính duration của dịch vụ
- kiểm tra overlap thời gian

---

# 6.2 Một lịch có nhiều dịch vụ

Ví dụ:
- cắt tóc
- nhuộm
- hấp tóc

=> appointment phải hỗ trợ multiple services.

---

# 6.3 Tính tổng tiền tự động

Hệ thống tự:
- cộng tiền dịch vụ
- áp dụng giảm giá
- tính final amount

---

# 6.4 Theo dõi lịch sử khách hàng

Có thể xem:
- đã dùng dịch vụ gì
- stylist yêu thích
- tổng chi tiêu

---

# 7. Thiết kế database đề xuất

# users

```text
id
username
password
role
is_active
created_at
```

---

# employees

```text
id
full_name
phone
email
specialization
role
is_active
```

---

# customers

```text
id
full_name
phone
email
gender
created_at
```

---

# services

```text
id
name
description
duration_minutes
price
is_active
```

---

# appointments

```text
id
customer_id
employee_id
appointment_time
estimated_end_time
status
note
created_at
```

---

# appointment_services

```text
id
appointment_id
service_id
price
duration_minutes
```

---

# payments

```text
id
appointment_id
subtotal
discount_amount
final_amount
payment_method
payment_status
paid_at
```

---

# sms_logs

```text
id
appointment_id
phone
message
status
sent_at
```

---

# 8. Các màn hình chính cần có

# 1. Dashboard

Hiển thị:
- lịch hôm nay
- doanh thu hôm nay
- khách mới
- top dịch vụ
- top nhân viên

---

# 2. Quản lý lịch hẹn

Chức năng:
- tạo lịch
- sửa lịch
- hủy lịch
- tìm kiếm
- lọc trạng thái
- check-in

Hiển thị:
- timeline
- calendar
- table

---

# 3. Quản lý khách hàng

- CRUD khách hàng
- lịch sử sử dụng dịch vụ
- tổng chi tiêu

---

# 4. Quản lý nhân viên

- CRUD nhân viên
- lịch làm việc
- KPI
- doanh thu cá nhân

---

# 5. Quản lý dịch vụ

- CRUD dịch vụ
- giá
- thời lượng

---

# 6. Thanh toán

- tạo hóa đơn
- chọn phương thức thanh toán
- in hóa đơn

---

# 7. Báo cáo

- doanh thu theo ngày/tháng
- top dịch vụ
- top nhân viên
- tỷ lệ hủy lịch

---

# 9. Yêu cầu UI/UX

Thiết kế hiện đại:
- tone sáng
- pastel
- rounded corner
- card dashboard
- sidebar đẹp
- status badge màu
- table hiện đại
- calendar scheduling

Không sử dụng Swing mặc định quá cũ.

Bắt buộc:
- FlatLaf
- custom component
- spacing đẹp
- icon hiện đại

---

# 10. Yêu cầu kỹ thuật

Backend:
- Java
- JDBC hoặc JPA/Hibernate

Desktop:
- Java Swing
- FlatLaf

Database:
- SQL Server hoặc MySQL

Architecture:
- MVC
- layered architecture

---

# 11. Điểm nổi bật hệ thống cần hướng tới

Hệ thống không chỉ CRUD.

Phải có:
- workflow thật
- role-based system
- scheduling
- employee assignment
- appointment lifecycle
- dashboard analytics
- payment flow
- notification flow

Mục tiêu cuối:
- giống phần mềm salon thực tế
- giao diện hiện đại
- thao tác chuyên nghiệp
- business flow rõ ràng

