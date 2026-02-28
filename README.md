# MediBook - Nen tang Dat lich Kham benh truc tuyen

Du an nay la mot he thong quan ly lich hen y te (Clinic Management System) hoan chinh, duoc xay dung de giai quyet bai toan dat lich giua Benh nhan va Bac si mot cach nhanh chong nhat. Du an tap trung vao tinh thuc te, giao dien hien dai va quy trinh thong bao tu dong (Email).

## Tai sao du an nay ton tai?
He thong giup loai bo quy trinh dat lich thu cong qua dien thoai. Benh nhan co the thay chinh xac khung gio trong cua bac si va nhan email xac nhan ngay lap tuc, trong khi bac si co bang dieu khien rieng de quan ly ca kham trong ngay.

## Tech Stack
He thong duoc thiet ke theo kien truc Micro-monolith (tach biet Front-End va Back-End):

### Back-End
- Java 21 + Spring Boot 3.x: Framework cot loi.
- Spring Security + JWT: Bao mat da tang, khong dung Session, xac thuc qua Token.
- PostgreSQL: Co so du lieu quan trong, luu tru lich hen va ho su bac si.
- Resend API: He thong gui Email thong bao (xac nhan lich, bao lich moi cho admin).
- Lombok & MapStruct: Code gon gang, giam thieu boilerplate.

### Front-End
- HTML5, CSS3, Vanilla JS: Khong dung React/Vue de toi uu toc do tai va giu su don gian.
- Vite (Build tool): Dong goi tai nguyen nhanh chong.
- Tailwind-style CSS: Su dung cac class tien ich de giao dien trong premium va hien dai.

## Tinh nang noi bat
- Tim kiem thong minh: Loc bac si theo chuyen khoa, kinh nghiem.
- Dat lich Real-time: Khoa khung gio ngay khi co nguoi dat de tranh trung lich.
- Thong bao Email: Gui mail xac nhan cho benh nhan va thong bao cho bac si/admin.
- Bang dieu khien Bac si: Quan ly ca kham, cap nhat trang thai "Hoan thanh" hoac "Huy".
- Quan tri vien (Admin): Quan ly danh sach bac si, chuyen khoa va theo doi toan bo he thong.

## Cai dat va Chay duoi may (Local)
Neu ban muon voc vach ma nguon duoi local, hay lam theo cac buoc:

1. Clone project:
   git clone https://github.com/Thooooo1/Mediplus.git

2. Cau hinh bien moi truong: Tao file .env o root hoac cau hinh trong IDE:
   - DB_URL: Link Postgres cua ban.
   - JWT_SECRET: Chuoi bao mat tuy y (it nhat 32 ky tu).
   - RESEND_API_KEY: Lay tu Resend.com.
   - MAIL_ENABLED: Cai thanh true de bat gui mail.

3. Chay Back-End:
   cd backend
   ./mvnw spring-boot:run

4. Chay Front-End: Mo file index.html trong thu muc frontend bang Live Server hoac deploy len Vercel/Netlify.

## Demo va API Documentation
- Giao dien Web: https://mediplus.vercel.app
- Swagger UI (Danh cho Dev): https://medibook-api-yd85.onrender.com/swagger-ui.html

## Tai khoan Test
Vai tro | Email | Mat khau
--- | --- | ---
Admin chinh | tnguyenanh189@gmail.com | 123
Admin he thong | admin@gmail.com | 123
Bac si | minh.nguyen@gmail.com | 123
Benh nhan | patient1@gmail.com | 123

---
Du an duoc bao tri boi @Thooooo1. Neu thay hay hay cho 5 sao nhe!
