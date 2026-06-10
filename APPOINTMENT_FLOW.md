# Logic lịch hẹn Salon Manager

Tài liệu này mô tả luồng chính của phần lịch hẹn để test theo 3 vai trò: admin, nhân viên và khách hàng.

## 1. Nguyên tắc chung

- Hệ thống dùng giờ hiện tại của máy đang chạy app/server.
- Không được đặt lịch ở thời điểm trong quá khứ.
- Giờ làm việc cố định: `08:00 - 20:00`.
- Slot lịch được chia theo mỗi `30 phút`.
- Thời lượng dịch vụ được làm tròn lên theo block 30 phút.
  - Ví dụ dịch vụ 45 phút sẽ chiếm slot 60 phút.
  - Dịch vụ 30 phút chiếm slot 30 phút.
- Một phòng/khu vực không được có 2 lịch bị trùng thời gian nếu lịch đó đã giữ chỗ.

## 2. Trạng thái lịch hẹn

Hệ thống dùng các trạng thái sau:

| Trạng thái | Ý nghĩa |
|---|---|
| Chờ đặt cọc | Lịch mới tạo, chưa giữ chỗ chắc chắn |
| Đã giữ chỗ | Khách đã đặt cọc, salon giữ phòng và khung giờ |
| Đang phục vụ | Khách đang được làm dịch vụ |
| Chờ thanh toán | Dịch vụ đã xong, còn cần thu phần còn lại |
| Hoàn thành | Đã làm xong và thu đủ tiền |
| Đã hủy | Lịch đã bị hủy |

## 3. Luồng chuẩn của một lịch hẹn

```text
Tạo lịch
→ Chờ đặt cọc

Khách đặt cọc
→ Đã giữ chỗ

Nhân viên/admin bắt đầu phục vụ
→ Đang phục vụ

Nhân viên/admin kết thúc dịch vụ
→ Chờ thanh toán

Nhân viên/admin bấm Hoàn thành, hệ thống thu phần còn lại
→ Hoàn thành
```

Nếu khách hoặc salon hủy:

```text
Chờ đặt cọc / Đã giữ chỗ
→ Đã hủy
```

## 4. Logic đặt cọc và giữ chỗ

- Khi lịch vừa tạo, trạng thái là `Chờ đặt cọc`.
- Ở trạng thái `Chờ đặt cọc`, lịch chưa được xem là giữ chỗ chắc chắn.
- Khi khách đặt cọc thành công:
  - Hệ thống tạo payment giai đoạn `deposit`.
  - Số tiền cọc = `20%` giá dịch vụ.
  - Trạng thái lịch chuyển sang `Đã giữ chỗ`.
  - Phòng/khu vực và khung giờ bắt đầu được khóa để tránh trùng lịch.

## 5. Logic thanh toán phần còn lại

- Chỉ thu phần còn lại khi lịch đang ở trạng thái `Chờ thanh toán`.
- Khi bấm `Hoàn thành` ở trạng thái `Chờ thanh toán`:
  - Hệ thống tạo payment giai đoạn `balance`.
  - Số tiền thu = tổng tiền dịch vụ - số tiền đã thu.
  - Khi đã thu đủ, trạng thái chuyển sang `Hoàn thành`.

## 6. Logic chống trùng lịch

Một lịch sẽ chặn phòng/khu vực nếu trạng thái là:

- `Đã giữ chỗ`
- `Đang phục vụ`
- `Chờ thanh toán`
- `Hoàn thành`

Một lịch không chặn phòng/khu vực nếu trạng thái là:

- `Chờ đặt cọc`
- `Đã hủy`

Khi tạo hoặc cập nhật lịch:

- Nếu chọn cùng phòng/khu vực.
- Và thời gian bị giao nhau với một lịch đã giữ chỗ.
- Hệ thống sẽ báo trùng lịch và không cho lưu.

Ví dụ:

```text
Lịch A:
Phòng 1, 09:00 - 10:00, Đã giữ chỗ

Không được tạo lịch B:
Phòng 1, 09:30 - 10:00
Phòng 1, 09:30 - 10:30
Phòng 1, 08:30 - 09:30
```

Nhưng có thể tạo:

```text
Phòng 1, 10:00 - 10:30
Phòng 2, 09:30 - 10:00
```

## 7. Quyền thao tác theo vai trò

### Admin / chủ cửa hàng

Admin có quyền vận hành đầy đủ:

- Tạo lịch.
- Sửa lịch.
- Hủy lịch.
- Bắt đầu phục vụ.
- Kết thúc dịch vụ.
- Thu phần còn lại bằng nút `Hoàn thành`.
- Gửi nhắc lịch.
- Quản lý khách hàng, dịch vụ, khu vực/phòng và báo cáo.

Action theo trạng thái:

| Trạng thái | Action |
|---|---|
| Chờ đặt cọc | Sửa, Hủy lịch |
| Đã giữ chỗ | Sửa, Hủy lịch, Bắt đầu phục vụ, Nhắc lịch |
| Đang phục vụ | Sửa, Kết thúc dịch vụ |
| Chờ thanh toán | Sửa, Hoàn thành |
| Hoàn thành | Sửa ghi chú |
| Đã hủy | Sửa ghi chú |

### Nhân viên

Nhân viên chỉ tập trung vận hành:

- Xem và xử lý lịch hẹn.
- Tạo lịch hộ khách.
- Sửa lịch khi cần.
- Hủy lịch.
- Bắt đầu phục vụ.
- Kết thúc dịch vụ.
- Hoàn thành lịch bằng cách thu phần còn lại.
- Gửi nhắc lịch.
- Xem/thêm/cập nhật khách hàng.

Nhân viên không được:

- Xem tổng quan doanh thu.
- Quản lý dịch vụ.
- Quản lý khu vực/phòng.
- Xem báo cáo.
- Quản lý tài khoản người dùng.
- Xóa khách hàng.
- Sửa điểm tích lũy.

### Khách hàng

Khách hàng chỉ thao tác trên lịch của chính mình:

- Xem lịch của tôi.
- Đặt lịch.
- Sửa lịch khi còn `Chờ đặt cọc`.
- Hủy lịch khi còn `Chờ đặt cọc`.
- Đặt cọc để giữ chỗ.
- Xem/cập nhật tài khoản cá nhân.

Action theo trạng thái:

| Trạng thái | Action |
|---|---|
| Chờ đặt cọc | Sửa, Hủy lịch, Đặt cọc |
| Đã giữ chỗ | Xem lịch |
| Đang phục vụ | Xem lịch |
| Chờ thanh toán | Xem lịch |
| Hoàn thành | Xem lịch |
| Đã hủy | Xem lịch |

## 8. Tài khoản khách hàng

Khi admin hoặc nhân viên thêm khách hàng mới:

- Hệ thống tạo thêm tài khoản trong `app_users`.
- Username được tạo từ họ tên đã normalize.
  - Ví dụ: `Nguyễn Minh Anh` → `nguyenminhanh`.
  - Nếu trùng, hệ thống thêm số phía sau: `nguyenminhanh2`.
- Mật khẩu mặc định: `123456`.
- Role mặc định: `CUSTOMER`.

Khách có thể dùng tài khoản này để đăng nhập và đặt lịch/đặt cọc.

## 9. Các case nên test

### Case 1: Khách tạo lịch mới

1. Đăng nhập khách hàng.
2. Tạo lịch ở ngày/giờ tương lai.
3. Kiểm tra lịch có trạng thái `Chờ đặt cọc`.
4. Bấm `Đặt cọc`.
5. Kiểm tra lịch chuyển sang `Đã giữ chỗ`.

### Case 2: Không cho đặt quá khứ

1. Tạo lịch với ngày hoặc giờ đã qua.
2. Hệ thống phải không cho lưu.

### Case 3: Không cho trùng lịch đã giữ chỗ

1. Tạo lịch A.
2. Đặt cọc lịch A để chuyển sang `Đã giữ chỗ`.
3. Tạo lịch B cùng phòng, thời gian giao với lịch A.
4. Hệ thống phải báo trùng lịch.

### Case 4: Cho phép lịch chờ đặt cọc chưa khóa slot

1. Tạo lịch A nhưng chưa đặt cọc.
2. Tạo lịch B cùng phòng, cùng khung giờ.
3. Hệ thống vẫn có thể cho tạo vì lịch A chưa giữ chỗ.

### Case 5: Nhân viên xử lý lịch

1. Đăng nhập nhân viên.
2. Chọn lịch `Đã giữ chỗ`.
3. Bấm `Bắt đầu phục vụ`.
4. Kiểm tra trạng thái thành `Đang phục vụ`.
5. Bấm `Kết thúc dịch vụ`.
6. Kiểm tra trạng thái thành `Chờ thanh toán`.
7. Bấm `Hoàn thành`.
8. Kiểm tra trạng thái thành `Hoàn thành`.

### Case 6: Tạo khách hàng tự sinh tài khoản

1. Đăng nhập admin hoặc nhân viên.
2. Thêm khách hàng mới.
3. Đăng xuất.
4. Đăng nhập bằng username sinh từ họ tên và mật khẩu `123456`.
5. Kiểm tra khách đăng nhập được.
