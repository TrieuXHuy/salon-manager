# 🚀 Quick Start: Java Swing Client cho Salon Booking

Hướng dẫn nhanh để chạy ứng dụng Swing Client kết nối với Spring Boot Backend.

---

## ⚡ Khởi Động (3 bước)

### 1️⃣ **Chạy Spring Boot Backend**

```bash
# Terminal 1: Chạy backend
cd d:\salonn-booking
mvn spring-boot:run
```

Chờ message:
```
Started SalonnBookingApplication in X.XXX seconds
```

✅ Backend đã sẵn sàng tại: `http://localhost:8080`

---

### 2️⃣ **Chạy Swing Client**

#### Option A: Từ IDE (IntelliJ IDEA / VS Code)
1. Mở project trong IDE
2. Tìm file: `SwingClient.java`
3. Right-click → **Run** (hoặc Ctrl+Shift+F10)

#### Option B: Từ Terminal
```bash
# Terminal 2: Compile & chạy
cd d:\salonn-booking
mvn clean compile
java -cp target/classes com.salonnbooking.SwingClient
```

✅ Cửa sổ Swing sẽ hiện lên!

---

### 3️⃣ **Sử Dụng Ứng Dụng**

```
┌─────────────────────────────────────┐
│  Salon Booking System               │
├──────────┬──────────────────────────┤
│ Dashboard│ Customer Management      │
│Customers │ ┌──────────────────────┐│
│Appointments
│ │ Full Name: [_____________]││
│Services │ Phone:     [_____________]││
│ Reports │ Email:     [_____________]││
│ Logout  │ Gender:    [Male ▼]      ││
│          │ [Add] [Update] [Delete]  ││
│          │ ┌──────────────────────┐││
│          │ │ ID │ Name │ Phone    │││
│          │ │ 1  │ John │ 123-456 │││
│          │ └──────────────────────┘││
│          └──────────────────────────┘│
└─────────────────────────────────────┘
```

---

## 📝 Các Tác Vụ Cơ Bản

### Thêm Khách Hàng Mới
1. Click **"Customers"** trong sidebar
2. Điền form:
   - Full Name: `John Doe`
   - Phone: `0123456789`
   - Email: `john@example.com`
   - Gender: `Male`
3. Click **"Add"**
4. Thông báo thành công → Table được refresh tự động ✓

### Sửa Khách Hàng
1. Click **"Customers"** 
2. Click 1 hàng trong bảng → Form tự động điền
3. Sửa thông tin
4. Click **"Update"** ✓

### Xóa Khách Hàng
1. Click **"Customers"**
2. Click 1 hàng → Chọn
3. Click **"Delete"**
4. Xác nhận → Khách hàng bị xóa ✓

### Đặt Lịch Hẹn
1. Click **"Appointments"** trong sidebar
2. Chọn **Customer** từ dropdown (tự load từ API)
3. Chọn **Service** từ dropdown
4. Nhập **Appointment Date/Time**: `2024-12-31 14:30`
   - Format: `yyyy-MM-dd HH:mm`
5. Chọn **Status**: `CONFIRMED`
6. (Tùy chọn) Thêm **Notes**
7. Click **"Add"** → Lịch hẹn được tạo ✓

### Quản Lý Dịch Vụ
1. Click **"Services"** trong sidebar
2. Tương tự như Customers:
   - Thêm: Nhập Service Name, Price, Duration → Click "Add"
   - Sửa: Click hàng → Chỉnh sửa → Click "Update"
   - Xóa: Click hàng → Click "Delete" → Xác nhận

---

## 🎯 Có Vấn Đề?

### ❌ Connection Refused / Network error

**Nguyên nhân**: Backend không chạy

**Cách fix**:
```bash
# Check port 8080 có bận không
netstat -ano | findstr :8080

# Kill process (nếu cần)
taskkill /PID <PID> /F

# Chạy lại backend
mvn spring-boot:run
```

---

### ❌ "LocalDateTime parse exception"

**Nguyên nhân**: Format ngày giờ sai

**Cách fix**:
- Đúng: `2024-12-31 14:30`
- Sai: `31/12/2024 2:30 PM` ❌

---

### ❌ ComboBox trống (không có khách hàng/dịch vụ)

**Nguyên nhân**: Data chưa load xong

**Cách fix**:
1. Check backend có chạy không
2. Click **"Refresh"** button
3. Kiểm tra database có dữ liệu không

---

### ❌ Table không update sau khi Add/Update

**Nguyên nhân**: Lỗi API call

**Cách fix**:
1. Check console có error message không
2. Xem backend log có lỗi không
3. Validate form data trước khi submit

---

## 💡 Tips & Tricks

### Tip 1: Xem Network Requests
Mở browser DevTools → Network tab → thực hiện action
```
POST http://localhost:8080/api/customers
Response: 201 Created
```

### Tip 2: Debug trong IDE
1. Set breakpoint tại `ApiClient.java`
2. Run with Debug mode
3. F5 để step through code

### Tip 3: Modify Backend URL
Nếu backend chạy trên port khác:
```java
// ApiClient.java, dòng ~28
private static final String BASE_URL = "http://localhost:9090/api"; // Thay port
```

---

## 📊 Kiến Trúc Tóm Tắt

```
Swing Client (Java)
    ↓
ApiClient (HTTP)
    ↓
Spring Boot REST API
    ↓
Database (SQL Server)
```

**Luồng dữ liệu**:
1. User nhập form trong Swing
2. Click button → Gọi `ApiClient.method()`
3. ApiClient gửi HTTP request → Backend
4. Backend xử lý → trả về response
5. ApiClient parse JSON → return object
6. Swing update UI (table, dialog message)

---

## 🔗 Liên Kết Hữu Ích

- **Hướng dẫn chi tiết**: [SWING_IMPLEMENTATION.md](./SWING_IMPLEMENTATION.md)
- **Tài liệu Prompt**: [SWING_PROMPTS.md](./SWING_PROMPTS.md)
- **Backend API Docs**: [README_API.md](./README_API.md)
- **FlatLaf Theme**: [https://www.formdev.com/flatlaf/](https://www.formdev.com/flatlaf/)

---

## 📋 Checklist Trước Khi Chạy

- ✅ Java 11+ được cài
- ✅ Maven được cài
- ✅ SQL Server backend chạy
- ✅ Spring Boot backend chạy (port 8080)
- ✅ Project compile thành công (`mvn clean compile`)
- ✅ `target/classes` folder tồn tại

---

## 🎓 Tiếp Theo

Sau khi chạy thành công:

1. **Tìm hiểu code**:
   - Đọc `MainDashboard.java` → Hiểu layout
   - Đọc `CustomerPanel.java` → Hiểu CRUD pattern
   - Đọc `ApiClient.java` → Hiểu API communication

2. **Mở rộng ứng dụng**:
   - Thêm Dashboard panel (thống kê)
   - Thêm Report panel (tạo báo cáo)
   - Thêm Settings panel (cấu hình ứng dụng)

3. **Cải thiện giao diện**:
   - Thêm search/filter trong table
   - Thêm export to Excel
   - Thêm print functionality

---

## 📞 Support

Gặp vấn đề? 

1. Kiểm tra **console output** (Backend log)
2. Kiểm tra **IDE console** (Swing error message)
3. Đọc lại hướng dẫn trong [SWING_IMPLEMENTATION.md](./SWING_IMPLEMENTATION.md)
4. Check [Troubleshooting section](./SWING_IMPLEMENTATION.md#-troubleshooting)

---

**Happy Coding! 🎉**

*Tạo bởi: AI Assistant (GitHub Copilot)*  
*Tháng 5, 2026 - Salon Booking System*
