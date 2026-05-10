# Thiết Kế Lại Hệ Thống Salon Manager

## 1. Nhận định từ tài liệu nghiệp vụ

Tài liệu mô tả đúng hướng của một phần mềm salon thực tế:

- có nhiều vai trò thao tác khác nhau
- lịch hẹn có vòng đời rõ ràng
- một lịch có thể gồm nhiều dịch vụ
- có kiểm tra trùng lịch nhân viên
- có thanh toán, lịch sử khách hàng, báo cáo và nhắc lịch

Điểm quan trọng là hệ thống này không nên thiết kế theo CRUD rời rạc. Trục trung tâm phải là `appointment workflow`.

## 2. Vấn đề của cấu trúc hiện tại

Code hiện tại mới đáp ứng mức khởi đầu:

- `appointment` đang gắn trực tiếp với một `service`
- chưa có `employee` riêng đúng nghĩa
- chưa có `appointment_services`
- chưa có `appointment status history`
- chưa có `work schedule / time slot / overlap rule`
- login đã có service nhưng chưa có cửa sổ đăng nhập và chưa seed tài khoản mặc định

Nói ngắn gọn: UI đang đi trước, còn domain model chưa đủ sâu để mang nghiệp vụ salon.

## 3. Cấu trúc hệ thống nên đi theo

Đề xuất chia thành 4 lớp rõ ràng:

### 3.1 Presentation Layer

- `ui.auth`: đăng nhập
- `ui.dashboard`: tổng quan
- `ui.appointment`: lịch hẹn, calendar, check-in
- `ui.customer`: khách hàng
- `ui.employee`: nhân viên
- `ui.service`: dịch vụ
- `ui.payment`: thanh toán
- `ui.report`: báo cáo

### 3.2 Application Layer

Chứa use case nghiệp vụ:

- `AuthService`
- `AppointmentService`
- `SchedulingService`
- `PaymentService`
- `CustomerProfileService`
- `ReportService`
- `NotificationService`

Lớp này xử lý:

- validate quy trình
- kiểm tra trùng lịch
- đổi trạng thái
- tính giá
- điều phối transaction

### 3.3 Domain Layer

Các aggregate/chính thể chính:

- `UserAccount`
- `Employee`
- `Customer`
- `Service`
- `Appointment`
- `AppointmentService`
- `Payment`
- `SmsLog`

Nên bổ sung:

- `AppointmentStatusHistory`
- `EmployeeSchedule`
- `CustomerNote` hoặc `CustomerPreference`

### 3.4 Infrastructure Layer

- JPA repositories
- JDBC/JPA configuration
- SMS provider adapter
- report query adapter

## 4. Luồng nghiệp vụ cốt lõi

### 4.1 Tạo lịch

1. chọn khách hàng
2. chọn nhiều dịch vụ
3. chọn nhân viên
4. tính tổng thời lượng
5. tính `estimated_end_time`
6. kiểm tra trùng lịch nhân viên
7. lưu lịch ở trạng thái `PENDING` hoặc `CONFIRMED`

### 4.2 Check-in và thực hiện

Flow chuẩn:

`PENDING -> CONFIRMED -> CHECKED_IN -> IN_PROGRESS -> COMPLETED -> PAID`

Các nhánh phụ:

- `CANCELLED`
- `NO_SHOW`
- `REFUNDED` là trạng thái thanh toán, không phải trạng thái lịch

### 4.3 Thanh toán

1. lấy toàn bộ dịch vụ của lịch
2. tính `subtotal`
3. áp dụng `discount`
4. ra `final_amount`
5. lưu payment method + payment status

## 5. Thiết kế database đề xuất

## 5.1 users

```text
id PK
username varchar(50) unique not null
password varchar(100) not null
full_name nvarchar(255) not null
phone varchar(20)
email varchar(255)
role varchar(30) not null
is_active bit not null
created_at datetime not null
updated_at datetime
```

Giữ đơn giản theo yêu cầu hiện tại: mật khẩu lưu plain text. Về sau nên băm.

## 5.2 employees

```text
id PK
user_id FK -> users.id null
employee_code varchar(30) unique
full_name nvarchar(255) not null
phone varchar(20)
email varchar(255)
specialization nvarchar(255)
hire_date date
is_active bit not null
created_at datetime not null
```

Tách `employee` khỏi `users` để vẫn quản lý được nhân sự chưa có tài khoản.

## 5.3 customers

```text
id PK
customer_code varchar(30) unique
full_name nvarchar(255) not null
phone varchar(20) not null
email varchar(255)
gender varchar(20)
date_of_birth date
preferred_stylist_id FK -> employees.id null
total_spent decimal(18,2) default 0
last_visit_at datetime null
created_at datetime not null
updated_at datetime
```

## 5.4 services

```text
id PK
service_code varchar(30) unique
name nvarchar(255) not null
description nvarchar(1000)
duration_minutes int not null
price decimal(18,2) not null
category nvarchar(100)
is_active bit not null
created_at datetime not null
updated_at datetime
```

## 5.5 appointments

```text
id PK
appointment_code varchar(30) unique
customer_id FK -> customers.id not null
employee_id FK -> employees.id not null
appointment_time datetime not null
estimated_end_time datetime not null
status varchar(30) not null
check_in_at datetime null
started_at datetime null
completed_at datetime null
cancelled_at datetime null
cancel_reason nvarchar(500) null
note nvarchar(1000) null
created_by FK -> users.id null
created_at datetime not null
updated_at datetime
```

`employee_id` là nhân viên chính phụ trách lịch. Nếu cần nhiều người cùng tham gia thì bổ sung bảng phân công phụ sau.

## 5.6 appointment_services

```text
id PK
appointment_id FK -> appointments.id not null
service_id FK -> services.id not null
line_no int not null
service_name_snapshot nvarchar(255) not null
price decimal(18,2) not null
duration_minutes int not null
assigned_employee_id FK -> employees.id null
status varchar(30) null
note nvarchar(500) null
```

Đây là bảng bắt buộc nếu muốn một lịch có nhiều dịch vụ.

## 5.7 appointment_status_history

```text
id PK
appointment_id FK -> appointments.id not null
from_status varchar(30)
to_status varchar(30) not null
changed_by FK -> users.id null
changed_at datetime not null
note nvarchar(500)
```

Giúp audit quy trình nghiệp vụ.

## 5.8 payments

```text
id PK
appointment_id FK -> appointments.id not null
subtotal decimal(18,2) not null
discount_amount decimal(18,2) not null
final_amount decimal(18,2) not null
payment_method varchar(30) not null
payment_status varchar(30) not null
paid_at datetime null
transaction_ref varchar(100) null
created_by FK -> users.id null
created_at datetime not null
```

## 5.9 sms_logs

```text
id PK
appointment_id FK -> appointments.id null
customer_id FK -> customers.id null
phone varchar(20) not null
message nvarchar(1000) not null
message_type varchar(30) not null
status varchar(30) not null
sent_at datetime null
created_at datetime not null
```

## 5.10 employee_schedules

```text
id PK
employee_id FK -> employees.id not null
work_date date not null
start_time time not null
end_time time not null
is_day_off bit not null
note nvarchar(255)
```

## 6. Ràng buộc nghiệp vụ cần code

### 6.1 Không trùng lịch nhân viên

Điều kiện chặn:

```text
new_start < existing_end
AND new_end > existing_start
AND employee_id trùng nhau
AND status không thuộc CANCELLED, NO_SHOW
```

### 6.2 Không cho nhảy trạng thái sai

Ví dụ:

- không được từ `PENDING` sang `COMPLETED`
- không được `PAID` khi lịch chưa `COMPLETED` trừ khi cho phép thu trước

### 6.3 Snapshot dữ liệu khi đặt lịch

Tên dịch vụ, giá, thời lượng nên copy vào `appointment_services` để sau này đổi bảng giá không làm sai lịch sử cũ.

## 7. Thứ tự triển khai hợp lý

### Giai đoạn 1

- login
- users
- customer CRUD
- service CRUD
- employee CRUD

### Giai đoạn 2

- appointment + multiple services
- overlap validation
- appointment lifecycle

### Giai đoạn 3

- payment
- dashboard
- report
- SMS log

## 8. Kết luận

Hướng đúng cho dự án này là:

- giữ Swing cho desktop
- giữ Spring Boot + JPA cho service/data layer
- lấy `appointment workflow` làm trung tâm
- tách lại entity theo nhiều dịch vụ trên một lịch
- làm login đơn giản trước để có cửa vào hệ thống và phân vai nền tảng
