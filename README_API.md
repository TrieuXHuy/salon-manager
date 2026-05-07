# Salon Booking System - REST API

Hệ thống quản lý đặt lịch salon, dịch vụ và thanh toán hoàn chỉnh được xây dựng với Spring Boot và SQL Server.

## 🚀 Tính Năng

- ✅ **Quản lý khách hàng** (Customer): CRUD đầy đủ
- ✅ **Quản lý lịch hẹn** (Appointment): Đặt, sửa, hủy lịch
- ✅ **Quản lý dịch vụ** (Service): Quản lý dịch vụ salon
- ✅ **Quản lý thanh toán** (Payment): Xử lý thanh toán
- ✅ **Ghi log SMS** (SMS Log): Ghi nhận tin nhắn
- ✅ **Xử lý lỗi toàn cầu** (Global Exception Handler)
- ✅ **API Documentation** với Swagger UI
- ✅ **Validation** đầu vào

## 📋 Yêu Cầu Hệ Thống

- Java 17+
- SQL Server
- Maven 3.6+

## ⚙️ Cấu Hình

### 1. Database Configuration

Chỉnh sửa file `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=booking_system;encrypt=true;trustServerCertificate=true
    username: sa
    password: YourStrong@Pass123
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: update
```

### 2. Build & Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Or java -jar
java -jar target/salonn-booking-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

### Swagger UI

Truy cập: `http://localhost:8080/swagger-ui.html`

---

## 🔌 API Endpoints

### 👥 Customers (Khách Hàng)

#### Lấy danh sách khách hàng
```http
GET /api/customers
```

**Response:**
```json
[
  {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "email": "a@example.com",
    "gender": "male"
  }
]
```

#### Lấy khách hàng theo ID
```http
GET /api/customers/{id}
```

#### Tạo khách hàng mới
```http
POST /api/customers
Content-Type: application/json

{
  "fullName": "Trần Thị B",
  "phone": "0987654321",
  "email": "b@example.com",
  "gender": "female"
}
```

#### Cập nhật khách hàng
```http
PUT /api/customers/{id}
Content-Type: application/json

{
  "fullName": "Trần Thị B Updated",
  "phone": "0987654321",
  "email": "b.updated@example.com",
  "gender": "female"
}
```

#### Xóa khách hàng
```http
DELETE /api/customers/{id}
```

---

### 💇 Services (Dịch Vụ)

#### Lấy tất cả dịch vụ
```http
GET /api/services
```

#### Lấy dịch vụ hoạt động
```http
GET /api/services/active
```

#### Lấy dịch vụ theo ID
```http
GET /api/services/{id}
```

**Response:**
```json
{
  "id": 1,
  "name": "Cắt tóc nữ",
  "price": 150000,
  "durationMinutes": 45,
  "description": "Cắt tóc chuyên nghiệp cho nữ",
  "isActive": true
}
```

#### Tạo dịch vụ mới
```http
POST /api/services
Content-Type: application/json

{
  "name": "Nhuộm tóc",
  "price": 250000,
  "durationMinutes": 90,
  "description": "Nhuộm tóc cao cấp",
  "isActive": true
}
```

#### Cập nhật dịch vụ
```http
PUT /api/services/{id}
Content-Type: application/json

{
  "name": "Nhuộm tóc premium",
  "price": 300000,
  "durationMinutes": 120,
  "description": "Nhuộm tóc cao cấp premium",
  "isActive": true
}
```

#### Xóa dịch vụ
```http
DELETE /api/services/{id}
```

---

### 📅 Appointments (Lịch Hẹn)

#### Lấy tất cả lịch hẹn
```http
GET /api/appointments
```

#### Lấy lịch hẹn theo ID
```http
GET /api/appointments/{id}
```

**Response:**
```json
{
  "id": 1,
  "customerId": 1,
  "serviceId": 1,
  "appointmentTime": "2026-05-10T14:00:00",
  "status": "pending",
  "note": "Khách hàng yêu cầu thêm phục vụ"
}
```

#### Tạo lịch hẹn mới
```http
POST /api/appointments
Content-Type: application/json

{
  "customerId": 1,
  "serviceId": 1,
  "appointmentTime": "2026-05-12T10:00:00",
  "status": "pending",
  "note": "Khách hàng VIP"
}
```

**Status Values:**
- `pending` - Chờ xác nhận
- `confirmed` - Đã xác nhận
- `completed` - Đã hoàn thành
- `cancelled` - Đã hủy

#### Cập nhật lịch hẹn
```http
PUT /api/appointments/{id}
Content-Type: application/json

{
  "customerId": 1,
  "serviceId": 2,
  "appointmentTime": "2026-05-12T11:00:00",
  "status": "confirmed",
  "note": "Đã xác nhận với khách hàng"
}
```

#### Xóa lịch hẹn
```http
DELETE /api/appointments/{id}
```

---

### 💰 Payments (Thanh Toán)

#### Lấy tất cả thanh toán
```http
GET /api/payments
```

#### Lấy thanh toán theo ID
```http
GET /api/payments/{id}
```

**Response:**
```json
{
  "id": 1,
  "appointmentId": 1,
  "amount": 150000,
  "paymentMethod": "cash",
  "paymentStatus": "unpaid",
  "paidAt": null
}
```

#### Tạo thanh toán mới
```http
POST /api/payments
Content-Type: application/json

{
  "appointmentId": 1,
  "amount": 150000,
  "paymentMethod": "cash",
  "paymentStatus": "unpaid",
  "paidAt": null
}
```

**Payment Methods:**
- `cash` - Tiền mặt
- `bank_transfer` - Chuyển khoản ngân hàng
- `momo` - Momo
- `card` - Thẻ tín dụng

**Payment Status:**
- `unpaid` - Chưa thanh toán
- `paid` - Đã thanh toán
- `refunded` - Đã hoàn tiền

#### Cập nhật thanh toán
```http
PUT /api/payments/{id}
Content-Type: application/json

{
  "appointmentId": 1,
  "amount": 150000,
  "paymentMethod": "bank_transfer",
  "paymentStatus": "paid",
  "paidAt": "2026-05-10T14:30:00"
}
```

#### Đánh dấu thanh toán đã hoàn tất
```http
POST /api/payments/{id}/mark-paid
```

#### Xóa thanh toán
```http
DELETE /api/payments/{id}
```

---

### 📱 SMS Logs

#### Lấy tất cả SMS logs
```http
GET /api/sms-logs
```

#### Lấy SMS log theo ID
```http
GET /api/sms-logs/{id}
```

**Response:**
```json
{
  "id": 1,
  "appointmentId": 1,
  "phone": "0901234567",
  "message": "Xác nhận lịch hẹn cắt tóc vào 14:00",
  "status": "success"
}
```

#### Tạo SMS log mới
```http
POST /api/sms-logs
Content-Type: application/json

{
  "appointmentId": 1,
  "phone": "0901234567",
  "message": "Lời nhắc: Bạn có lịch hẹn hôm nay lúc 14:00",
  "status": "success"
}
```

**Status:**
- `success` - Gửi thành công
- `failed` - Gửi thất bại

#### Cập nhật SMS log
```http
PUT /api/sms-logs/{id}
Content-Type: application/json

{
  "appointmentId": 1,
  "phone": "0901234567",
  "message": "Cập nhật tin nhắn",
  "status": "failed"
}
```

#### Xóa SMS log
```http
DELETE /api/sms-logs/{id}
```

---

## 🔍 Error Handling

Ứng dụng có xử lý lỗi toàn cầu:

**404 Not Found:**
```json
{
  "timestamp": "2026-05-07T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 99",
  "path": "uri=/api/customers/99"
}
```

**400 Bad Request (Validation Error):**
```json
{
  "timestamp": "2026-05-07T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid input parameters",
  "path": "uri=/api/customers"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2026-05-07T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Error details...",
  "path": "uri=/api/customers"
}
```

---

## 📊 Data Models

### Customer
```java
{
  id: Integer,
  fullName: String,
  phone: String (unique, max 20 chars),
  email: String,
  gender: Enum (male, female, other),
  createdAt: LocalDateTime
}
```

### ServiceEntity
```java
{
  id: Integer,
  name: String,
  price: BigDecimal,
  durationMinutes: Integer,
  description: String,
  isActive: Boolean
}
```

### Appointment
```java
{
  id: Integer,
  customer: Customer,
  service: ServiceEntity,
  appointmentTime: LocalDateTime,
  status: Enum (pending, confirmed, completed, cancelled),
  note: String,
  createdAt: LocalDateTime
}
```

### Payment
```java
{
  id: Integer,
  appointment: Appointment,
  amount: BigDecimal,
  paymentMethod: Enum (cash, bank_transfer, momo, card),
  paymentStatus: Enum (unpaid, paid, refunded),
  paidAt: LocalDateTime
}
```

### SmsLog
```java
{
  id: Integer,
  appointment: Appointment,
  phone: String,
  message: String,
  status: Enum (success, failed),
  sentAt: LocalDateTime
}
```

---

## 🏗️ Project Structure

```
src/main/java/com/salonnbooking/
├── api/
│   ├── AppointmentController.java
│   ├── CustomerController.java
│   ├── PaymentController.java
│   ├── ServiceController.java
│   ├── SmsLogController.java
│   └── dto/
│       ├── AppointmentRequests.java
│       ├── CustomerRequests.java
│       ├── PaymentRequests.java
│       ├── ServiceRequests.java
│       └── SmsLogRequests.java
├── config/
│   └── OpenAPIConfig.java
├── domain/
│   ├── Appointment.java
│   ├── AppointmentStatus.java
│   ├── Customer.java
│   ├── Gender.java
│   ├── Payment.java
│   ├── PaymentMethod.java
│   ├── PaymentStatus.java
│   ├── ServiceEntity.java
│   ├── SmsLog.java
│   └── SmsStatus.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── AppointmentRepository.java
│   ├── CustomerRepository.java
│   ├── PaymentRepository.java
│   ├── ServiceRepository.java
│   └── SmsLogRepository.java
├── service/
│   ├── AppointmentService.java
│   ├── CustomerService.java
│   ├── PaymentService.java
│   ├── ServiceService.java
│   └── SmsLogService.java
└── SalonnBookingApplication.java
```

---

## 📝 Sample Request/Response

### Tạo khách hàng và lịch hẹn

**1. Tạo khách hàng:**
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "email": "a@example.com",
    "gender": "male"
  }'
```

**Response:**
```json
{
  "id": 1,
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "email": "a@example.com",
  "gender": "male"
}
```

**2. Tạo dịch vụ:**
```bash
curl -X POST http://localhost:8080/api/services \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cắt tóc nữ",
    "price": 150000,
    "durationMinutes": 45,
    "description": "Cắt tóc chuyên nghiệp",
    "isActive": true
  }'
```

**3. Tạo lịch hẹn:**
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "serviceId": 1,
    "appointmentTime": "2026-05-12T10:00:00",
    "status": "pending",
    "note": "Khách hàng VIP"
  }'
```

**4. Tạo thanh toán:**
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "amount": 150000,
    "paymentMethod": "cash",
    "paymentStatus": "unpaid"
  }'
```

---

## 🔒 Validation Rules

- **fullName**: Bắt buộc, tối đa 100 ký tự
- **phone**: Bắt buộc, duy nhất, tối đa 20 ký tự
- **email**: Email hợp lệ
- **appointmentTime**: Bắt buộc, định dạng ISO 8601
- **amount**: Bắt buộc, phải lớn hơn 0

---

## 📚 Công Nghệ Sử Dụng

- **Spring Boot 3.5.14**
- **Spring Data JPA**
- **Hibernate ORM**
- **SQL Server**
- **Springdoc OpenAPI (Swagger UI)**
- **Jakarta Bean Validation**

---

## 👨‍💼 Hỗ Trợ

Nếu bạn có câu hỏi hoặc vấn đề, vui lòng liên hệ: support@salonn-booking.com

---

## 📄 License

Apache License 2.0
