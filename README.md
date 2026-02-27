# 🏥 MediBook — Hệ thống đặt lịch khám bệnh trực môn

**MediBook** là một nền tảng quản lý y tế hiện đại, giúp kết nối bệnh nhân và bác sĩ một cách nhanh chóng và chuyên nghiệp. Ứng dụng cung cấp đầy đủ các tính năng từ đặt lịch, quản lý hồ sơ bệnh án đến quản trị hệ thống toàn diện.

---

## 🚀 Khởi chạy nhanh với Docker (Khuyên dùng)

Hệ thống đã được đóng gói hoàn chỉnh bằng Docker Compose, bao gồm: Cơ sở dữ liệu PostgreSQL, Backend Spring Boot và Frontend Nginx.

### Yêu cầu hệ thống
- Docker và Docker Compose đã được cài đặt.

### Các bước thực hiện
1. **Sao chép tệp cấu hình**:
   ```bash
   cp .env.example .env
   ```
2. **Khởi chạy hệ thống**:
   ```bash
   docker-compose up -d --build
   ```
3. **Truy cập ứng dụng**:
   - Frontend: [http://localhost](http://localhost)
   - Swagger API Docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🛠 Kiến trúc hệ thống

Dự án được xây dựng theo mô hình Microservices-ready với các tầng công nghệ hiện đại:

- **Frontend**: HTML5, Vanilla CSS (MediBook Design System), JavaScript (ES6+). Được phục vụ bởi **Nginx**.
- **Backend**: **Spring Boot 3.3**, Spring Security (JWT), Spring Data JPA.
- **Database**: **PostgreSQL 16** cho lưu trữ dữ liệu bền vững.
- **Migration**: Flyway được sử dụng để quản lý phiên bản cơ sở dữ liệu.

---

## 🔑 Tài khoản mặc định

Sau khi hệ thống khởi chạy, bạn có thể đăng nhập bằng các tài khoản mẫu sau:

| Vai trò | Email | Mật khẩu |
| :--- | :--- | :--- |
| **Quản trị viên** | `admin@medibook.vn` | `admin123` |
| **Bác sĩ** | `doctor@medibook.vn` | `doctor123` |
| **Bệnh nhân** | `user@medibook.vn` | `user123` |

---

## ✅ Danh sách kiểm tra (Test Checklist)

### 1. Luồng Bệnh nhân
- [ ] Đăng ký tài khoản và Đăng nhập.
- [ ] Tìm kiếm bác sĩ theo chuyên khoa hoặc tên.
- [ ] Đặt lịch khám: Chọn chuyên khoa → Chọn ngày/giờ → Xác nhận.
- [ ] Xem danh sách lịch hẹn và chi tiết ca khám.
- [ ] Hủy lịch hẹn (khi trạng thái là Chờ xác nhận).

### 2. Luồng Bác sĩ
- [ ] Xem thống kê tổng quan (Dashboard).
- [ ] Quản lý lịch khám: Xác nhận (Confirm) hoặc Hoàn thành (Complete).
- [ ] Ghi chú chuyên môn cho ca khám đã hoàn thành.

### 3. Luồng Quản trị (Admin)
- [ ] Xem KPIs toàn hệ thống (Tổng người dùng, bác sĩ, lịch hẹn).
- [ ] Quản lý danh sách bác sĩ (Thêm mới bác sĩ chuyên khoa).
- [ ] Quản lý người dùng (Bật/Tắt tài khoản).
- [ ] Quản lý toàn bộ lịch hẹn hệ thống.

---

## 📄 Giấy phép & Bảo mật
Dự án được thực hiện với các tiêu chuẩn bảo mật:
- Mật khẩu được mã hóa bằng **BCrypt**.
- Truy cập API được bảo vệ bởi **JWT**.
- Phân quyền nghiêm ngặt dựa trên vai trò (RBAC).

© 2026 **MediBook Project**. All rights reserved.
