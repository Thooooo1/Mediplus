# MediBook - Hệ thống Đặt lịch Khám bệnh

Đây là hệ thống quản lý và đặt lịch khám bệnh trực tuyến. Trang web giúp bệnh nhân dễ dàng tìm kiếm bác sĩ theo chuyên khoa, chọn ngày giờ và đặt lịch khám. Bác sĩ và admin có trang quản trị riêng để theo dõi lịch hẹn và quản lý toàn bộ nền tảng.

## Cấu trúc dự án
Dự án được thiết kế theo mô hình tách biệt (Frontend và Backend riêng), tiện cho việc quản lý mã nguồn và triển khai:
- **Frontend**: Nằm trong thư mục `frontend`. Chạy hoàn toàn bằng HTML, CSS và JavaScript thuần.
- **Backend**: Nằm trong thư mục `backend`. Xây dựng bằng Java Spring Boot 3, chịu trách nhiệm xử lý API và quản lý cơ sở dữ liệu.

## Công nghệ sử dụng
- **Backend API**: Java 21, Spring Boot (Web, Security, Data JPA, Mail)
- **Database**: PostgreSQL
- **Bảo mật**: Cấp quyền và xác thực qua JWT (JSON Web Token), mã hóa mật khẩu bằng BCrypt.
- **Triển khai (Deployment)**: 
  - Backend & Database chạy trên hệ thống của Render.
  - Frontend được host trên nền tảng Vercel.

## Hướng dẫn xem Demo trực tiếp
Dự án hiện đã được đưa lên mạng đầy đủ, bạn không cần phải cài đặt phức tạp ở dưới máy tính cá nhân.
- **Cổng Giao Diện Web (Frontend)**: [Gắn link Vercel của bạn vào đây]
- **Tài liệu API Backend (Swagger)**: [https://medibook-api-yd85.onrender.com/swagger-ui.html](https://medibook-api-yd85.onrender.com/swagger-ui.html)

## Tài khoản dùng thử
Bạn có thể dùng các tài khoản mình đã tạo sẵn dưới đây để test thử các chức năng:

- **Admin**: `admin@medibook.vn` / Mật khẩu: `admin123`
- **Bác sĩ**: `doctor@medibook.vn` / Mật khẩu: `doctor123`
- **Bệnh nhân**: `user@medibook.vn` / Mật khẩu: `user123`

*(Hoặc bạn có thể tự bấm **Đăng ký** để tạo một tài khoản Bệnh nhân hoàn toàn mới).*

## Các chức năng chính
- **Bệnh nhân**: Tìm bác sĩ, xem chi tiết giờ trống, đặt lịch. Hệ thống tự động gửi email thông báo xác nhận.
- **Bác sĩ**: Xem được danh sách bệnh nhân đã đặt lịch với mình hôm nay, bấm hoàn thành ca khám và nhập ghi chú bệnh án.
- **Quản trị viên (Admin)**: Xem thống kê tổng số user/lịch hẹn, thêm bác sĩ mới vào hệ thống, khóa tài khoản người dùng vi phạm.

---
*Phát triển năm 2026. Mọi thắc mắc vui lòng liên hệ tác giả.*
