SET NOCOUNT ON;

USE [booking_system];
GO

MERGE [dbo].[app_users] AS target
USING (VALUES
    ('owner01', '123456', 'OWNER'),
    ('manager01', '123456', 'OWNER'),
    ('staff01', '123456', 'STAFF'),
    ('staff02', '123456', 'STAFF'),
    ('staff03', '123456', 'STAFF'),
    ('staff04', '123456', 'STAFF'),
    ('reception01', '123456', 'STAFF'),
    ('reception02', '123456', 'STAFF'),
    ('customer01', '123456', 'CUSTOMER'),
    ('customer02', '123456', 'CUSTOMER'),
    ('customer03', '123456', 'CUSTOMER'),
    ('customer04', '123456', 'CUSTOMER'),
    ('customer05', '123456', 'CUSTOMER'),
    ('test_customer01', '123456', 'CUSTOMER'),
    ('test_customer02', '123456', 'CUSTOMER'),
    ('test_customer03', '123456', 'CUSTOMER'),
    ('test_customer04', '123456', 'CUSTOMER'),
    ('test_customer05', '123456', 'CUSTOMER')
) AS source ([username], [password], [user_role])
ON target.[username] = source.[username]
WHEN MATCHED THEN
    UPDATE SET
        [password] = source.[password],
        [user_role] = source.[user_role]
WHEN NOT MATCHED THEN
    INSERT ([username], [password], [user_role], [created_at])
    VALUES (source.[username], source.[password], source.[user_role], SYSDATETIME());
GO

MERGE [dbo].[customers] AS target
USING (VALUES
    ('test_customer01', N'Nguyễn Minh Anh', '0901000001', N'nguyen.minh.anh@example.com', N'female', 120, N'Khách quen, thích gội dưỡng sinh'),
    ('test_customer02', N'Trần Hoàng Nam', '0901000002', N'tran.hoang.nam@example.com', N'male', 80, N'Thường đặt lịch cuối tuần'),
    ('test_customer03', N'Lê Thanh Trúc', '0901000003', N'le.thanh.truc@example.com', N'female', 210, N'Ưu tiên stylist nữ'),
    ('test_customer04', N'Phạm Gia Huy', '0901000004', N'pham.gia.huy@example.com', N'male', 45, N'Cần nhắc lịch qua SMS'),
    ('test_customer05', N'Võ Kim Ngân', '0901000005', N'vo.kim.ngan@example.com', N'female', 300, N'Hay dùng combo tóc và nail'),
    (NULL, N'Đặng Quốc Bảo', '0901000006', N'dang.quoc.bao@example.com', N'male', 15, N'Khách mới'),
    (NULL, N'Bùi Phương Linh', '0901000007', N'bui.phuong.linh@example.com', N'female', 160, N'Thích dịch vụ chăm sóc da'),
    (NULL, N'Hoàng Nhật Minh', '0901000008', N'hoang.nhat.minh@example.com', N'male', 70, N'Cắt tóc nam định kỳ'),
    (NULL, N'Ngô Thảo Vy', '0901000009', N'ngo.thao.vy@example.com', N'female', 95, N'Da nhạy cảm'),
    (NULL, N'Đỗ Hải Đăng', '0901000010', N'do.hai.dang@example.com', N'male', 35, N'Ưu tiên khung giờ chiều'),
    (NULL, N'Phan Khánh Linh', '0901000011', N'phan.khanh.linh@example.com', N'female', 185, N'Thích nhuộm tông nâu lạnh'),
    (NULL, N'Vũ Đức Anh', '0901000012', N'vu.duc.anh@example.com', N'male', 60, N'Khách văn phòng'),
    (NULL, N'Đinh Mai Chi', '0901000013', N'dinh.mai.chi@example.com', N'female', 240, N'Hay đặt lịch theo nhóm'),
    (NULL, N'Mai Tuấn Kiệt', '0901000014', N'mai.tuan.kiet@example.com', N'male', 25, N'Khách mới từ quảng cáo'),
    (NULL, N'Lâm Bảo Ngọc', '0901000015', N'lam.bao.ngoc@example.com', N'female', 330, N'Khách VIP'),
    (NULL, N'Cao Minh Quân', '0901000016', N'cao.minh.quan@example.com', N'male', 55, N'Thường thanh toán tiền mặt'),
    (NULL, N'Trịnh Ngọc Hà', '0901000017', N'trinh.ngoc.ha@example.com', N'female', 110, N'Cần tư vấn trước khi làm tóc'),
    (NULL, N'Hồ Anh Khoa', '0901000018', N'ho.anh.khoa@example.com', N'male', 40, N'Thích dịch vụ nhanh'),
    (NULL, N'Nguyễn Hà My', '0901000019', N'nguyen.ha.my@example.com', N'female', 275, N'Ưu tiên sản phẩm phục hồi'),
    (NULL, N'Trần Duy Phúc', '0901000020', N'tran.duy.phuc@example.com', N'male', 65, N'Cắt tóc sau giờ làm'),
    (NULL, N'Lê Bích Ngọc', '0901000021', N'le.bich.ngoc@example.com', N'female', 145, N'Thích nail màu pastel'),
    (NULL, N'Phạm Minh Khôi', '0901000022', N'pham.minh.khoi@example.com', N'male', 20, N'Khách walk-in'),
    (NULL, N'Võ Hồng Nhung', '0901000023', N'vo.hong.nhung@example.com', N'female', 195, N'Thích massage đá nóng'),
    (NULL, N'Đặng Thiên Ân', '0901000024', N'dang.thien.an@example.com', N'other', 90, N'Ưu tiên tư vấn riêng'),
    (NULL, N'Bùi Ngọc Mai', '0901000025', N'bui.ngoc.mai@example.com', N'female', 220, N'Hay mua gói chăm sóc da'),
    (NULL, N'Hoàng Gia Bảo', '0901000026', N'hoang.gia.bao@example.com', N'male', 50, N'Cần giữ lịch đúng giờ'),
    (NULL, N'Ngô Phương Uyên', '0901000027', N'ngo.phuong.uyen@example.com', N'female', 130, N'Tóc khô, cần phục hồi'),
    (NULL, N'Đỗ Quang Hưng', '0901000028', N'do.quang.hung@example.com', N'male', 75, N'Thích combo cắt gội'),
    (NULL, N'Phan Diệu Linh', '0901000029', N'phan.dieu.linh@example.com', N'female', 260, N'Khách thân thiết'),
    (NULL, N'Vũ Thành Đạt', '0901000030', N'vu.thanh.dat@example.com', N'male', 30, N'Khách mới'),
    (NULL, N'Nguyễn Thuỳ Dương', '0901000031', N'nguyen.thuy.duong@example.com', N'female', 155, N'Thích nối mi tự nhiên'),
    (NULL, N'Trần Bảo Châu', '0901000032', N'tran.bao.chau@example.com', N'female', 205, N'Hay đặt lịch buổi sáng'),
    (NULL, N'Lê Quốc Việt', '0901000033', N'le.quoc.viet@example.com', N'male', 85, N'Cắt tóc định kỳ 3 tuần'),
    (NULL, N'Phạm An Nhiên', '0901000034', N'pham.an.nhien@example.com', N'other', 115, N'Ưu tiên không gian yên tĩnh'),
    (NULL, N'Võ Mai Phương', '0901000035', N'vo.mai.phuong@example.com', N'female', 290, N'Thích dịch vụ spa VIP'),
    (NULL, N'Đặng Minh Tâm', '0901000036', N'dang.minh.tam@example.com', N'male', 40, N'Khách gần salon'),
    (NULL, N'Bùi Hải Yến', '0901000037', N'bui.hai.yen@example.com', N'female', 170, N'Cần test màu trước khi nhuộm'),
    (NULL, N'Hoàng Tuấn Anh', '0901000038', N'hoang.tuan.anh@example.com', N'male', 95, N'Thích thanh toán chuyển khoản'),
    (NULL, N'Ngô Khánh An', '0901000039', N'ngo.khanh.an@example.com', N'female', 125, N'Thích chăm sóc móng'),
    (NULL, N'Đỗ Gia Hân', '0901000040', N'do.gia.han@example.com', N'female', 310, N'Khách VIP, ưu tiên phòng riêng')
) AS source ([username], [full_name], [phone], [email], [gender], [loyalty_points], [note])
ON target.[phone] = source.[phone]
WHEN MATCHED THEN
    UPDATE SET
        [username] = source.[username],
        [full_name] = source.[full_name],
        [email] = source.[email],
        [gender] = source.[gender],
        [loyalty_points] = source.[loyalty_points],
        [note] = source.[note]
WHEN NOT MATCHED THEN
    INSERT ([username], [full_name], [phone], [email], [gender], [loyalty_points], [note], [created_at])
    VALUES (source.[username], source.[full_name], source.[phone], source.[email], source.[gender], source.[loyalty_points], source.[note], SYSDATETIME());
GO

MERGE [dbo].[services] AS target
USING (VALUES
    (N'Cắt tóc nữ', 120000.00, 45, N'Tư vấn kiểu tóc, cắt và sấy tạo kiểu', 1),
    (N'Cắt tóc nam', 80000.00, 30, N'Cắt, gội và tạo kiểu nhanh', 1),
    (N'Cắt tóc trẻ em', 70000.00, 25, N'Cắt tóc nhẹ nhàng cho bé', 1),
    (N'Gội đầu thư giãn', 120000.00, 40, N'Gội đầu kết hợp massage da đầu', 1),
    (N'Gội đầu dưỡng sinh', 180000.00, 60, N'Gội đầu, massage cổ vai gáy và thư giãn da đầu', 1),
    (N'Sấy tạo kiểu', 90000.00, 30, N'Sấy phồng, uốn lọn nhẹ hoặc duỗi tự nhiên', 1),
    (N'Nhuộm tóc thời trang', 750000.00, 150, N'Tư vấn màu, nhuộm và hấp khóa màu', 1),
    (N'Nhuộm phủ bạc', 420000.00, 90, N'Nhuộm phủ bạc đều màu, giữ tóc mềm', 1),
    (N'Tẩy tóc', 900000.00, 180, N'Tẩy nền tóc theo cấp độ, có phục hồi', 1),
    (N'Uốn setting', 950000.00, 180, N'Uốn tạo kiểu với thuốc dưỡng chuyên sâu', 1),
    (N'Uốn lạnh', 650000.00, 150, N'Uốn sóng tự nhiên, phù hợp tóc khỏe', 1),
    (N'Duỗi phục hồi', 850000.00, 150, N'Duỗi thẳng kết hợp phục hồi keratin', 1),
    (N'Phục hồi keratin', 700000.00, 120, N'Phục hồi tóc khô xơ bằng keratin', 1),
    (N'Hấp dầu phục hồi', 300000.00, 75, N'Phục hồi tóc khô xơ, hư tổn nhẹ', 1),
    (N'Chăm sóc da cơ bản', 350000.00, 75, N'Làm sạch, xông hơi, đắp mặt nạ và dưỡng da', 1),
    (N'Chăm sóc da chuyên sâu', 650000.00, 105, N'Lấy nhân mụn, điện di tinh chất và phục hồi da', 1),
    (N'Trị mụn chuyên sâu', 780000.00, 120, N'Làm sạch sâu, xử lý mụn và làm dịu da', 1),
    (N'Massage body thư giãn', 500000.00, 90, N'Massage toàn thân giảm căng cơ', 1),
    (N'Massage đá nóng', 680000.00, 100, N'Massage body kết hợp đá nóng', 1),
    (N'Tẩy tế bào chết body', 420000.00, 80, N'Làm sạch và dưỡng ẩm toàn thân', 1),
    (N'Làm móng gel', 250000.00, 60, N'Cắt da, sơn gel và dưỡng móng', 1),
    (N'Nail art cơ bản', 320000.00, 75, N'Trang trí móng đơn giản theo mẫu', 1),
    (N'Nail art cao cấp', 450000.00, 90, N'Thiết kế móng nghệ thuật theo yêu cầu', 1),
    (N'Đắp bột móng', 480000.00, 100, N'Nối móng, định hình và sơn gel', 1),
    (N'Nối mi classic', 380000.00, 75, N'Nối mi tự nhiên, nhẹ mắt', 1),
    (N'Nối mi volume', 520000.00, 90, N'Nối mi dày, nổi bật hơn', 1),
    (N'Dặm mi', 220000.00, 45, N'Dặm lại mi sau 2 đến 3 tuần', 1),
    (N'Triệt lông vùng nhỏ', 300000.00, 45, N'Triệt lông công nghệ ánh sáng cho vùng nhỏ', 1),
    (N'Triệt lông vùng lớn', 850000.00, 90, N'Triệt lông công nghệ ánh sáng cho vùng lớn', 1),
    (N'Combo cắt gội nữ', 180000.00, 60, N'Cắt tóc nữ, gội đầu và sấy tạo kiểu', 1),
    (N'Combo tóc và nail', 620000.00, 120, N'Cắt gội tạo kiểu và sơn gel', 1),
    (N'Combo spa thư giãn', 980000.00, 150, N'Chăm sóc da cơ bản và massage body', 1),
    (N'Gói cô dâu cơ bản', 1500000.00, 180, N'Làm tóc, trang điểm nhẹ và chăm sóc da nhanh', 1),
    (N'Tư vấn tạo kiểu cá nhân', 200000.00, 45, N'Tư vấn kiểu tóc, màu tóc và quy trình chăm sóc', 1),
    (N'Dịch vụ ngừng bán mẫu', 100000.00, 30, N'Dữ liệu mẫu cho trạng thái không hoạt động', 0)
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

SELECT
    (SELECT COUNT(*) FROM [dbo].[app_users]) AS users_count,
    (SELECT COUNT(*) FROM [dbo].[customers]) AS customers_count,
    (SELECT COUNT(*) FROM [dbo].[services]) AS services_count;
GO
