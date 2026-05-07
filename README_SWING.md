# 🏖️ Salon Booking System - Complete Guide

Hệ thống quản lý salon hoàn chỉnh với **Spring Boot Backend** + **Java Swing Desktop Client**.

---

## 📦 Cấu Trúc Dự Án

```
salon-booking/
├── 🎨 UI Layer (Java Swing Desktop)
│   ├── SwingClient.java ........................ Entry point
│   ├── ui/MainDashboard.java .................. Khung sườn
│   └── ui/panel/
│       ├── CustomerPanel.java ................. Quản lý khách hàng
│       ├── AppointmentPanel.java .............. Quản lý lịch hẹn
│       └── ServicePanel.java .................. Quản lý dịch vụ
│
├── 🔌 API Client Layer
│   └── client/ApiClient.java .................. HTTP Client
│
├── 🌐 REST API Layer (Spring Boot)
│   ├── api/
│   │   ├── CustomerController.java
│   │   ├── AppointmentController.java
│   │   ├── ServiceController.java
│   │   ├── PaymentController.java
│   │   ├── DashboardController.java
│   │   ├── ReportController.java
│   │   ├── AnalyticsController.java
│   │   ├── SmsLogController.java
│   │   ├── ScheduleController.java
│   │   └── SystemController.java
│   └── dto/ ..................................... Request/Response classes
│
├── 💼 Business Logic Layer
│   └── service/
│       ├── CustomerService.java
│       ├── AppointmentService.java
│       ├── ServiceService.java
│       ├── PaymentService.java
│       ├── AnalyticsService.java
│       ├── DashboardService.java
│       ├── ReportService.java
│       ├── ScheduleService.java
│       └── SmsLogService.java
│
├── 🗄️ Data Layer
│   ├── domain/ .................................... Entity classes
│   └── repository/ ................................ Spring Data JPA
│
├── 📚 Documentation
│   ├── README.md .................................. (This file)
│   ├── README_API.md .............................. API Documentation
│   ├── SWING_IMPLEMENTATION.md ................... Swing Details
│   ├── SWING_PROMPTS.md .......................... AI Prompts Used
│   ├── SWING_QUICKSTART.md ....................... Quick Start
│   └── HELP.md ................................... Additional Help
│
└── pom.xml ....................................... Maven configuration

```

---

## 🚀 Khởi Động Nhanh

### Bước 1: Chạy Backend (Spring Boot)
```bash
cd d:\salonn-booking
mvn spring-boot:run
# Backend sẵn sàng tại: http://localhost:8080
```

### Bước 2: Chạy Frontend (Swing Client)
```bash
# Từ IDE: Right-click SwingClient.java → Run
# Hoặc terminal:
mvn clean compile
java -cp target/classes com.salonnbooking.SwingClient
```

### Bước 3: Sử Dụng
1. Click "Customers" → Thêm khách hàng
2. Click "Appointments" → Đặt lịch hẹn
3. Click "Services" → Quản lý dịch vụ

👉 **Chi tiết**: [SWING_QUICKSTART.md](./SWING_QUICKSTART.md)

---

## 🎯 Tính Năng

### 1. **Quản Lý Khách Hàng** (Customer)
- ✅ Thêm khách hàng mới
- ✅ Xem danh sách khách hàng
- ✅ Cập nhật thông tin
- ✅ Xóa khách hàng
- ✅ Lọc theo tên, SĐT

**REST API**:
```
GET    /api/customers           - Lấy tất cả khách hàng
GET    /api/customers/{id}      - Lấy 1 khách hàng
POST   /api/customers           - Tạo khách hàng mới
PUT    /api/customers/{id}      - Cập nhật khách hàng
DELETE /api/customers/{id}      - Xóa khách hàng
```

### 2. **Quản Lý Lịch Hẹn** (Appointment)
- ✅ Đặt lịch hẹn mới
- ✅ Chọn khách hàng & dịch vụ
- ✅ Đặt thời gian
- ✅ Theo dõi trạng thái (Pending, Confirmed, Completed, Cancelled)
- ✅ Thêm ghi chú

**REST API**:
```
GET    /api/appointments        - Lấy tất cả lịch hẹn
GET    /api/appointments/{id}   - Lấy 1 lịch hẹn
POST   /api/appointments        - Tạo lịch hẹn mới
PUT    /api/appointments/{id}   - Cập nhật lịch hẹn
DELETE /api/appointments/{id}   - Xóa lịch hẹn
```

### 3. **Quản Lý Dịch Vụ** (Service)
- ✅ Thêm dịch vụ mới (Hair Cut, Manicure, etc.)
- ✅ Đặt giá dịch vụ
- ✅ Xác định thời gian (duration)
- ✅ Bắt tắt/bật dịch vụ

**REST API**:
```
GET    /api/services           - Lấy tất cả dịch vụ
GET    /api/services/active    - Lấy dịch vụ hoạt động
GET    /api/services/{id}      - Lấy 1 dịch vụ
POST   /api/services           - Tạo dịch vụ mới
PUT    /api/services/{id}      - Cập nhật dịch vụ
DELETE /api/services/{id}      - Xóa dịch vụ
```

### 4. **Quản Lý Thanh Toán** (Payment)
- ✅ Ghi nhận thanh toán
- ✅ Theo dõi trạng thái (Pending, Paid, Cancelled)
- ✅ Chọn phương thức thanh toán (Cash, Card, Bank Transfer)

### 5. **Dashboard & Analytics**
- ✅ Thống kê khách hàng
- ✅ Doanh thu theo tháng
- ✅ Lịch hẹn sắp tới
- ✅ Báo cáo hiệu suất

### 6. **Xử Lý Lỗi & Validation**
- ✅ Global Exception Handler
- ✅ Resource Not Found (404)
- ✅ Input Validation (constraints)
- ✅ Friendly error messages

---

## 🛠️ Technology Stack

### Frontend (Desktop)
| Công nghệ | Phiên bản | Mục đích |
|-----------|----------|---------|
| **Java** | 17+ | Ngôn ngữ lập trình |
| **Swing** | Built-in | GUI Framework |
| **FlatLaf** | 3.4.1 | Modern UI Theme (IntelliJ) |
| **Gson** | 2.10.1 | JSON Processing |
| **Maven** | 3.6+ | Build Tool |

### Backend
| Công nghệ | Phiên bản | Mục đích |
|-----------|----------|---------|
| **Spring Boot** | 3.5.14 | Framework |
| **Spring Data JPA** | - | ORM |
| **SQL Server** | - | Database |
| **Lombok** | Latest | Boilerplate reduction |
| **Validation** | Jakarta | Input validation |
| **Swagger** | 2.0.2 | API Documentation |

---

## 📖 Hướng Dẫn Chi Tiết

### For Desktop Users (Swing Client)
👉 [SWING_QUICKSTART.md](./SWING_QUICKSTART.md) - Bắt đầu nhanh
👉 [SWING_IMPLEMENTATION.md](./SWING_IMPLEMENTATION.md) - Chi tiết kỹ thuật

### For Developers (API & Backend)
👉 [README_API.md](./README_API.md) - REST API Documentation
👉 [SWING_PROMPTS.md](./SWING_PROMPTS.md) - AI Prompts & Code Patterns

### For Extension & Customization
👉 [HELP.md](./HELP.md) - Common issues & solutions

---

## 📊 Database Schema

### Customers Table
```sql
CREATE TABLE customers (
    id INT PRIMARY KEY IDENTITY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    gender VARCHAR(10),
    created_at DATETIME DEFAULT GETDATE()
);
```

### Services Table
```sql
CREATE TABLE services (
    id INT PRIMARY KEY IDENTITY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    description TEXT,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE()
);
```

### Appointments Table
```sql
CREATE TABLE appointments (
    id INT PRIMARY KEY IDENTITY,
    customer_id INT NOT NULL,
    service_id INT NOT NULL,
    appointment_time DATETIME NOT NULL,
    status VARCHAR(20),
    note TEXT,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (service_id) REFERENCES services(id)
);
```

---

## 🔧 Configuration

### Backend Configuration (application.yaml)
```yaml
spring:
  application:
    name: salonn-booking
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=salon_db;encrypt=true;trustServerCertificate=true
    username: sa
    password: YourPassword123
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8080
```

### Frontend Configuration (ApiClient.java)
```java
private static final String BASE_URL = "http://localhost:8080/api";
```

---

## 🧪 Testing

### API Testing with cURL
```bash
# Get all customers
curl http://localhost:8080/api/customers

# Create customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Doe","phone":"0123456789","email":"john@example.com","gender":"MALE"}'
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## 📈 Project Status

| Component | Status | Docs |
|-----------|--------|------|
| Backend API | ✅ Complete | [README_API.md](./README_API.md) |
| Swing Frontend | ✅ Complete | [SWING_IMPLEMENTATION.md](./SWING_IMPLEMENTATION.md) |
| Customer Management | ✅ Complete | - |
| Appointment Booking | ✅ Complete | - |
| Service Management | ✅ Complete | - |
| Payment System | ✅ Complete | - |
| Dashboard/Analytics | 🔄 In Progress | - |
| Reports | 🔄 In Progress | - |

---

## 🎓 Learning Path

1. **Beginner**: [SWING_QUICKSTART.md](./SWING_QUICKSTART.md) - Chạy ứng dụng
2. **Intermediate**: [SWING_IMPLEMENTATION.md](./SWING_IMPLEMENTATION.md) - Hiểu code
3. **Advanced**: [SWING_PROMPTS.md](./SWING_PROMPTS.md) - Mở rộng & customize

---

## 🐛 Troubleshooting

### Backend Issues
- Port 8080 bận → [HELP.md](./HELP.md#port-already-in-use)
- Database connection → [README_API.md](./README_API.md#database-configuration)

### Swing Client Issues  
- Connection refused → [SWING_QUICKSTART.md](./SWING_QUICKSTART.md#-connection-refused--network-error)
- LocalDateTime error → [SWING_QUICKSTART.md](./SWING_QUICKSTART.md#-localdatetime-parse-exception)

### More Help
👉 [HELP.md](./HELP.md) - FAQ & Solutions

---

## 📞 Support & Contribution

- 📧 Email: [your-email@example.com]
- 🐙 GitHub: [your-github-repo]
- 📝 Issues: [GitHub Issues](https://github.com/...)

---

## 📄 License

Copyright © 2024 Salon Booking System. All rights reserved.

---

## 👨‍💻 Developer Notes

### File Organization
- **src/main/java** - Application code
- **src/main/resources** - Configuration & static files
- **src/test** - Unit & integration tests
- **target** - Compiled output
- **pom.xml** - Maven dependencies

### Key Classes
| Class | Purpose |
|-------|---------|
| `SwingClient.java` | Application entry point |
| `MainDashboard.java` | Main window frame |
| `ApiClient.java` | HTTP communication |
| `*Controller.java` | REST endpoints |
| `*Service.java` | Business logic |
| `*Repository.java` | Data access |

---

## 🎉 Quick Links

- 🚀 [Quick Start](./SWING_QUICKSTART.md)
- 📚 [Full Documentation](./SWING_IMPLEMENTATION.md)
- 🔌 [API Docs](./README_API.md)
- 💡 [AI Prompts](./SWING_PROMPTS.md)
- ❓ [FAQ & Help](./HELP.md)

---

**Last Updated**: May 2026  
**Version**: 1.0.0  
**Status**: Stable ✅

Created with ❤️ by GitHub Copilot
