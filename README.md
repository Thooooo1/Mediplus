# MediBook - He thong quan ly dat lich kham benh truc tuyen

MediBook la mot giai phap cong nghe toan dien giup ket noi benh nhan va bac si. He thong cho phep tu dong hoa quy trinh dat lich, giam thieu sai sot va toi uu hoa thoi gian cho ca phong kham va nguoi benh.

## Muc tieu du an
Du an duoc thiet ke de mang lai trai nghiem nguoi dung tot nhat voi toc do phan hoi nhanh, bao mat thong tin va he thong thong bao thoi gian thuc qua email.

## Kien truc cong nghe

### Backend (Java Spring Boot)
- Framework: Spring Boot 3.2.x
- Ngon ngu: Java 21
- Quan ly phu thuoc: Maven
- Bao mat: Spring Security, JWT (Json Web Token), BCrypt Password Encoder
- Co so du lieu: PostgreSQL
- Dich vu Email: Resend API (HTTP Client)
- Thu vien ho tro: Lombok, JPA, Hibernate

### Frontend (Modern Web)
- Cong nghe: HTML5, CSS3, JavaScript (ES6+)
- Cong cu build: Vite
- CSS: Custom CSS (theo phong cach hien dai, responsive)
- Tuong tac: Fetch API de ket noi voi Backend

## Cac tinh nang chi tiet

### Doi voi Benh nhan
- Dang ky, dang nhap va quan ly thong tin ca nhan.
- Tim kiem bac si theo ten hoac theo chuyen khoa y te.
- Xem lich trong cua bac si trong tung ngay cu the.
- Dat lich hen truc tuyen va nhan email xac nhan ngay lap tuc.
- Quan ly lich su cac cuoc hen da dat.

### Doi voi Bac si
- Xem danh sach lich hen can kham trong ngay.
- Cap nhat trang thai cuoc hen (Xac nhan, Hoan thanh, Huy).
- Quan ly ho so ca nhan va thoi gian lam viec tai phong kham.

### Doi voi Quan tri vien (Admin)
- Quan ly toan bo nguoi dung (Bac si, Benh nhan).
- Them moi bac si va phan vao cac chuyen khoa tuong ung.
- Xem thong ke tong quan ve hoat dong cua he thong.
- Kiem soat trang thai hoat dong của cac tai khoan.

## Huong dan cai dat va trien khai

### Yeu cau he thong
- JDK 21 tro len.
- Maven 3.8+.
- PostgreSQL 15+.
- Node.js (neu muon su dung Vite cho frontend).

### Cac buoc thuc hien
1. Tai ma nguon:
   git clone https://github.com/Thooooo1/Mediplus.git

2. Cau hinh co so du lieu:
   Tao mot database ten la 'medibook' trong PostgreSQL.

3. Cau hinh bien moi truong:
   Ban can thiet lap cac bien sau (trong file application.yml hoac bien moi truong cua he thong):
   - DB_URL: jdbc:postgresql://localhost:5432/medibook
   - DB_USER: ten_dang_nhap_db
   - DB_PASS: mat_khau_db
   - JWT_SECRET: chuoi_ky_tu_bao_mat_tren_32_so
   - RESEND_API_KEY: Key lay tu trang resend.com
   - MAIL_ENABLED: true (de kich hoat gui mail)

4. Chay Backend:
   cd backend
   mvn spring-boot:run

5. Chay Frontend:
   Mo folder frontend va chay qua Live Server hoac dung lenh:
   npm install
   npm run dev

## Thong tin demo truc tiep
- Giao dien nguoi dung: https://mediplus.vercel.app
- API Swagger: https://medibook-api-yd85.onrender.com/swagger-ui.html

## Danh sach tai khoan thu nghiem
Vai tro | Email | Mat khau
--- | --- | ---
Quan tri vien | tnguyenanh189@gmail.com | 123
Admin he thong | admin@gmail.com | 123
Bac si | minh.nguyen@gmail.com | 123
Benh nhan | patient1@gmail.com | 123

---
Ban quyen thuoc ve doi ngu phat trien MediBook 2026.
