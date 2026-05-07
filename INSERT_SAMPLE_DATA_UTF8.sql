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
(N'Nguyá»…n VÄƒn An', '0901234567', 'an.nguyen@email.com', N'male', GETDATE()),
(N'Tráº§n Thá»‹ BÃ­ch', '0902345678', 'bich.tran@email.com', N'female', GETDATE()),
(N'Pháº¡m VÄƒn Chung', '0903456789', 'chung.pham@email.com', N'male', GETDATE()),
(N'HoÃ ng Thá»‹ Diá»…m', '0904567890', 'diem.hoang@email.com', N'female', GETDATE()),
(N'LÃª VÄƒn Em', '0905678901', 'em.le@email.com', N'male', GETDATE()),
(N'VÅ© Thá»‹ HÆ°Æ¡ng', '0906789012', 'huong.vu@email.com', N'female', GETDATE()),
(N'Äáº·ng VÄƒn Khang', '0907890123', 'khang.dang@email.com', N'male', GETDATE()),
(N'Cao Thá»‹ Linh', '0908901234', 'linh.cao@email.com', N'female', GETDATE()),
(N'TÃ´ VÄƒn Minh', '0909012345', 'minh.to@email.com', N'male', GETDATE()),
(N'Äá»— Thá»‹ Nhi', '0910123456', 'nhi.do@email.com', N'female', GETDATE()),
(N'BÃ¹i VÄƒn PhÃ¡t', '0911234567', 'phat.bui@email.com', N'male', GETDATE()),
(N'LÃ½ Thá»‹ Quá»³nh', '0912345678', 'quynh.ly@email.com', N'female', GETDATE()),
(N'NgÃ´ VÄƒn Rá»“ng', '0913456789', 'rong.ngo@email.com', N'male', GETDATE()),
(N'DÆ°Æ¡ng Thá»‹ SÆ°Æ¡ng', '0914567890', 'suong.duong@email.com', N'female', GETDATE()),
(N'HÃ  VÄƒn TÃ¹ng', '0915678901', 'tung.ha@email.com', N'male', GETDATE());

-- =====================================================
-- Insert Sample Services
-- =====================================================
INSERT INTO services (name, description, price, duration_minutes, is_active) VALUES
(N'Cáº¯t tÃ³c nam', N'Dá»‹ch vá»¥ cáº¯t tÃ³c cÆ¡ báº£n cho nam', 150000.00, 30, 1),
(N'Cáº¯t tÃ³c ná»¯', N'Dá»‹ch vá»¥ cáº¯t tÃ³c cho ná»¯', 200000.00, 45, 1),
(N'Nhuá»™m tÃ³c', N'Nhuá»™m tÃ³c vá»›i sáº£n pháº©m chuyÃªn nghiá»‡p', 350000.00, 90, 1),
(N'Uá»‘n tÃ³c', N'Uá»‘n tÃ³c kiá»ƒu HÃ n, Nháº­t', 400000.00, 120, 1),
(N'Ã‰p tÃ³c', N'Ã‰p tÃ³c duá»—i tháº³ng', 300000.00, 90, 1),
(N'Massage Ä‘áº§u', N'Massage thÆ° giÃ£n Ä‘áº§u 30 phÃºt', 150000.00, 30, 1),
(N'Gá»™i Ä‘áº§u dÆ°á»¡ng sinh', N'Gá»™i Ä‘áº§u vá»›i serum dÆ°á»¡ng tÃ³c', 100000.00, 20, 1),
(N'ChÄƒm sÃ³c rÃ¢u', N'Cáº¡o rÃ¢u, chá»‰nh rÃ¢u', 80000.00, 15, 1),
(N'Collagen tÃ³c', N'Trá»‹ liá»‡u collagen phá»¥c há»“i tÃ³c', 500000.00, 60, 1),
(N'Cáº¯t tÃ³c + Nhuá»™m', N'Combo cáº¯t tÃ³c vÃ  nhuá»™m', 400000.00, 120, 1);

-- =====================================================
-- Insert Sample Appointments
-- =====================================================
INSERT INTO appointments (customer_id, service_id, appointment_time, status, note, created_at) VALUES
-- Customer 1 - Nguyá»…n VÄƒn An
(1, 1, '2026-05-10 09:00:00', N'confirmed', N'Cáº¯t tÃ³c kiá»ƒu cá»©ng', GETDATE()),
(1, 7, '2026-05-15 10:30:00', N'pending', N'Gá»™i Ä‘áº§u sau cáº¯t', GETDATE()),

-- Customer 2 - Tráº§n Thá»‹ BÃ­ch
(2, 2, '2026-05-08 14:00:00', N'confirmed', N'Cáº¯t tÃ³c uá»‘n cÆ¡ báº£n', GETDATE()),
(2, 3, '2026-05-12 11:00:00', N'confirmed', N'Nhuá»™m blonde', GETDATE()),
(2, 9, '2026-05-20 15:00:00', N'pending', N'Collagen chuyÃªn sÃ¢u', GETDATE()),

-- Customer 3 - Pháº¡m VÄƒn Chung
(3, 1, '2026-05-09 08:30:00', N'confirmed', N'Cáº¯t tÃ³c Ä‘á»‹nh ká»³', GETDATE()),
(3, 8, '2026-05-16 16:00:00', N'pending', N'Chá»‰nh rÃ¢u', GETDATE()),

-- Customer 4 - HoÃ ng Thá»‹ Diá»…m
(4, 4, '2026-05-07 13:00:00', N'cancelled', N'Báº­n khÃ´ng Ä‘Æ°á»£c', GETDATE()),
(4, 2, '2026-05-18 09:30:00', N'confirmed', N'Cáº¯t tÃ³c má»›i', GETDATE()),
(4, 6, '2026-05-22 14:00:00', N'pending', N'Massage thÆ° giÃ£n', GETDATE()),

-- Customer 5 - LÃª VÄƒn Em
(5, 1, '2026-05-11 10:00:00', N'confirmed', N'Cáº¯t tÃ³c', GETDATE()),
(5, 5, '2026-05-17 15:30:00', N'pending', N'Ã‰p tÃ³c', GETDATE()),

-- Customer 6 - VÅ© Thá»‹ HÆ°Æ¡ng
(6, 3, '2026-05-13 10:30:00', N'confirmed', N'Nhuá»™m Ä‘á» rÆ°á»£u', GETDATE()),
(6, 10, '2026-05-25 11:00:00', N'pending', N'Cáº¯t + nhuá»™m combo', GETDATE()),

-- Customer 7 - Äáº·ng VÄƒn Khang
(7, 1, '2026-05-14 14:00:00', N'confirmed', N'Cáº¯t tÃ³c mohawk', GETDATE()),
(7, 8, '2026-05-21 16:00:00', N'pending', N'Chá»‰nh rÃ¢u', GETDATE()),

-- Customer 8 - Cao Thá»‹ Linh
(8, 2, '2026-05-19 13:00:00', N'confirmed', N'Cáº¯t layer', GETDATE()),
(8, 4, '2026-05-24 10:00:00', N'pending', N'Uá»‘n xoÄƒn', GETDATE()),

-- Customer 9 - TÃ´ VÄƒn Minh
(9, 1, '2026-05-23 09:00:00', N'pending', N'Cáº¯t tÃ³c', GETDATE()),
(9, 7, '2026-05-26 14:00:00', N'pending', N'Gá»™i Ä‘áº§u', GETDATE()),

-- Customer 10 - Äá»— Thá»‹ Nhi
(10, 3, '2026-05-27 10:30:00', N'pending', N'Nhuá»™m highlight', GETDATE()),
(10, 9, '2026-05-29 15:00:00', N'pending', N'Collagen tÃ³c', GETDATE()),

-- Customer 11 - BÃ¹i VÄƒn PhÃ¡t
(11, 1, '2026-05-28 08:00:00', N'pending', N'Cáº¯t tÃ³c', GETDATE()),
(11, 8, '2026-05-30 17:00:00', N'pending', N'Chá»‰nh rÃ¢u', GETDATE()),

-- Customer 12 - LÃ½ Thá»‹ Quá»³nh
(12, 2, '2026-05-31 11:00:00', N'pending', N'Cáº¯t tÃ³c ná»¯', GETDATE()),
(12, 6, '2026-06-02 16:00:00', N'pending', N'Massage Ä‘áº§u', GETDATE()),

-- Customer 13 - NgÃ´ VÄƒn Rá»“ng
(13, 1, '2026-06-01 09:30:00', N'pending', N'Cáº¯t tÃ³c', GETDATE()),

-- Customer 14 - DÆ°Æ¡ng Thá»‹ SÆ°Æ¡ng
(14, 4, '2026-06-03 13:00:00', N'pending', N'Uá»‘n tÃ³c', GETDATE()),
(14, 5, '2026-06-05 14:00:00', N'pending', N'Ã‰p tÃ³c', GETDATE()),

-- Customer 15 - HÃ  VÄƒn TÃ¹ng
(15, 1, '2026-06-04 10:00:00', N'pending', N'Cáº¯t tÃ³c Ä‘á»‹nh ká»³', GETDATE()),
(15, 3, '2026-06-07 15:00:00', N'pending', N'Nhuá»™m tÃ³c', GETDATE());

-- =====================================================
-- Insert Sample Payments
-- =====================================================
INSERT INTO payments (appointment_id, amount, payment_method, payment_status, paid_at) VALUES
(1, 150000.00, N'cash', N'paid', '2026-05-10 09:45:00'),
(3, 200000.00, N'card', N'paid', '2026-05-08 14:30:00'),
(4, 350000.00, N'cash', N'paid', '2026-05-12 11:45:00'),
(6, 150000.00, N'card', N'paid', '2026-05-09 09:15:00'),
(8, 200000.00, N'cash', N'unpaid', '2026-05-18 09:30:00'),
(10, 150000.00, N'card', N'paid', '2026-05-11 10:45:00'),
(12, 350000.00, N'cash', N'paid', '2026-05-13 11:00:00'),
(14, 150000.00, N'card', N'paid', '2026-05-14 14:45:00'),
(16, 200000.00, N'cash', N'paid', '2026-05-19 13:30:00');

-- =====================================================
-- Insert Sample SMS Logs
-- =====================================================
INSERT INTO sms_logs (appointment_id, phone, message, status, sent_at) VALUES
(1, N'0901234567', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch cáº¯t tÃ³c vÃ o 09:00 10/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-08 09:00:00'),
(3, N'0902345678', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch cáº¯t tÃ³c vÃ o 14:00 08/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-06 10:00:00'),
(4, N'0902345678', N'Nháº¯c nhá»Ÿ: Báº¡n cÃ³ lá»‹ch nhuá»™m tÃ³c vÃ o 11:00 12/5. Vui lÃ²ng Ä‘áº¿n Ä‘Ãºng giá»', N'success', '2026-05-11 18:00:00'),
(6, N'0903456789', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch cáº¯t tÃ³c vÃ o 08:30 09/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-07 14:00:00'),
(8, N'0904567890', N'ThÃ´ng bÃ¡o: Lá»‹ch háº¹n cá»§a báº¡n vÃ o 13:00 07/5 Ä‘Ã£ bá»‹ há»§y', N'success', '2026-05-06 16:00:00'),
(9, N'0904567890', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch cáº¯t tÃ³c vÃ o 09:30 18/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-16 10:00:00'),
(10, N'0905678901', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch gá»™i Ä‘áº§u vÃ o 10:00 11/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-09 10:00:00'),
(12, N'0906789012', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch nhuá»™m tÃ³c vÃ o 10:30 13/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-11 14:00:00'),
(14, N'0907890123', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch cáº¯t tÃ³c vÃ o 14:00 14/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-12 10:00:00'),
(16, N'0908901234', N'XÃ¡c nháº­n: Báº¡n cÃ³ lá»‹ch cáº¯t tÃ³c vÃ o 13:00 19/5. Vui lÃ²ng xÃ¡c nháº­n', N'success', '2026-05-17 10:00:00');

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

