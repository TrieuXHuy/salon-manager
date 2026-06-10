SET NOCOUNT ON;

USE [booking_system];
GO

IF COL_LENGTH(N'dbo.customers', N'username') IS NULL
    ALTER TABLE [dbo].[customers] ADD [username] varchar(50) NULL;
GO

ALTER TABLE [dbo].[customers] ALTER COLUMN [full_name] nvarchar(255) NULL;
GO

ALTER TABLE [dbo].[customers] ALTER COLUMN [phone] varchar(20) NULL;
GO

IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_customers_phone' AND object_id = OBJECT_ID(N'dbo.customers'))
    DROP INDEX [UX_customers_phone] ON [dbo].[customers];
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_customers_phone_not_null' AND object_id = OBJECT_ID(N'dbo.customers'))
    CREATE UNIQUE INDEX [UX_customers_phone_not_null] ON [dbo].[customers] ([phone]) WHERE [phone] IS NOT NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_customers_username_not_null' AND object_id = OBJECT_ID(N'dbo.customers'))
    CREATE UNIQUE INDEX [UX_customers_username_not_null] ON [dbo].[customers] ([username]) WHERE [username] IS NOT NULL;
GO

/* =========================
   APP USERS
   ========================= */
MERGE [dbo].[app_users] AS target
USING (VALUES
    (N'owner01',     N'123456', N'OWNER'),
    (N'staff01',     N'123456', N'STAFF'),
    (N'staff02',     N'123456', N'STAFF'),
    (N'reception01', N'123456', N'STAFF'),
    (N'customer01',  N'123456', N'CUSTOMER'),
    (N'customer02',  N'123456', N'CUSTOMER')
) AS source ([username], [password], [user_role])
ON target.[username] = source.[username]
WHEN MATCHED THEN
    UPDATE SET
        [password] = source.[password],
        [user_role] = source.[user_role]
WHEN NOT MATCHED THEN
    INSERT ([username], [password], [user_role], [created_at])
    VALUES (source.[username], source.[password], source.[user_role], DATEADD(day, -30, SYSDATETIME()));
GO

/* =========================
   CUSTOMERS
   ========================= */
MERGE [dbo].[customers] AS target
USING (VALUES
    (N'Nguyễn Minh Anh',   '0900000001', N'minhanh01@example.com',   N'female',  120, N'[TEST] Khách ưu tiên', -12),
    (N'Trần Hoàng Nam',    '0900000002', N'hoangnam02@example.com',   N'male',     80, N'[TEST] Khách thường xuyên', -10),
    (N'Lê Thanh Trúc',     '0900000003', N'thanhtruc03@example.com',  N'female',   45, N'[TEST] Đặt lịch dịch vụ spa', -9),
    (N'Phạm Gia Huy',      '0900000004', N'giahuy04@example.com',      N'male',     10, N'[TEST] Khách mới', -8),
    (N'Võ Kim Ngân',       '0900000005', N'kimngan05@example.com',     N'female',  160, N'[TEST] Điểm cao', -7),
    (N'Đặng Quốc Bảo',     '0900000006', N'quocbao06@example.com',     N'male',     55, N'[TEST] Có nhu cầu cắt + gội', -6),
    (N'Bùi Phương Linh',   '0900000007', N'phuonglinh07@example.com',   N'female',   32, N'[TEST] Chăm sóc tóc dài', -5),
    (N'Hoàng Nhật Minh',   '0900000008', N'nhatminh08@example.com',     N'male',     14, N'[TEST] Khách trẻ', -4),
    (N'Ngô Thảo Vy',       '0900000009', N'thaovy09@example.com',       N'female',   90, N'[TEST] Khách thân thiết', -3),
    (N'Đỗ Hải Đăng',       '0900000010', N'haidang10@example.com',      N'male',     66, N'[TEST] Dùng để test cọc', -2),
    (N'Phan Khánh Linh',   '0900000011', N'khanhlinh11@example.com',   N'female',  140, N'[TEST] Dùng để test thanh toán phần còn lại', -1),
    (N'Vũ Đức Anh',        '0900000012', N'ducanh12@example.com',       N'male',     25, N'[TEST] Khách có note', 0),
    (N'Đinh Mai Chi',      '0900000013', N'maichi13@example.com',      N'female',   18, N'[TEST] Khách test báo cáo', 1),
    (N'Mai Tuấn Kiệt',     '0900000014', N'tuankiet14@example.com',     N'male',     98, N'[TEST] Dùng để test slot', 2),
    (N'Lâm Bảo Ngọc',      '0900000015', N'baongoc15@example.com',      N'female',   77, N'[TEST] Khách test giao diện', 3)
) AS source ([full_name], [phone], [email], [gender], [loyalty_points], [note], [created_days_ago])
ON target.[phone] = source.[phone]
WHEN MATCHED THEN
    UPDATE SET
        [full_name] = source.[full_name],
        [email] = source.[email],
        [gender] = source.[gender],
        [loyalty_points] = source.[loyalty_points],
        [note] = source.[note]
WHEN NOT MATCHED THEN
    INSERT ([full_name], [phone], [email], [gender], [loyalty_points], [note], [created_at])
    VALUES (
        source.[full_name],
        source.[phone],
        source.[email],
        source.[gender],
        source.[loyalty_points],
        source.[note],
        DATEADD(day, source.[created_days_ago], SYSDATETIME())
    );
GO

UPDATE [dbo].[customers]
SET [username] = N'customer01'
WHERE [phone] = '0900000001' AND ([username] IS NULL OR [username] = N'customer01');

UPDATE [dbo].[customers]
SET [username] = N'customer02'
WHERE [phone] = '0900000002' AND ([username] IS NULL OR [username] = N'customer02');
GO

/* =========================
   SERVICES
   ========================= */
MERGE [dbo].[services] AS target
USING (VALUES
    (N'Cắt tóc nam',             80000.00,  30,  N'Cắt, gội và tạo kiểu nhanh', 1),
    (N'Cắt tóc nữ',             120000.00,  45,  N'Tư vấn kiểu tóc, cắt và sấy tạo kiểu', 1),
    (N'Gội đầu dưỡng sinh',     180000.00,  60,  N'Gội đầu, massage cổ vai gáy và thư giãn da đầu', 1),
    (N'Hấp dầu phục hồi',       300000.00,  75,  N'Phục hồi tóc khô xơ, hư tổn nhẹ', 1),
    (N'Nhuộm tóc thời trang',   750000.00, 150,  N'Tư vấn màu, nhuộm và hấp khóa màu', 1),
    (N'Uốn setting',           950000.00, 180,  N'Uốn tạo kiểu với thuốc dưỡng chuyên sâu', 1),
    (N'Duỗi phục hồi',          850000.00, 150,  N'Duỗi thẳng kết hợp phục hồi keratin', 1),
    (N'Chăm sóc da cơ bản',     350000.00,  75,  N'Làm sạch, xông hơi, đắp mặt nạ và dưỡng da', 1),
    (N'Chăm sóc da chuyên sâu', 650000.00, 105,  N'Lấy nhân mụn, điện di tinh chất và phục hồi da', 1),
    (N'Massage body thư giãn',  500000.00,  90,  N'Massage toàn thân giảm căng cơ', 1),
    (N'Combo spa thư giãn',     980000.00, 150,  N'Chăm sóc da cơ bản và massage body', 1),
    (N'Combo tóc và nail',      620000.00, 120,  N'Cắt gội tạo kiểu và sơn gel', 1)
) AS source ([name], [price], [duration_minutes], [description], [is_active])
ON target.[name] = source.[name]
WHEN MATCHED THEN
    UPDATE SET
        [price] = source.[price],
        [duration_minutes] = source.[duration_minutes],
        [description] = source.[description],
        [is_active] = source.[is_active]
WHEN NOT MATCHED THEN
    INSERT ([name], [price], [duration_minutes], [description], [is_active])
    VALUES (source.[name], source.[price], source.[duration_minutes], source.[description], source.[is_active]);
GO
