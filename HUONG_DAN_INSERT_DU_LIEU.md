# Hướng Dẫn Insert Dữ Liệu Mẫu - SQL Server

## 📋 Dữ Liệu Được Tạo

File `INSERT_SAMPLE_DATA.sql` chứa dữ liệu mẫu cho toàn bộ hệ thống:

| Bảng | Số Lượng | Chi Tiết |
|------|----------|---------|
| **Customer** | 15 | Khách hàng nam/nữ với tên tiếng Việt |
| **Service** | 10 | Các dịch vụ salon (cắt, nhuộm, uốn, ép, massage, v.v.) |
| **Appointment** | 29 | Lịch hẹn với các trạng thái: PENDING, CONFIRMED, CANCELLED |
| **Payment** | 9 | Thanh toán bằng CASH/CARD |
| **SMS Log** | 10 | Nhật ký SMS gửi tới khách hàng |

---

## 🚀 Cách Sử Dụng

### Cách 1: Sử dụng SQL Server Management Studio (SSMS)

1. **Mở SQL Server Management Studio**

2. **Kết nối đến database:**
   - Server: localhost
   - Database: SalonBookingDB
   - Login: sa / YourPassword123!

3. **Mở file SQL:**
   - File → Open → File...
   - Chọn file: `INSERT_SAMPLE_DATA.sql`

4. **Chạy script:**
   - Nhấn F5 hoặc Ctrl+E để Execute
   - Hoặc Query → Execute

5. **Xem kết quả:**
   ```
   ===== SAMPLE DATA INSERTION COMPLETE =====
   
   Customers: 15
   Services: 10
   Appointments: 29
   Payments: 9
   SMS Logs: 10
   ```

### Cách 2: Sử dụng Command Line

```bash
# Sử dụng sqlcmd
sqlcmd -S localhost -U sa -P YourPassword123! -d SalonBookingDB -i INSERT_SAMPLE_DATA.sql

# Hoặc qua PowerShell
Invoke-Sqlcmd -ServerInstance localhost -Username sa -Password "YourPassword123!" -Database SalonBookingDB -InputFile "INSERT_SAMPLE_DATA.sql"
```

### Cách 3: Copy-Paste vào Query Editor

1. Mở SQL Server Management Studio
2. Tạo New Query
3. Copy toàn bộ nội dung từ `INSERT_SAMPLE_DATA.sql`
4. Paste vào Query Editor
5. Nhấn F5 để Execute

---

## 📊 Dữ Liệu Chi Tiết

### Khách Hàng (15 khách)
```
1. Nguyễn Văn An     - 0901234567
2. Trần Thị Bích     - 0902345678
3. Phạm Văn Chung    - 0903456789
4. Hoàng Thị Diễm    - 0904567890
5. Lê Văn Em         - 0905678901
6. Vũ Thị Hương      - 0906789012
7. Đặng Văn Khang    - 0907890123
8. Cao Thị Linh      - 0908901234
9. Tô Văn Minh       - 0909012345
10. Đỗ Thị Nhi       - 0910123456
11. Bùi Văn Phát     - 0911234567
12. Lý Thị Quỳnh     - 0912345678
13. Ngô Văn Rồng     - 0913456789
14. Dương Thị Sương  - 0914567890
15. Hà Văn Tùng      - 0915678901
```

### Dịch Vụ (10 dịch vụ)
```
1. Cắt tóc nam          - 150,000 VNĐ
2. Cắt tóc nữ           - 200,000 VNĐ
3. Nhuộm tóc            - 350,000 VNĐ
4. Uốn tóc              - 400,000 VNĐ
5. Ép tóc               - 300,000 VNĐ
6. Massage đầu          - 150,000 VNĐ
7. Gội đầu dưỡng sinh   - 100,000 VNĐ
8. Chăm sóc râu         - 80,000 VNĐ
9. Collagen tóc         - 500,000 VNĐ
10. Cắt tóc + Nhuộm    - 400,000 VNĐ
```

### Lịch Hẹn
- **CONFIRMED**: 10 lịch đã xác nhận
- **PENDING**: 17 lịch chờ xác nhận
- **CANCELLED**: 2 lịch đã hủy

---

## ✅ Xác Minh Dữ Liệu

### Sau khi Insert, chạy các query này để kiểm tra:

#### 1. Tất cả khách hàng
```sql
SELECT * FROM dbo.customer;
```

#### 2. Tất cả dịch vụ
```sql
SELECT * FROM dbo.service_entity;
```

#### 3. Tất cả lịch hẹn (kèm tên khách & dịch vụ)
```sql
SELECT 
    a.id,
    c.full_name AS customer_name,
    s.name AS service_name,
    a.appointment_time,
    a.status,
    a.note
FROM dbo.appointment a
JOIN dbo.customer c ON a.customer_id = c.id
JOIN dbo.service_entity s ON a.service_id = s.id
ORDER BY a.appointment_time;
```

#### 4. Lịch hẹn đã xác nhận
```sql
SELECT 
    a.id,
    c.full_name AS customer_name,
    s.name AS service_name,
    a.appointment_time,
    s.price
FROM dbo.appointment a
JOIN dbo.customer c ON a.customer_id = c.id
JOIN dbo.service_entity s ON a.service_id = s.id
WHERE a.status = N'CONFIRMED'
ORDER BY a.appointment_time;
```

#### 5. Lịch hẹn chờ xác nhận
```sql
SELECT 
    a.id,
    c.full_name AS customer_name,
    s.name AS service_name,
    a.appointment_time
FROM dbo.appointment a
JOIN dbo.customer c ON a.customer_id = c.id
JOIN dbo.service_entity s ON a.service_id = s.id
WHERE a.status = N'PENDING'
ORDER BY a.appointment_time;
```

#### 6. Tóm tắt thanh toán
```sql
SELECT 
    a.id AS appointment_id,
    c.full_name AS customer_name,
    s.name AS service_name,
    s.price,
    p.amount,
    p.payment_method,
    p.status AS payment_status
FROM dbo.payment p
JOIN dbo.appointment a ON p.appointment_id = a.id
JOIN dbo.customer c ON a.customer_id = c.id
JOIN dbo.service_entity s ON a.service_id = s.id
ORDER BY p.transaction_date DESC;
```

#### 7. Nhật ký SMS
```sql
SELECT * FROM dbo.sms_log ORDER BY sent_at DESC;
```

---

## 🗑️ Xóa Dữ Liệu (Nếu cần reset)

Nếu muốn xóa tất cả dữ liệu và chạy lại script:

```sql
DELETE FROM dbo.sms_log;
DELETE FROM dbo.payment;
DELETE FROM dbo.appointment;
DELETE FROM dbo.service_entity;
DELETE FROM dbo.customer;

-- Reset identity seeds
DBCC CHECKIDENT ('customer', RESEED, 0);
DBCC CHECKIDENT ('service_entity', RESEED, 0);
DBCC CHECKIDENT ('appointment', RESEED, 0);
DBCC CHECKIDENT ('payment', RESEED, 0);
DBCC CHECKIDENT ('sms_log', RESEED, 0);
```

---

## 🎯 Kiểm Tra Trên Swing Client

Sau khi Insert dữ liệu, chạy Swing client:

```bash
.\mvnw.cmd exec:java -Dexec.mainClass="com.salonnbooking.SwingClient"
```

### Các màn hình sẽ hiển thị:

1. **Appointment Panel**
   - ✅ Hiển thị 29 lịch hẹn
   - ✅ Tên khách & dịch vụ tự động resolve
   - ✅ Ngày/giờ định dạng: yyyy-MM-dd HH:mm

2. **Customer Panel**
   - ✅ Hiển thị 15 khách hàng
   - ✅ Số điện thoại, email, giới tính

3. **Service Panel**
   - ✅ Hiển thị 10 dịch vụ
   - ✅ Giá tiền định dạng đúng

4. **Functionality**
   - ✅ Thêm lịch hẹn mới → sẽ xuất hiện trong bảng
   - ✅ Sửa lịch → cập nhật trên database
   - ✅ Xóa lịch → xóa khỏi bảng
   - ✅ Refresh → tải lại dữ liệu

---

## 📝 Ghi Chú Quan Trọng

### Định dạng Dữ Liệu
- **Giới tính**: MALE, FEMALE
- **Trạng thái lịch**: PENDING, CONFIRMED, CANCELLED
- **Phương thức thanh toán**: CASH, CARD
- **Trạng thái thanh toán**: COMPLETED, PENDING, FAILED
- **Trạng thái SMS**: SENT, FAILED, PENDING

### Ngày giờ
- Tất cả appointment có thời gian chi tiết: `yyyy-MM-dd HH:mm:ss`
- Appointments từ 2026-05-07 trở đi (có lịch quá khứ, hiện tại, tương lai)

### Giá tiền
- Đơn vị: VNĐ (Vietnamese Dong)
- Lưu trữ dưới dạng DECIMAL(10,2)
- Ví dụ: 150000.00

---

## 🔗 Liên Quan Đến

- **Database**: SalonBookingDB (SQL Server)
- **Swing Client**: Sẽ tải dữ liệu từ Spring Boot API
- **Spring Boot**: localhost:8080/api/...

---

## ✨ Bước Tiếp Theo

1. ✅ Insert dữ liệu bằng script này
2. ✅ Kiểm tra bằng SSMS (xem các query bên trên)
3. ✅ Chạy Spring Boot backend
4. ✅ Chạy Swing client
5. ✅ Test CRUD operations với dữ liệu thực

---

**Dữ liệu được tạo**: 2026-05-07  
**Tổng records**: 64 (15 + 10 + 29 + 9 + 1)  
**Sẵn sàng**: ✅ YES
