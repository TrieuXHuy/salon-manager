# Hướng Dẫn Phát Triển Java Swing Client cho Salon Booking

## 📋 Tổng Quan Dự Án

Dự án này cung cấp một **Java Swing Desktop Application** (Swing Client) kết nối với **Spring Boot Backend** để quản lý Salon Booking.

### Cấu Trúc:
- **Frontend**: Java Swing GUI (Desktop Application)
- **Backend**: Spring Boot REST API
- **Database**: SQL Server
- **Theme**: FlatLaf (IntelliJ Theme)

---

## 🎯 Các Thành Phần Chính

### 1. **ApiClient** (`src/main/java/com/salonnbooking/client/ApiClient.java`)

**Mục đích**: Đóng vai trò là HTTP Client để gọi REST API của Spring Boot Backend.

**Phương thức chính**:
```java
// Customer API
- getAllCustomers()
- getCustomer(Integer id)
- createCustomer(CustomerRequests.Create)
- updateCustomer(Integer id, CustomerRequests.Update)
- deleteCustomer(Integer id)

// Appointment API
- getAllAppointments()
- getAppointment(Integer id)
- createAppointment(AppointmentRequests.Create)
- updateAppointment(Integer id, AppointmentRequests.Update)
- deleteAppointment(Integer id)

// Service API
- getAllServices()
- getActiveServices()
- getService(Integer id)
- createService(ServiceRequests.Create)
- updateService(Integer id, ServiceRequests.Update)
- deleteService(Integer id)
```

**Đặc điểm**:
- Sử dụng `java.net.http.HttpClient` (Java 11+)
- Xử lý JSON với **Gson**
- Custom deserializer cho `LocalDateTime`

---

### 2. **MainDashboard** (`src/main/java/com/salonnbooking/ui/MainDashboard.java`)

**Mục đích**: Khung sườn chính của ứng dụng.

**Bố cục (BorderLayout)**:
- **WEST**: Sidebar Navigation (Menu buttons)
- **CENTER**: Content Area (CardLayout - chuyển đổi giữa các panel)

**Menu Items**:
- Dashboard
- Customers
- Appointments
- Services
- Reports
- Logout

**Đặc điểm**:
- Sử dụng `FlatLaf` (IntelliJ Dark Purple Theme)
- `CardLayout` để quản lý chuyển đổi panel
- Sidebar với navigation buttons

---

### 3. **CustomerPanel** (`src/main/java/com/salonnbooking/ui/panel/CustomerPanel.java`)

**Mục đích**: Quản lý danh sách khách hàng (CRUD operations).

**Giao diện**:
```
┌─────────────────────────────────────────┐
│ Customer Management                     │
├─────────────────────────────────────────┤
│ ┌─ Customer Information ─────────────┐ │
│ │ Full Name: [________] Phone: [____]│ │
│ │ Email: [____________] Gender: [___]│ │
│ │ [Add] [Update] [Delete] [Clear]    │ │
│ └────────────────────────────────────┘ │
│ ┌─ Customer List ────────────────────┐ │
│ │ ID │ Name │ Phone │ Email │ Gender │ │
│ │ 1  │ John │ 123.. │ john@ │ Male   │ │
│ │ 2  │ Jane │ 456.. │ jane@ │ Female │ │
│ └────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

**Các tính năng**:
- Hiển thị danh sách khách hàng trong `JTable`
- Form input: Full Name, Phone, Email, Gender
- CRUD Operations: Thêm, Sửa, Xóa, Làm mới
- Validation form
- Sử dụng `SwingWorker` để không block UI khi gọi API

**Xử lý sự kiện**:
1. Click row trong table → Load form tương ứng
2. Click "Add" → Gọi `ApiClient.createCustomer()`
3. Click "Update" → Gọi `ApiClient.updateCustomer()`
4. Click "Delete" → Xác nhận → Gọi `ApiClient.deleteCustomer()`

---

### 4. **AppointmentPanel** (`src/main/java/com/salonnbooking/ui/panel/AppointmentPanel.java`)

**Mục đích**: Quản lý lịch hẹn của salon.

**Giao diện**:
```
┌────────────────────────────────────────┐
│ Appointment Booking                    │
├────────────────────────────────────────┤
│ ┌─ Appointment Information ────────┐  │
│ │ Customer: [Customer Dropdown]    │  │
│ │ Service: [Service Dropdown]      │  │
│ │ Date/Time: [2024-12-31 10:00]    │  │
│ │ Status: [CONFIRMED/PENDING/...]  │  │
│ │ Notes: [________________________] │  │
│ │ [Add] [Update] [Delete] [Clear]  │  │
│ └─────────────────────────────────┘  │
│ ┌─ Appointment List ──────────────┐  │
│ │ ID │ Customer │ Service │ Time │  │
│ └─────────────────────────────────┘  │
└────────────────────────────────────────┘
```

**Các tính năng**:
- ComboBox để chọn Customer (populated từ API)
- ComboBox để chọn Service (populated từ API)
- JTextField cho appointment date/time (format: yyyy-MM-dd HH:mm)
- ComboBox cho Status (CONFIRMED, PENDING, CANCELLED, COMPLETED)
- JTextArea cho ghi chú
- JTable hiển thị danh sách appointments
- CRUD operations với SwingWorker

**Xử lý LocalDateTime**:
```java
DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
LocalDateTime appointmentTime = LocalDateTime.parse(tfAppointmentDateTime.getText(), DATE_FORMATTER);
```

---

### 5. **ServicePanel** (`src/main/java/com/salonnbooking/ui/panel/ServicePanel.java`)

**Mục đích**: Quản lý các dịch vụ của salon.

**Giao diện**: Tương tự CustomerPanel nhưng với thêm các field:
- Service Name
- Price (VND)
- Duration (minutes)
- Description
- Is Active (checkbox)

---

### 6. **SwingClient** (`src/main/java/com/salonnbooking/SwingClient.java`)

**Mục đích**: Entry point của ứng dụng.

**Quy trình khởi động**:
1. Setup FlatLaf theme
2. Tạo MainDashboard
3. Tạo các panel (CustomerPanel, AppointmentPanel, ServicePanel)
4. Thêm panel vào dashboard
5. Hiển thị dashboard

---

## 🛠️ Công Nghệ Sử Dụng

### Dependencies:
```xml
<!-- Swing GUI -->
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf</artifactId>
    <version>3.4.1</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>

<!-- HTTP Client (Java 11+) -->
<!-- Built-in java.net.http.HttpClient -->
```

### Thư viện Java:
- `javax.swing.*` - UI components
- `java.awt.*` - Layout managers
- `java.net.http.*` - HTTP communication
- `java.time.*` - Date/Time handling
- `com.google.gson.*` - JSON serialization

---

## 📝 Hướng Dẫn Sử Dụng

### Chuẩn bị:

1. **Cấu hình Backend URL**:
   - Mở `ApiClient.java`
   - Đổi `BASE_URL` nếu cần (default: `http://localhost:8080/api`)

2. **Chạy Spring Boot Backend**:
   ```bash
   mvn spring-boot:run
   ```

3. **Chạy Swing Client**:
   ```bash
   mvn clean compile
   java -cp target/classes com.salonnbooking.SwingClient
   ```

### Sử dụng ứng dụng:

#### Quản lý Khách hàng:
1. Click "Customers" trong sidebar
2. Nhập thông tin (Tên, SĐT, Email, Giới tính)
3. Click "Add" để thêm khách hàng mới
4. Click row để chọn khách hàng
5. Sửa form rồi click "Update" để cập nhật
6. Click "Delete" để xóa (có xác nhận)

#### Đặt lịch hẹn:
1. Click "Appointments" trong sidebar
2. Chọn khách hàng từ dropdown
3. Chọn dịch vụ từ dropdown
4. Nhập ngày giờ (format: `2024-12-31 14:30`)
5. Chọn trạng thái
6. (Tùy chọn) Thêm ghi chú
7. Click "Add" để đặt lịch

#### Quản lý dịch vụ:
1. Click "Services" trong sidebar
2. Nhập thông tin dịch vụ
3. Sử dụng CRUD operations tương tự

---

## 🔌 Kết nối API

### Kiến trúc Request/Response:

#### Example: Thêm khách hàng
```
Client (Swing)
    ↓
ApiClient.createCustomer(CustomerRequests.Create)
    ↓ HTTP POST http://localhost:8080/api/customers
    ↓
CustomerController.create()
    ↓
CustomerService.save()
    ↓
CustomerRepository.save() → Database
    ↓ HTTP 201 + CustomerRequests.Response
    ↓
ApiClient → JOptionPane.showMessage("Success!")
    ↓
loadCustomers() → Làm mới table
```

### Error Handling:

**Exception Handling Pattern**:
```java
SwingWorker<Result, Void> worker = new SwingWorker<>() {
    @Override
    protected Result doInBackground() throws Exception {
        return ApiClient.method(); // Có thể throw exception
    }

    @Override
    protected void done() {
        try {
            Result result = get(); // Re-throw exception nếu có
            // Handle success
        } catch (Exception e) {
            JOptionPane.showMessageDialog(panel,
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
};
worker.execute();
```

---

## 🎨 Giao diện & Theme

### FlatLaf Theme:
- **IntelliJ Dark Purple Theme** - Modern, professional look
- Tự động customize các Swing components

### Thiết kế:
- **BorderLayout**: Sidebar + Content Area
- **CardLayout**: Chuyển đổi panel mượt mà
- **GridBagLayout**: Form layout linh hoạt

---

## 🚀 Mở rộng ứng dụng

### Thêm panel mới (ví dụ: ReportPanel):

1. **Tạo file mới**: `src/main/java/com/salonnbooking/ui/panel/ReportPanel.java`
   ```java
   public class ReportPanel extends JPanel {
       // Implementation...
   }
   ```

2. **Thêm vào MainDashboard**:
   ```java
   public static final String PANEL_REPORT = "report";
   ```

3. **Thêm Button vào Sidebar**:
   ```java
   String[] buttonLabels = { "...", "Reports" };
   String[] panelNames = { "...", PANEL_REPORT };
   ```

4. **Khởi tạo trong SwingClient**:
   ```java
   ReportPanel reportPanel = new ReportPanel();
   dashboard.addPanel(MainDashboard.PANEL_REPORT, reportPanel);
   ```

---

## 📌 Best Practices

### 1. **Threading**:
- ✅ Sử dụng `SwingWorker` cho tất cả API calls
- ❌ Không gọi API trực tiếp trên EDT (Event Dispatch Thread)

### 2. **Error Handling**:
- ✅ Luôn có try-catch và thông báo lỗi cho user
- ❌ Không để exception lặng im

### 3. **Validation**:
- ✅ Validate form trước khi gửi request
- ❌ Không tin vào dữ liệu từ user

### 4. **UI Responsiveness**:
- ✅ Disable button khi đang xử lý
- ✅ Hiển thị progress indicator
- ❌ Không block UI khi gọi API

---

## 📚 Tài liệu Tham Khảo

- [Java Swing Official Docs](https://docs.oracle.com/javase/tutorial/uiswing/)
- [FlatLaf Documentation](https://www.formdev.com/flatlaf/)
- [Gson Documentation](https://github.com/google/gson/blob/master/README.md)
- [Spring Boot REST API](https://spring.io/guides/gs/rest-service/)

---

## ❓ Troubleshooting

### Problem: "Connection refused" khi gọi API
**Solution**: Đảm bảo Spring Boot backend đang chạy trên `http://localhost:8080`

### Problem: "LocalDateTime parse exception"
**Solution**: Đảm bảo format ngày giờ là `yyyy-MM-dd HH:mm` (ví dụ: `2024-12-31 14:30`)

### Problem: ComboBox không hiển thị item
**Solution**: Đảm bảo `loadInitialData()` đã hoàn thành trước khi sử dụng ComboBox

### Problem: Table không refresh
**Solution**: Kiểm tra `refreshTable()` có được gọi trong `done()` của SwingWorker

---

## 📝 License & Author

Tạo bởi: AI Assistant (GitHub Copilot)
Dự án: Salon Booking System
Ngày: Tháng 5, 2026
