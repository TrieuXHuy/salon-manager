-- =====================================================
-- Salon Booking System - Sample Data for Testing
-- SQL Server
-- =====================================================

-- Make sure you're using the correct database
USE [booking_system];
GO

SET XACT_ABORT ON;
GO

BEGIN TRANSACTION;

-- Clear existing sample data so identity IDs start from 1 again.
-- This script uses fixed customer_id/service_id/appointment_id values below,
-- so it must run on a clean set of these tables.
DELETE FROM sms_logs;
DELETE FROM payments;
DELETE FROM appointments;
DELETE FROM services;
DELETE FROM customers;
DBCC CHECKIDENT ('customers', RESEED, 0);
DBCC CHECKIDENT ('services', RESEED, 0);
DBCC CHECKIDENT ('appointments', RESEED, 0);
DBCC CHECKIDENT ('payments', RESEED, 0);
DBCC CHECKIDENT ('sms_logs', RESEED, 0);

-- =====================================================
-- Insert Sample Customers
-- =====================================================
INSERT INTO customers (full_name, phone, email, gender, created_at) VALUES
(N'Nguyễn Văn An', '0901234567', 'an.nguyen@email.com', N'MALE', GETDATE()),
(N'Trần Thị Bích', '0902345678', 'bich.tran@email.com', N'FEMALE', GETDATE()),
(N'Phạm Văn Chung', '0903456789', 'chung.pham@email.com', N'MALE', GETDATE()),
(N'Hoàng Thị Diễm', '0904567890', 'diem.hoang@email.com', N'FEMALE', GETDATE()),
(N'Lê Văn Em', '0905678901', 'em.le@email.com', N'MALE', GETDATE()),
(N'Vũ Thị Hương', '0906789012', 'huong.vu@email.com', N'FEMALE', GETDATE()),
(N'Đặng Văn Khang', '0907890123', 'khang.dang@email.com', N'MALE', GETDATE()),
(N'Cao Thị Linh', '0908901234', 'linh.cao@email.com', N'FEMALE', GETDATE()),
(N'Tô Văn Minh', '0909012345', 'minh.to@email.com', N'MALE', GETDATE()),
(N'Đỗ Thị Nhi', '0910123456', 'nhi.do@email.com', N'FEMALE', GETDATE()),
(N'Bùi Văn Phát', '0911234567', 'phat.bui@email.com', N'MALE', GETDATE()),
(N'Lý Thị Quỳnh', '0912345678', 'quynh.ly@email.com', N'FEMALE', GETDATE()),
(N'Ngô Văn Rồng', '0913456789', 'rong.ngo@email.com', N'MALE', GETDATE()),
(N'Dương Thị Sương', '0914567890', 'suong.duong@email.com', N'FEMALE', GETDATE()),
(N'Hà Văn Tùng', '0915678901', 'tung.ha@email.com', N'MALE', GETDATE());

-- =====================================================
-- Insert Sample Services
-- =====================================================
INSERT INTO services (name, description, price, duration_minutes, is_active) VALUES
(N'Cắt tóc nam', N'Dịch vụ cắt tóc cơ bản cho nam', 150000.00, 30, 1),
(N'Cắt tóc nữ', N'Dịch vụ cắt tóc cho nữ', 200000.00, 45, 1),
(N'Nhuộm tóc', N'Nhuộm tóc với sản phẩm chuyên nghiệp', 350000.00, 90, 1),
(N'Uốn tóc', N'Uốn tóc kiểu Hàn, Nhật', 400000.00, 120, 1),
(N'Ép tóc', N'Ép tóc duỗi thẳng', 300000.00, 90, 1),
(N'Massage đầu', N'Massage thư giãn đầu 30 phút', 150000.00, 30, 1),
(N'Gội đầu dưỡng sinh', N'Gội đầu với serum dưỡng tóc', 100000.00, 20, 1),
(N'Chăm sóc râu', N'Cạo râu, chỉnh râu', 80000.00, 15, 1),
(N'Collagen tóc', N'Trị liệu collagen phục hồi tóc', 500000.00, 60, 1),
(N'Cắt tóc + Nhuộm', N'Combo cắt tóc và nhuộm', 400000.00, 120, 1);

-- =====================================================
-- Insert Sample Appointments
-- =====================================================
INSERT INTO appointments (customer_id, service_id, appointment_time, status, note, created_at) VALUES
-- Customer 1 - Nguyễn Văn An
(1, 1, '2026-05-10 09:00:00', N'confirmed', N'Cắt tóc kiểu cứng', GETDATE()),
(1, 7, '2026-05-15 10:30:00', N'pending', N'Gội đầu sau cắt', GETDATE()),

-- Customer 2 - Trần Thị Bích
(2, 2, '2026-05-08 14:00:00', N'confirmed', N'Cắt tóc uốn cơ bản', GETDATE()),
(2, 3, '2026-05-12 11:00:00', N'confirmed', N'Nhuộm blonde', GETDATE()),
(2, 9, '2026-05-20 15:00:00', N'pending', N'Collagen chuyên sâu', GETDATE()),

-- Customer 3 - Phạm Văn Chung
(3, 1, '2026-05-09 08:30:00', N'confirmed', N'Cắt tóc định kỳ', GETDATE()),
(3, 8, '2026-05-16 16:00:00', N'pending', N'Chỉnh râu', GETDATE()),

-- Customer 4 - Hoàng Thị Diễm
(4, 4, '2026-05-07 13:00:00', N'cancelled', N'Bận không được', GETDATE()),
(4, 2, '2026-05-18 09:30:00', N'confirmed', N'Cắt tóc mới', GETDATE()),
(4, 6, '2026-05-22 14:00:00', N'pending', N'Massage thư giãn', GETDATE()),

-- Customer 5 - Lê Văn Em
(5, 1, '2026-05-11 10:00:00', N'confirmed', N'Cắt tóc', GETDATE()),
(5, 5, '2026-05-17 15:30:00', N'pending', N'Ép tóc', GETDATE()),

-- Customer 6 - Vũ Thị Hương
(6, 3, '2026-05-13 10:30:00', N'confirmed', N'Nhuộm đỏ rượu', GETDATE()),
(6, 10, '2026-05-25 11:00:00', N'pending', N'Cắt + nhuộm combo', GETDATE()),

-- Customer 7 - Đặng Văn Khang
(7, 1, '2026-05-14 14:00:00', N'confirmed', N'Cắt tóc mohawk', GETDATE()),
(7, 8, '2026-05-21 16:00:00', N'pending', N'Chỉnh râu', GETDATE()),

-- Customer 8 - Cao Thị Linh
(8, 2, '2026-05-19 13:00:00', N'confirmed', N'Cắt layer', GETDATE()),
(8, 4, '2026-05-24 10:00:00', N'pending', N'Uốn xoăn', GETDATE()),

-- Customer 9 - Tô Văn Minh
(9, 1, '2026-05-23 09:00:00', N'pending', N'Cắt tóc', GETDATE()),
(9, 7, '2026-05-26 14:00:00', N'pending', N'Gội đầu', GETDATE()),

-- Customer 10 - Đỗ Thị Nhi
(10, 3, '2026-05-27 10:30:00', N'pending', N'Nhuộm highlight', GETDATE()),
(10, 9, '2026-05-29 15:00:00', N'pending', N'Collagen tóc', GETDATE()),

-- Customer 11 - Bùi Văn Phát
(11, 1, '2026-05-28 08:00:00', N'pending', N'Cắt tóc', GETDATE()),
(11, 8, '2026-05-30 17:00:00', N'pending', N'Chỉnh râu', GETDATE()),

-- Customer 12 - Lý Thị Quỳnh
(12, 2, '2026-05-31 11:00:00', N'pending', N'Cắt tóc nữ', GETDATE()),
(12, 6, '2026-06-02 16:00:00', N'pending', N'Massage đầu', GETDATE()),

-- Customer 13 - Ngô Văn Rồng
(13, 1, '2026-06-01 09:30:00', N'pending', N'Cắt tóc', GETDATE()),

-- Customer 14 - Dương Thị Sương
(14, 4, '2026-06-03 13:00:00', N'pending', N'Uốn tóc', GETDATE()),
(14, 5, '2026-06-05 14:00:00', N'pending', N'Ép tóc', GETDATE()),

-- Customer 15 - Hà Văn Tùng
(15, 1, '2026-06-04 10:00:00', N'pending', N'Cắt tóc định kỳ', GETDATE()),
(15, 3, '2026-06-07 15:00:00', N'pending', N'Nhuộm tóc', GETDATE());

-- =====================================================
-- Insert Sample Payments
-- =====================================================
INSERT INTO payments (appointment_id, amount, payment_method, payment_status, paid_at) VALUES
(1, 150000.00, N'CASH', N'paid', '2026-05-10 09:45:00'),
(3, 200000.00, N'CARD', N'paid', '2026-05-08 14:30:00'),
(4, 350000.00, N'CASH', N'paid', '2026-05-12 11:45:00'),
(6, 150000.00, N'CARD', N'paid', '2026-05-09 09:15:00'),
(8, 200000.00, N'CASH', N'unpaid', '2026-05-18 09:30:00'),
(10, 150000.00, N'CARD', N'paid', '2026-05-11 10:45:00'),
(12, 350000.00, N'CASH', N'paid', '2026-05-13 11:00:00'),
(14, 150000.00, N'CARD', N'paid', '2026-05-14 14:45:00'),
(16, 200000.00, N'CASH', N'paid', '2026-05-19 13:30:00');

-- =====================================================
-- Insert Sample SMS Logs
-- =====================================================
INSERT INTO sms_logs (appointment_id, phone, message, status, sent_at) VALUES
(1, N'0901234567', N'Xác nhận: Bạn có lịch cắt tóc vào 09:00 10/5. Vui lòng xác nhận', N'success', '2026-05-08 09:00:00'),
(3, N'0902345678', N'Xác nhận: Bạn có lịch cắt tóc vào 14:00 08/5. Vui lòng xác nhận', N'success', '2026-05-06 10:00:00'),
(4, N'0902345678', N'Nhắc nhở: Bạn có lịch nhuộm tóc vào 11:00 12/5. Vui lòng đến đúng giờ', N'success', '2026-05-11 18:00:00'),
(6, N'0903456789', N'Xác nhận: Bạn có lịch cắt tóc vào 08:30 09/5. Vui lòng xác nhận', N'success', '2026-05-07 14:00:00'),
(8, N'0904567890', N'Thông báo: Lịch hẹn của bạn vào 13:00 07/5 đã bị hủy', N'success', '2026-05-06 16:00:00'),
(9, N'0904567890', N'Xác nhận: Bạn có lịch cắt tóc vào 09:30 18/5. Vui lòng xác nhận', N'success', '2026-05-16 10:00:00'),
(10, N'0905678901', N'Xác nhận: Bạn có lịch gội đầu vào 10:00 11/5. Vui lòng xác nhận', N'success', '2026-05-09 10:00:00'),
(12, N'0906789012', N'Xác nhận: Bạn có lịch nhuộm tóc vào 10:30 13/5. Vui lòng xác nhận', N'success', '2026-05-11 14:00:00'),
(14, N'0907890123', N'Xác nhận: Bạn có lịch cắt tóc vào 14:00 14/5. Vui lòng xác nhận', N'success', '2026-05-12 10:00:00'),
(16, N'0908901234', N'Xác nhận: Bạn có lịch cắt tóc vào 13:00 19/5. Vui lòng xác nhận', N'success', '2026-05-17 10:00:00');

COMMIT TRANSACTION;

-- =====================================================
-- Verify Data
-- =====================================================
DECLARE @customerCount INT;
DECLARE @serviceCount INT;
DECLARE @appointmentCount INT;
DECLARE @paymentCount INT;
DECLARE @smsLogCount INT;

SELECT @customerCount = COUNT(*) FROM customers;
SELECT @serviceCount = COUNT(*) FROM services;
SELECT @appointmentCount = COUNT(*) FROM appointments;
SELECT @paymentCount = COUNT(*) FROM payments;
SELECT @smsLogCount = COUNT(*) FROM sms_logs;

PRINT '===== SAMPLE DATA INSERTION COMPLETE =====';
PRINT '';
PRINT 'Customers: ' + CAST(@customerCount AS NVARCHAR(10));
PRINT 'Services: ' + CAST(@serviceCount AS NVARCHAR(10));
PRINT 'Appointments: ' + CAST(@appointmentCount AS NVARCHAR(10));
PRINT 'Payments: ' + CAST(@paymentCount AS NVARCHAR(10));
PRINT 'SMS Logs: ' + CAST(@smsLogCount AS NVARCHAR(10));
PRINT '';
PRINT 'You can now run the Swing client and see the data!';

-- =====================================================
-- Optional: View Sample Data
-- =====================================================
/*
-- View all customers
SELECT * FROM customers;

-- View all services
SELECT * FROM services;

-- View all appointments with details
SELECT 
    a.id,
    c.full_name AS customer_name,
    s.name AS service_name,
    a.appointment_time,
    a.status,
    a.note
FROM appointments a
JOIN customers c ON a.customer_id = c.id
JOIN services s ON a.service_id = s.id
ORDER BY a.appointment_time;

-- View confirmed appointments
SELECT 
    a.id,
    c.full_name AS customer_name,
    s.name AS service_name,
    a.appointment_time,
    s.price,
    a.status
FROM appointments a
JOIN customers c ON a.customer_id = c.id
JOIN services s ON a.service_id = s.id
WHERE a.status = N'confirmed'
ORDER BY a.appointment_time;

-- View pending appointments
SELECT 
    a.id,
    c.full_name AS customer_name,
    s.name AS service_name,
    a.appointment_time,
    a.status
FROM appointments a
JOIN customers c ON a.customer_id = c.id
JOIN services s ON a.service_id = s.id
WHERE a.status = N'pending'
ORDER BY a.appointment_time;

-- View payment summary
SELECT 
    a.id AS appointment_id,
    c.full_name AS customer_name,
    s.name AS service_name,
    s.price,
    p.amount,
    p.payment_method,
    p.payment_status
FROM payments p
JOIN appointments a ON p.appointment_id = a.id
JOIN customers c ON a.customer_id = c.id
JOIN services s ON a.service_id = s.id
ORDER BY p.paid_at DESC;
*/
