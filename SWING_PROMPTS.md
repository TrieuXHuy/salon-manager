# Các AI Prompts cho Phát Triển Java Swing Client

Tài liệu này ghi lại các prompt được sử dụng để tạo ra Swing Client cho dự án Salon Booking.

---

## 📌 Prompt 1: Thiết Kế Cấu Trúc Tổng Thể (Layout & Navigation)

### Prompt gốc:
```
Tôi đang phát triển ứng dụng Java Swing cho dự án quản lý Salon (Spring Boot backend). 
Hãy đóng vai Senior Java Developer, viết code cho lớp MainDashboard.java. Yêu cầu:

Sử dụng FlatLaf (IntelliJ Theme) để giao diện hiện đại.

Bố cục dùng BorderLayout: 
- Phía trái là Sidebar (cấp các nút: Dashboard, Appointment, Customer, Service, Report), 
- chính giữa là JPanel dùng CardLayout để chuyển đổi giữa các màn hình.

Viết code sạch, tách biệt logic điều hướng và giao diện.
```

### Kết quả thực hiện:
✅ **MainDashboard.java** - Khung sườn chính
- BorderLayout: WEST (Sidebar) + CENTER (CardLayout ContentPanel)
- FlatLaf IntelliJ Dark Purple Theme
- Navigation buttons: Dashboard, Customers, Appointments, Services, Reports
- CardLayout để chuyển đổi panel mượt mà
- Logout button

**File**: `src/main/java/com/salonnbooking/ui/MainDashboard.java`

---

## 📌 Prompt 2: Màn Hình Nghiệp Vụ (CRUD & Table)

### Prompt gốc:
```
Viết code cho CustomerPanel.java trong Swing để quản lý khách hàng. 
Dựa vào cấu trúc DTO CustomerRequests trong mã nguồn của tôi:

Sử dụng JTable để hiển thị danh sách (ID, Name, Phone, Email, Gender).

Có các ô JTextField và JButton (Thêm, Sửa, Xóa, Làm mới) ở phía trên/dưới Table.

Tích hợp DefaultTableModel. Hãy viết các phương thức mapping dữ liệu từ List DTO vào Table.

Giao diện cần đồng bộ với FlatLaf.
```

### Kết quả thực hiện:
✅ **CustomerPanel.java** - Quản lý khách hàng
- Form input: Full Name, Phone, Email, Gender
- JTable với DefaultTableModel
- CRUD Buttons: Add, Update, Delete, Clear
- Row selection → Auto-load form
- SwingWorker để gọi API không block UI
- Validation form

**File**: `src/main/java/com/salonnbooking/ui/panel/CustomerPanel.java`

✅ **ServicePanel.java** - Quản lý dịch vụ (mở rộng từ CustomerPanel)
- Service Name, Price, Duration (minutes), Description, Is Active
- Tương tự CRUD pattern

**File**: `src/main/java/com/salonnbooking/ui/panel/ServicePanel.java`

---

## 📌 Prompt 3: Kết Nối Swing với Spring Boot Service

### Prompt gốc:
```
Tôi cần kết nối AppointmentService (Spring) vào giao diện Swing AppointmentPanel. 
Hãy hướng dẫn và viết code mẫu:

1. Cách sử dụng SwingWorker để gọi API từ Service mà không làm treo giao diện (non-blocking UI).

2. Viết hàm loadAppointments() để lấy dữ liệu từ AppointmentRepository thông qua Service 
   và hiển thị lên JTable.

3. Xử lý thông báo lỗi bằng JOptionPane khi gặp ResourceNotFoundException 
   như trong code backend của tôi.
```

### Kết quả thực hiện:
✅ **AppointmentPanel.java** - Quản lý lịch hẹn
- ComboBox cho Customer (populated từ API)
- ComboBox cho Service (populated từ API)
- TextField cho appointment date/time (format: yyyy-MM-dd HH:mm)
- ComboBox cho Status (CONFIRMED, PENDING, CANCELLED, COMPLETED)
- TextArea cho notes
- JTable hiển thị appointments
- SwingWorker pattern cho tất cả API calls
- Error handling với JOptionPane

✅ **ApiClient.java** - HTTP Client tương ứng
- GET: getAllCustomers(), getAllAppointments(), getAllServices()
- POST: createCustomer(), createAppointment(), createService()
- PUT: updateCustomer(), updateAppointment(), updateService()
- DELETE: deleteCustomer(), deleteAppointment(), deleteService()
- Custom Gson deserializer cho LocalDateTime
- Error handling với HTTP status codes

**Files**: 
- `src/main/java/com/salonnbooking/ui/panel/AppointmentPanel.java`
- `src/main/java/com/salonnbooking/client/ApiClient.java`

---

## 🎯 Pattern & Best Practices Được Implement

### 1. **SwingWorker Pattern** (Async API Calls)
```java
SwingWorker<List<CustomerRequests.Response>, Void> worker = new SwingWorker<>() {
    @Override
    protected List<CustomerRequests.Response> doInBackground() throws Exception {
        return ApiClient.getAllCustomers(); // Gọi API trên background thread
    }

    @Override
    protected void done() {
        try {
            List<CustomerRequests.Response> customers = get();
            refreshTable(customers); // Update UI trên EDT
        } catch (Exception e) {
            JOptionPane.showMessageDialog(CustomerPanel.this,
                "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
};
worker.execute();
```

### 2. **GridBagLayout Pattern** (Form Layout)
```java
GridBagConstraints gbc = new GridBagConstraints();
gbc.insets = new Insets(5, 5, 5, 5);
gbc.fill = GridBagConstraints.HORIZONTAL;

// Add label + textfield
addLabel(panel, "Full Name:", 0, 0, gbc);
tfFullName = new JTextField(20);
panel.add(tfFullName, setPosition(gbc, 1, 0));
```

### 3. **Validation Pattern**
```java
private boolean validateForm() {
    if (tfFullName.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Full Name is required", "Validation Error",
            JOptionPane.WARNING_MESSAGE);
        return false;
    }
    // More validations...
    return true;
}
```

### 4. **ComboBox with Custom Objects**
```java
private static class ComboBoxCustomer {
    private final Integer id;
    private final String name;
    
    public Integer getId() { return id; }
    
    @Override
    public String toString() { return name; }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof ComboBoxCustomer && id.equals(((ComboBoxCustomer)o).id);
    }
}
```

### 5. **LocalDateTime Handling**
```java
DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
LocalDateTime appointmentTime = LocalDateTime.parse(tfAppointmentDateTime.getText(), 
    DATE_FORMATTER);
```

---

## 📁 Cấu Trúc Tệp Dự Án

```
src/main/java/com/salonnbooking/
├── SwingClient.java                          ← Entry point
├── ui/
│   ├── MainDashboard.java                    ← Khung sườn chính
│   └── panel/
│       ├── CustomerPanel.java                ← Quản lý khách hàng
│       ├── AppointmentPanel.java             ← Quản lý lịch hẹn
│       ├── ServicePanel.java                 ← Quản lý dịch vụ
│       └── common/
│           └── (Các component tái sử dụng)
├── client/
│   └── ApiClient.java                        ← HTTP Client cho backend
├── api/
│   ├── *Controller.java                      ← REST Controllers
│   └── dto/
│       ├── CustomerRequests.java
│       ├── AppointmentRequests.java
│       ├── ServiceRequests.java
│       └── ...
├── service/
│   ├── CustomerService.java
│   ├── AppointmentService.java
│   ├── ServiceService.java
│   └── ...
├── domain/
│   ├── Customer.java
│   ├── Appointment.java
│   ├── ServiceEntity.java
│   └── ...
└── repository/
    ├── CustomerRepository.java
    ├── AppointmentRepository.java
    ├── ServiceRepository.java
    └── ...
```

---

## 🚀 Cách Chạy Ứng Dụng

### 1. **Chuẩn bị Dependencies**
```bash
# Cập nhật pom.xml với các dependency:
# - FlatLaf (UI Theme)
# - Gson (JSON Processing)
# - HttpClient (API Communication)
```

### 2. **Chạy Spring Boot Backend**
```bash
cd d:\salonn-booking
mvn spring-boot:run
# Hoặc: mvn clean compile && java -cp target/classes com.salonnbooking.SalonnBookingApplication
```

### 3. **Chạy Swing Client**
```bash
# Từ IDE (IntelliJ, Eclipse, VS Code):
# - Right-click SwingClient.java → Run

# Hoặc từ terminal:
mvn clean compile
java -cp target/classes com.salonnbooking.SwingClient
```

### 4. **Sử dụng Ứng Dụng**
1. Nhấn vào "Customers" trong Sidebar
2. Nhập thông tin khách hàng
3. Click "Add" để thêm mới
4. Click row để sửa hoặc xóa

---

## 🔧 Configuration

### API Base URL
Nếu backend chạy trên port khác, sửa trong `ApiClient.java`:
```java
private static final String BASE_URL = "http://localhost:8080/api"; // Default
// Thay đổi port nếu cần
```

### FlatLaf Theme
Để đổi theme, sửa trong `SwingClient.java`:
```java
FlatDarkPurpleIJTheme.setup(); // Có thể thay bằng theme khác
```

---

## 📋 Checklist Hoàn Thành

- ✅ Prompt 1: MainDashboard với BorderLayout & CardLayout
- ✅ Prompt 2: CustomerPanel với CRUD & JTable
- ✅ Prompt 3: AppointmentPanel với SwingWorker & LocalDateTime
- ✅ ApiClient với tất cả CRUD operations
- ✅ ServicePanel (mở rộng)
- ✅ SwingClient (entry point)
- ✅ FlatLaf theme integration
- ✅ Error handling & validation
- ✅ Tài liệu hóa

---

## 📚 Tài Liệu Liên Quan

- [SWING_IMPLEMENTATION.md](./SWING_IMPLEMENTATION.md) - Hướng dẫn chi tiết
- [pom.xml](./pom.xml) - Maven dependencies
- [Spring Boot API docs](./README_API.md)

---

## ❓ Q&A

**Q: Làm sao để thêm panel mới?**
A: Tạo class mới extends JPanel, implement CRUD logic, thêm vào MainDashboard & SwingClient

**Q: Tại sao dùng SwingWorker?**
A: Để không block Event Dispatch Thread (EDT) khi gọi API mất thời gian

**Q: Cách xử lý exception từ API?**
A: Dùng try-catch trong SwingWorker.done(), hiển thị lỗi với JOptionPane

**Q: LocalDateTime parse exception?**
A: Đảm bảo format là `yyyy-MM-dd HH:mm` (ví dụ: `2024-12-31 14:30`)

---

**Tạo bởi**: AI Assistant (GitHub Copilot)  
**Ngày**: Tháng 5, 2026  
**Dự án**: Salon Booking System - Java Swing Client
