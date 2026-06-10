-- Basic seed data for Salon Manager.
-- Run this after the database schema has been created.
-- This file only inserts accounts, customers, services and service rooms.
-- It does not insert appointments, payments or admin account.
--
-- Important relationship:
-- customers.username should match app_users.username for CUSTOMER accounts.
-- The system creates the admin account automatically: admin / 123456.

BEGIN TRANSACTION;

-- Staff account
INSERT INTO app_users (username, password, user_role, created_at)
VALUES
('staff01', '123456', 'STAFF', GETDATE());

-- Customer accounts
INSERT INTO app_users (username, password, user_role, created_at)
VALUES
('nguyenminhanh', '123456', 'CUSTOMER', GETDATE()),
('tranhoangnam', '123456', 'CUSTOMER', GETDATE()),
('lethanhtruc', '123456', 'CUSTOMER', GETDATE()),
('phamgiahuy', '123456', 'CUSTOMER', GETDATE()),
('vokimngan', '123456', 'CUSTOMER', GETDATE()),
('dangquocbao', '123456', 'CUSTOMER', GETDATE());

-- Customer profiles
-- username must match the CUSTOMER usernames above.
INSERT INTO customers (username, full_name, phone, email, gender, loyalty_points, note, created_at)
VALUES
('nguyenminhanh', N'Nguyễn Minh Anh', '0901000001', 'nguyen.minh.anh@example.com', 'female', 120, N'Khách quen, thích gội dưỡng sinh', GETDATE()),
('tranhoangnam', N'Trần Hoàng Nam', '0901000002', 'tran.hoang.nam@example.com', 'male', 80, N'Thường đặt lịch cuối tuần', GETDATE()),
('lethanhtruc', N'Lê Thanh Trúc', '0901000003', 'le.thanh.truc@example.com', 'female', 200, N'Ưu tiên stylist nữ', GETDATE()),
('phamgiahuy', N'Phạm Gia Huy', '0901000004', 'pham.gia.huy@example.com', 'male', 40, N'Cần nhắc lịch qua email', GETDATE()),
('vokimngan', N'Võ Kim Ngân', '0901000005', 'vo.kim.ngan@example.com', 'female', 260, N'Hay dùng combo tóc và nail', GETDATE()),
('dangquocbao', N'Đặng Quốc Bảo', '0901000006', 'dang.quoc.bao@example.com', 'male', 20, N'Khách mới', GETDATE());

-- Services
INSERT INTO services (name, price, duration_minutes, description, is_active)
VALUES
(N'Cắt tóc nữ', 120000.00, 45, N'Tư vấn kiểu tóc, cắt và sấy tạo kiểu', 1),
(N'Cắt tóc nam', 80000.00, 30, N'Cắt, gội và tạo kiểu nhanh', 1),
(N'Cắt tóc trẻ em', 70000.00, 30, N'Cắt tóc nhẹ nhàng cho bé', 1),
(N'Gội đầu thư giãn', 120000.00, 40, N'Gội đầu kết hợp massage da đầu', 1),
(N'Gội đầu dưỡng sinh', 180000.00, 60, N'Gội đầu, massage cổ vai gáy và thư giãn da đầu', 1),
(N'Sấy tạo kiểu', 90000.00, 30, N'Sấy phồng, uốn lọn nhẹ hoặc duỗi tự nhiên', 1),
(N'Nhuộm tóc thời trang', 750000.00, 150, N'Tư vấn màu, nhuộm và hấp khóa màu', 1),
(N'Uốn tóc', 650000.00, 120, N'Uốn lọn, uốn cụp hoặc tạo form theo yêu cầu', 1),
(N'Hấp phục hồi tóc', 250000.00, 60, N'Phục hồi tóc khô xơ bằng dưỡng chất', 1),
(N'Làm nail cơ bản', 150000.00, 60, N'Cắt da, tạo form móng và sơn màu cơ bản', 1);

-- Service rooms / areas
INSERT INTO service_rooms (name, description, is_active)
VALUES
(N'Phòng 1', N'Khu làm tóc chính', 1),
(N'Phòng 2', N'Khu gội đầu và dưỡng sinh', 1),
(N'Phòng 3', N'Khu nail và chăm sóc tay chân', 1);

COMMIT TRANSACTION;
