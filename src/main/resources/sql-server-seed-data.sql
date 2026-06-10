SET NOCOUNT ON;

IF DB_ID(N'booking_system') IS NULL
BEGIN
    CREATE DATABASE [booking_system];
END
GO

USE [booking_system];
GO

IF OBJECT_ID(N'dbo.customers', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[customers] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_customers] PRIMARY KEY,
        [username] varchar(50) NULL,
        [full_name] nvarchar(255) NULL,
        [phone] varchar(20) NULL,
        [email] nvarchar(255) NULL,
        [gender] nvarchar(10) NULL,
        [loyalty_points] int NOT NULL CONSTRAINT [DF_customers_loyalty_points] DEFAULT 0,
        [note] nvarchar(500) NULL,
        [created_at] datetime2(6) NOT NULL CONSTRAINT [DF_customers_created_at] DEFAULT SYSUTCDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.services', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[services] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_services] PRIMARY KEY,
        [name] nvarchar(255) NOT NULL,
        [price] decimal(10,2) NOT NULL,
        [duration_minutes] int NOT NULL,
        [description] nvarchar(500) NULL,
        [is_active] bit NOT NULL CONSTRAINT [DF_services_is_active] DEFAULT 1
    );
END
GO

IF OBJECT_ID(N'dbo.service_rooms', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[service_rooms] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_service_rooms] PRIMARY KEY,
        [name] nvarchar(100) NOT NULL,
        [description] nvarchar(255) NULL,
        [is_active] bit NOT NULL CONSTRAINT [DF_service_rooms_is_active] DEFAULT 1
    );
END
GO

IF OBJECT_ID(N'dbo.appointments', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[appointments] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_appointments] PRIMARY KEY,
        [customer_id] int NOT NULL,
        [service_id] int NOT NULL,
        [room_id] int NULL,
        [appointment_time] datetime2(6) NOT NULL,
        [total_amount] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_total_amount] DEFAULT 0,
        [deposit_amount] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_deposit_amount] DEFAULT 0,
        [amount_paid] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_amount_paid] DEFAULT 0,
        [remaining_amount] decimal(10,2) NOT NULL CONSTRAINT [DF_appointments_remaining_amount] DEFAULT 0,
        [status] nvarchar(50) NOT NULL,
        [note] nvarchar(500) NULL,
        [created_at] datetime2(6) NOT NULL CONSTRAINT [DF_appointments_created_at] DEFAULT SYSUTCDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.payments', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[payments] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_payments] PRIMARY KEY,
        [appointment_id] int NOT NULL,
        [amount] decimal(10,2) NOT NULL,
        [payment_stage] nvarchar(50) NOT NULL CONSTRAINT [DF_payments_payment_stage] DEFAULT N'deposit',
        [payment_method] nvarchar(50) NULL,
        [payment_status] nvarchar(50) NOT NULL,
        [paid_at] datetime2(6) NULL
    );
END
GO

IF OBJECT_ID(N'dbo.sms_logs', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[sms_logs] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_sms_logs] PRIMARY KEY,
        [appointment_id] int NOT NULL,
        [phone] varchar(20) NOT NULL,
        [message] nvarchar(500) NOT NULL,
        [sent_at] datetime2(6) NOT NULL CONSTRAINT [DF_sms_logs_sent_at] DEFAULT SYSUTCDATETIME(),
        [status] nvarchar(50) NOT NULL
    );
END
GO

IF OBJECT_ID(N'dbo.app_users', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[app_users] (
        [id] int IDENTITY(1,1) NOT NULL CONSTRAINT [PK_app_users] PRIMARY KEY,
        [username] varchar(50) NOT NULL,
        [password] varchar(255) NOT NULL,
        [user_role] varchar(20) NULL,
        [created_at] datetime2(6) NOT NULL CONSTRAINT [DF_app_users_created_at] DEFAULT SYSUTCDATETIME()
    );
END
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

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_app_users_username' AND object_id = OBJECT_ID(N'dbo.app_users'))
    CREATE UNIQUE INDEX [UX_app_users_username] ON [dbo].[app_users] ([username]);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_customers_gender')
    ALTER TABLE [dbo].[customers] ADD CONSTRAINT [CK_customers_gender] CHECK ([gender] IS NULL OR [gender] IN (N'male', N'female', N'other'));
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_appointment_status')
    ALTER TABLE [dbo].[appointments] DROP CONSTRAINT [CK_appointment_status];
ALTER TABLE [dbo].[appointments] ADD CONSTRAINT [CK_appointment_status] CHECK ([status] IN (N'pending', N'confirmed', N'in_progress', N'awaiting_payment', N'completed', N'cancelled', N'paid'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_payment_status')
    ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payment_status] CHECK ([payment_status] IN (N'unpaid', N'paid', N'refunded'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_payment_method')
    ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payment_method] CHECK ([payment_method] IS NULL OR [payment_method] IN (N'cash', N'bank_transfer', N'momo', N'card'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_payment_stage')
    ALTER TABLE [dbo].[payments] ADD CONSTRAINT [CK_payment_stage] CHECK ([payment_stage] IN (N'deposit', N'balance'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_sms_status')
    ALTER TABLE [dbo].[sms_logs] ADD CONSTRAINT [CK_sms_status] CHECK ([status] IN (N'success', N'failed'));
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_appointments_customers')
    ALTER TABLE [dbo].[appointments] ADD CONSTRAINT [FK_appointments_customers] FOREIGN KEY ([customer_id]) REFERENCES [dbo].[customers] ([id]);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_appointments_services')
    ALTER TABLE [dbo].[appointments] ADD CONSTRAINT [FK_appointments_services] FOREIGN KEY ([service_id]) REFERENCES [dbo].[services] ([id]);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_appointments_service_rooms')
    ALTER TABLE [dbo].[appointments] ADD CONSTRAINT [FK_appointments_service_rooms] FOREIGN KEY ([room_id]) REFERENCES [dbo].[service_rooms] ([id]);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_payments_appointments')
    ALTER TABLE [dbo].[payments] ADD CONSTRAINT [FK_payments_appointments] FOREIGN KEY ([appointment_id]) REFERENCES [dbo].[appointments] ([id]);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_sms_logs_appointments')
    ALTER TABLE [dbo].[sms_logs] ADD CONSTRAINT [FK_sms_logs_appointments] FOREIGN KEY ([appointment_id]) REFERENCES [dbo].[appointments] ([id]);
GO

MERGE [dbo].[app_users] AS target
USING (VALUES
    ('admin', '123456', 'OWNER'),
    ('owner01', '123456', 'OWNER'),
    ('staff01', '123456', 'STAFF'),
    ('staff02', '123456', 'STAFF'),
    ('staff03', '123456', 'STAFF'),
    ('reception01', '123456', 'STAFF'),
    ('customer01', '123456', 'CUSTOMER'),
    ('customer02', '123456', 'CUSTOMER')
) AS source ([username], [password], [user_role])
ON target.[username] = source.[username]
WHEN MATCHED THEN
    UPDATE SET [password] = source.[password], [user_role] = source.[user_role]
WHEN NOT MATCHED THEN
    INSERT ([username], [password], [user_role], [created_at])
    VALUES (source.[username], source.[password], source.[user_role], DATEADD(day, -90, SYSDATETIME()));
GO

MERGE [dbo].[service_rooms] AS target
USING (VALUES
    (N'Phòng 1', N'Khu phục vụ tiêu chuẩn', 1),
    (N'Phòng 2', N'Khu phục vụ tiêu chuẩn', 1),
    (N'Phòng 3', N'Khu VIP yên tĩnh', 1),
    (N'Phòng 4', N'Khu chăm sóc tóc và da đầu', 1),
    (N'Phòng 5', N'Khu nail và mi', 1),
    (N'Phòng Couple', N'Phòng đôi cho khách đi cùng bạn bè hoặc gia đình', 1),
    (N'Phòng Spa VIP', N'Phòng spa cao cấp, riêng tư', 1),
    (N'Phòng Bảo trì', N'Tạm ngưng nhận lịch', 0)
) AS source ([name], [description], [is_active])
ON target.[name] = source.[name]
WHEN MATCHED THEN
    UPDATE SET [description] = source.[description], [is_active] = source.[is_active]
WHEN NOT MATCHED THEN
    INSERT ([name], [description], [is_active])
    VALUES (source.[name], source.[description], source.[is_active]);
GO

MERGE [dbo].[services] AS target
USING (VALUES
    (N'Cắt tóc nữ', 120000.00, 45, N'Tư vấn kiểu tóc, cắt và sấy tạo kiểu', 1),
    (N'Cắt tóc nam', 80000.00, 30, N'Cắt, gội và tạo kiểu nhanh', 1),
    (N'Gội đầu dưỡng sinh', 180000.00, 60, N'Gội đầu, massage cổ vai gáy và thư giãn da đầu', 1),
    (N'Nhuộm tóc thời trang', 750000.00, 150, N'Tư vấn màu, nhuộm và hấp khóa màu', 1),
    (N'Uốn setting', 950000.00, 180, N'Uốn tạo kiểu với thuốc dưỡng chuyên sâu', 1),
    (N'Duỗi phục hồi', 850000.00, 150, N'Duỗi thẳng kết hợp phục hồi keratin', 1),
    (N'Hấp dầu phục hồi', 300000.00, 75, N'Phục hồi tóc khô xơ, hư tổn nhẹ', 1),
    (N'Chăm sóc da cơ bản', 350000.00, 75, N'Làm sạch, xông hơi, đắp mặt nạ và dưỡng da', 1),
    (N'Chăm sóc da chuyên sâu', 650000.00, 105, N'Lấy nhân mụn, điện di tinh chất và phục hồi da', 1),
    (N'Massage body thư giãn', 500000.00, 90, N'Massage toàn thân giảm căng cơ', 1),
    (N'Massage đá nóng', 680000.00, 100, N'Massage body kết hợp đá nóng', 1),
    (N'Làm móng gel', 250000.00, 60, N'Cắt da, sơn gel và dưỡng móng', 1),
    (N'Nail art cao cấp', 450000.00, 90, N'Thiết kế móng nghệ thuật theo yêu cầu', 1),
    (N'Nối mi classic', 380000.00, 75, N'Nối mi tự nhiên, nhẹ mắt', 1),
    (N'Nối mi volume', 520000.00, 90, N'Nối mi dày, nổi bật hơn', 1),
    (N'Tẩy tế bào chết body', 420000.00, 80, N'Làm sạch và dưỡng ẩm toàn thân', 1),
    (N'Triệt lông vùng nhỏ', 300000.00, 45, N'Triệt lông công nghệ ánh sáng cho vùng nhỏ', 1),
    (N'Triệt lông vùng lớn', 850000.00, 90, N'Triệt lông công nghệ ánh sáng cho vùng lớn', 1),
    (N'Combo tóc và nail', 620000.00, 120, N'Cắt gội tạo kiểu và sơn gel', 1),
    (N'Combo spa thư giãn', 980000.00, 150, N'Chăm sóc da cơ bản và massage body', 1),
    (N'Dịch vụ ngừng bán mẫu', 100000.00, 30, N'Dữ liệu mẫu cho trạng thái không hoạt động', 0)
) AS source ([name], [price], [duration_minutes], [description], [is_active])
ON target.[name] = source.[name]
WHEN MATCHED THEN
    UPDATE SET [price] = source.[price], [duration_minutes] = source.[duration_minutes], [description] = source.[description], [is_active] = source.[is_active]
WHEN NOT MATCHED THEN
    INSERT ([name], [price], [duration_minutes], [description], [is_active])
    VALUES (source.[name], source.[price], source.[duration_minutes], source.[description], source.[is_active]);
GO

DECLARE @Names TABLE ([rn] int IDENTITY(1,1), [full_name] nvarchar(255), [gender] nvarchar(10));
INSERT INTO @Names ([full_name], [gender]) VALUES
(N'Nguyễn Minh Anh', N'female'), (N'Trần Hoàng Nam', N'male'), (N'Lê Thanh Trúc', N'female'), (N'Phạm Gia Huy', N'male'),
(N'Võ Kim Ngân', N'female'), (N'Đặng Quốc Bảo', N'male'), (N'Bùi Phương Linh', N'female'), (N'Hoàng Nhật Minh', N'male'),
(N'Ngô Thảo Vy', N'female'), (N'Đỗ Hải Đăng', N'male'), (N'Phan Khánh Linh', N'female'), (N'Vũ Đức Anh', N'male'),
(N'Đinh Mai Chi', N'female'), (N'Mai Tuấn Kiệt', N'male'), (N'Lâm Bảo Ngọc', N'female'), (N'Cao Minh Quân', N'male'),
(N'Trịnh Ngọc Hà', N'female'), (N'Hồ Anh Khoa', N'male'), (N'Nguyễn Hà My', N'female'), (N'Trần Duy Phúc', N'male'),
(N'Lê Bích Ngọc', N'female'), (N'Phạm Minh Khôi', N'male'), (N'Võ Hồng Nhung', N'female'), (N'Đặng Thiên Ân', N'other'),
(N'Bùi Ngọc Mai', N'female'), (N'Hoàng Gia Bảo', N'male'), (N'Ngô Phương Uyên', N'female'), (N'Đỗ Quang Hưng', N'male'),
(N'Phan Diệu Linh', N'female'), (N'Vũ Thành Đạt', N'male');

DECLARE @i int = 1;
DECLARE @nameCount int = (SELECT COUNT(*) FROM @Names);
WHILE @i <= 60
BEGIN
    DECLARE @phone varchar(20) = '09' + RIGHT('00000000' + CAST(10000000 + @i AS varchar(8)), 8);
    DECLARE @fullName nvarchar(255) = (SELECT [full_name] FROM @Names WHERE [rn] = ((@i - 1) % @nameCount) + 1);
    DECLARE @gender nvarchar(10) = (SELECT [gender] FROM @Names WHERE [rn] = ((@i - 1) % @nameCount) + 1);
    DECLARE @suffix nvarchar(10) = RIGHT('000' + CAST(@i AS nvarchar(3)), 3);

    IF NOT EXISTS (SELECT 1 FROM [dbo].[customers] WHERE [phone] = @phone)
    BEGIN
        INSERT INTO [dbo].[customers] ([full_name], [phone], [email], [gender], [loyalty_points], [note], [created_at])
        VALUES (
            @fullName + N' ' + @suffix,
            @phone,
            N'khach' + @suffix + N'@example.com',
            @gender,
            (@i * 13) % 520,
            CASE WHEN @i % 5 = 0 THEN N'[SEED] Khách ưu tiên tư vấn trước khi làm dịch vụ' ELSE N'[SEED] Khách hàng dữ liệu mẫu' END,
            DATEADD(day, -(@i + 20), SYSDATETIME())
        );
    END

    SET @i += 1;
END
GO

UPDATE [dbo].[customers]
SET [username] = N'customer01'
WHERE [phone] = '0910000001' AND ([username] IS NULL OR [username] = N'customer01');

UPDATE [dbo].[customers]
SET [username] = N'customer02'
WHERE [phone] = '0910000002' AND ([username] IS NULL OR [username] = N'customer02');
GO

DECLARE @SeedCustomerIds TABLE ([rn] int, [id] int, [phone] varchar(20));
INSERT INTO @SeedCustomerIds ([rn], [id], [phone])
SELECT ROW_NUMBER() OVER (ORDER BY [phone]), [id], [phone]
FROM [dbo].[customers]
WHERE [phone] BETWEEN '0910000001' AND '0910000060';

DECLARE @SeedServiceIds TABLE ([rn] int, [id] int);
INSERT INTO @SeedServiceIds ([rn], [id])
SELECT ROW_NUMBER() OVER (ORDER BY [id]), [id]
FROM [dbo].[services]
WHERE [is_active] = 1;

DECLARE @SeedRoomIds TABLE ([rn] int, [id] int);
INSERT INTO @SeedRoomIds ([rn], [id])
SELECT ROW_NUMBER() OVER (ORDER BY [id]), [id]
FROM [dbo].[service_rooms]
WHERE [is_active] = 1;

DECLARE @customerCount int = (SELECT COUNT(*) FROM @SeedCustomerIds);
DECLARE @serviceCount int = (SELECT COUNT(*) FROM @SeedServiceIds);
DECLARE @roomCount int = (SELECT COUNT(*) FROM @SeedRoomIds);
DECLARE @n int = 1;

WHILE @n <= 120 AND @customerCount > 0 AND @serviceCount > 0
BEGIN
    DECLARE @customerId int = (SELECT [id] FROM @SeedCustomerIds WHERE [rn] = ((@n - 1) % @customerCount) + 1);
    DECLARE @serviceId int = (SELECT [id] FROM @SeedServiceIds WHERE [rn] = ((@n - 1) % @serviceCount) + 1);
    DECLARE @roomId int = CASE WHEN @roomCount = 0 THEN NULL ELSE (SELECT [id] FROM @SeedRoomIds WHERE [rn] = ((@n - 1) % @roomCount) + 1) END;
    DECLARE @status nvarchar(50) =
        CASE
            WHEN @n % 12 = 0 THEN N'cancelled'
            WHEN @n % 7 = 0 THEN N'completed'
            WHEN @n % 5 = 0 THEN N'awaiting_payment'
            WHEN @n % 4 = 0 THEN N'in_progress'
            WHEN @n % 3 = 0 THEN N'confirmed'
            ELSE N'pending'
        END;
    DECLARE @appointmentTime datetime2(6) = DATEADD(
        minute,
        ((@n % 8) * 30),
        DATEADD(hour, 9, DATEADD(day, (@n % 50) - 25, CONVERT(datetime2(6), CONVERT(date, SYSDATETIME()))))
    );
    DECLARE @appointmentNote nvarchar(500) = N'[SEED] Lịch hẹn mẫu #' + RIGHT('000' + CAST(@n AS nvarchar(3)), 3);

    IF NOT EXISTS (SELECT 1 FROM [dbo].[appointments] WHERE [note] = @appointmentNote)
    BEGIN
        INSERT INTO [dbo].[appointments] ([customer_id], [service_id], [room_id], [appointment_time], [status], [note], [created_at])
        VALUES (@customerId, @serviceId, @roomId, @appointmentTime, @status, @appointmentNote, DATEADD(day, -(@n % 40), SYSDATETIME()));
    END

    SET @n += 1;
END
GO

INSERT INTO [dbo].[payments] ([appointment_id], [amount], [payment_stage], [payment_method], [payment_status], [paid_at])
SELECT
    a.[id],
    s.[price],
    N'balance',
    CASE
        WHEN a.[id] % 4 = 0 THEN N'cash'
        WHEN a.[id] % 4 = 1 THEN N'bank_transfer'
        WHEN a.[id] % 4 = 2 THEN N'momo'
        ELSE N'card'
    END,
    CASE
        WHEN a.[status] IN (N'paid', N'completed') THEN N'paid'
        WHEN a.[status] = N'cancelled' AND a.[id] % 2 = 0 THEN N'refunded'
        ELSE N'unpaid'
    END,
    CASE WHEN a.[status] IN (N'paid', N'completed') THEN DATEADD(hour, 1, a.[appointment_time]) ELSE NULL END
FROM [dbo].[appointments] a
JOIN [dbo].[services] s ON s.[id] = a.[service_id]
WHERE a.[note] LIKE N'[[]SEED] Lịch hẹn mẫu #%'
  AND NOT EXISTS (SELECT 1 FROM [dbo].[payments] p WHERE p.[appointment_id] = a.[id]);
GO

INSERT INTO [dbo].[sms_logs] ([appointment_id], [phone], [message], [sent_at], [status])
SELECT
    a.[id],
    c.[phone],
    N'[SEED] Salon xác nhận lịch hẹn của quý khách vào ' + CONVERT(nvarchar(16), a.[appointment_time], 120),
    DATEADD(hour, -2, a.[appointment_time]),
    CASE WHEN a.[id] % 17 = 0 THEN N'failed' ELSE N'success' END
FROM [dbo].[appointments] a
JOIN [dbo].[customers] c ON c.[id] = a.[customer_id]
WHERE a.[note] LIKE N'[[]SEED] Lịch hẹn mẫu #%'
  AND a.[status] IN (N'confirmed', N'in_progress', N'awaiting_payment', N'completed', N'paid')
  AND NOT EXISTS (
      SELECT 1
      FROM [dbo].[sms_logs] l
      WHERE l.[appointment_id] = a.[id]
        AND l.[message] LIKE N'[[]SEED] Salon xác nhận lịch hẹn%'
  );
GO

SELECT
    (SELECT COUNT(*) FROM [dbo].[app_users]) AS users_count,
    (SELECT COUNT(*) FROM [dbo].[customers] WHERE [note] LIKE N'[[]SEED]%') AS seed_customers_count,
    (SELECT COUNT(*) FROM [dbo].[services]) AS services_count,
    (SELECT COUNT(*) FROM [dbo].[service_rooms]) AS rooms_count,
    (SELECT COUNT(*) FROM [dbo].[appointments] WHERE [note] LIKE N'[[]SEED] Lịch hẹn mẫu #%') AS seed_appointments_count,
    (SELECT COUNT(*) FROM [dbo].[payments] p JOIN [dbo].[appointments] a ON a.[id] = p.[appointment_id] WHERE a.[note] LIKE N'[[]SEED] Lịch hẹn mẫu #%') AS seed_payments_count,
    (SELECT COUNT(*) FROM [dbo].[sms_logs] l JOIN [dbo].[appointments] a ON a.[id] = l.[appointment_id] WHERE a.[note] LIKE N'[[]SEED] Lịch hẹn mẫu #%') AS seed_sms_logs_count;
GO
