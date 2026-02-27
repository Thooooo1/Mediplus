-- ============================================================
-- MediBook PostgreSQL - Schema DDL + Seed Data
-- ============================================================

-- 1. TABLES
CREATE TABLE IF NOT EXISTS specialties (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS app_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(190) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  phone VARCHAR(30),
  password_hash VARCHAR(120) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS doctor_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES app_users(id),
  specialty_id UUID NOT NULL REFERENCES specialties(id),
  title VARCHAR(255),
  country VARCHAR(255),
  clinic_name VARCHAR(255),
  bio VARCHAR(1000),
  years_experience INT,
  consult_fee_vnd BIGINT,
  avatar_url VARCHAR(500),
  rating DOUBLE PRECISION,
  rating_count INT
);

CREATE TABLE IF NOT EXISTS working_hours (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctor_profiles(id),
  day_of_week SMALLINT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  slot_minutes SMALLINT NOT NULL DEFAULT 30,
  break_start TIME,
  break_end TIME,
  CONSTRAINT uq_working_hours_doctor_day UNIQUE (doctor_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS time_slots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctor_profiles(id),
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_slot_doctor_start UNIQUE (doctor_id, start_at)
);

CREATE TABLE IF NOT EXISTS appointments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctor_profiles(id),
  patient_id UUID NOT NULL REFERENCES app_users(id),
  time_slot_id UUID NOT NULL UNIQUE REFERENCES time_slots(id),
  status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
  patient_note VARCHAR(1000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  cancelled_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id UUID NOT NULL REFERENCES app_users(id),
  expires_at TIMESTAMPTZ NOT NULL,
  used BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS chat_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctor_profiles(id),
  patient_id UUID NOT NULL REFERENCES app_users(id),
  sender VARCHAR(10) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS medical_records (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  doctor_id UUID NOT NULL REFERENCES doctor_profiles(id),
  patient_id UUID NOT NULL REFERENCES app_users(id),
  diagnosis VARCHAR(255) NOT NULL,
  notes VARCHAR(2000),
  visit_date TIMESTAMPTZ NOT NULL,
  type VARCHAR(50),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- 2. SEED DATA
-- ============================================================
-- BCrypt hash of "Password@123"
-- $2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W

-- 2a) 12 Specialties
INSERT INTO specialties (id, code, name) VALUES
('a0000001-0000-0000-0000-000000000001','CARDIO','Tim mạch'),
('a0000001-0000-0000-0000-000000000002','DERMA','Da liễu'),
('a0000001-0000-0000-0000-000000000003','DENTAL','Nha khoa'),
('a0000001-0000-0000-0000-000000000004','ENT','Tai mũi họng'),
('a0000001-0000-0000-0000-000000000005','BEAUTY','Thẩm mỹ'),
('a0000001-0000-0000-0000-000000000006','NEURO','Thần kinh'),
('a0000001-0000-0000-0000-000000000007','ORTHO','Chỉnh hình - Cơ xương khớp'),
('a0000001-0000-0000-0000-000000000008','PEDIA','Nhi khoa'),
('a0000001-0000-0000-0000-000000000009','GASTRO','Tiêu hóa'),
('a0000001-0000-0000-0000-00000000000a','OPHTHAL','Nhãn khoa'),
('a0000001-0000-0000-0000-00000000000b','OBGYN','Sản phụ khoa'),
('a0000001-0000-0000-0000-00000000000c','ONCOL','Ung bướu')
ON CONFLICT (code) DO NOTHING;

-- 2b) 1 Admin
INSERT INTO app_users (id, email, full_name, password_hash, role, enabled) VALUES
('b0000001-0000-0000-0000-000000000001','admin@medibook.vn','Admin','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','ADMIN',true)
ON CONFLICT (email) DO NOTHING;

-- 2c) 26 Doctor Users
INSERT INTO app_users (id, email, full_name, password_hash, role, enabled) VALUES
('b0000001-0000-0000-0000-000000000d01','minh.nguyen@medibook.vn','Nguyễn Văn Minh','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d02','huong.tran@medibook.vn','Trần Thị Hương','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d03','david.miller@medibook.vn','David Miller','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d04','trang.pham@medibook.vn','Phạm Thị Trang','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d05','hans.muller@medibook.vn','Hans Müller','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d06','nam.le@medibook.vn','Lê Hoàng Nam','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d07','yuki.tanaka@medibook.vn','Tanaka Yuki','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d08','hai.vu@medibook.vn','Vũ Đức Hải','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d09','seojun.kim@medibook.vn','Kim Seo-jun','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d10','linh.hoang@medibook.vn','Hoàng Thị Linh','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d11','jiwoo.park@medibook.vn','Park Ji-woo','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d12','tuan.dang@medibook.vn','Đặng Quốc Tuấn','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d13','thomas.schneider@medibook.vn','Thomas Schneider','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d14','long.bui@medibook.vn','Bùi Thanh Long','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d15','james.williams@medibook.vn','James Williams','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d16','hiroshi.sato@medibook.vn','Sato Hiroshi','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d17','hoa.ngo@medibook.vn','Ngô Thị Hoa','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d18','weilin.lim@medibook.vn','Lim Wei Lin','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d19','duc.phan@medibook.vn','Phan Văn Đức','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d20','robert.johnson@medibook.vn','Robert Johnson','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d21','minhanh.duong@medibook.vn','Dương Minh Anh','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d22','chengwei.tan@medibook.vn','Tan Cheng Wei','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d23','quynh.ly@medibook.vn','Lý Thị Quỳnh','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d24','jean.martin@medibook.vn','Jean Martin','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d25','dung.ho@medibook.vn','Hồ Chí Dũng','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d26','michael.brown@medibook.vn','Michael Brown','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true)
ON CONFLICT (email) DO NOTHING;

-- 2d) 10 Patient Users
INSERT INTO app_users (id, email, full_name, password_hash, role, enabled) VALUES
('b0000001-0000-0000-0000-000000000p01','user1@medibook.local','Bệnh nhân 1','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p02','user2@medibook.local','Bệnh nhân 2','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p03','user3@medibook.local','Bệnh nhân 3','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p04','user4@medibook.local','Bệnh nhân 4','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p05','user5@medibook.local','Bệnh nhân 5','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p06','user6@medibook.local','Bệnh nhân 6','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p07','user7@medibook.local','Bệnh nhân 7','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p08','user8@medibook.local','Bệnh nhân 8','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p09','user9@medibook.local','Bệnh nhân 9','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-000000000p10','user10@medibook.local','Bệnh nhân 10','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true)
ON CONFLICT (email) DO NOTHING;

-- 2e) 26 Doctor Profiles
INSERT INTO doctor_profiles (id, user_id, specialty_id, title, country, clinic_name, bio, years_experience, consult_fee_vnd, avatar_url, rating, rating_count) VALUES
('c0000001-0000-0000-0000-000000000001','b0000001-0000-0000-0000-000000000d01','a0000001-0000-0000-0000-000000000001','PGS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','PGS.TS Nguyễn Văn Minh là chuyên gia hàng đầu về Tim mạch can thiệp, với hơn 22 năm kinh nghiệm tại Bệnh viện Bạch Mai.',22,450000,'https://randomuser.me/api/portraits/men/1.jpg',4.9,312),
('c0000001-0000-0000-0000-000000000002','b0000001-0000-0000-0000-000000000d02','a0000001-0000-0000-0000-000000000001','TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','TS.BS Trần Thị Hương chuyên về Siêu âm tim và Tim mạch dự phòng.',15,400000,'https://randomuser.me/api/portraits/women/2.jpg',4.8,198),
('c0000001-0000-0000-0000-000000000003','b0000001-0000-0000-0000-000000000d03','a0000001-0000-0000-0000-000000000001','Dr.','USA','Phòng khám Đa khoa Quốc tế MediBook','Dr. David Miller is a leading interventional cardiologist at Mayo Clinic.',25,1500000,'https://randomuser.me/api/portraits/men/3.jpg',4.9,520),
('c0000001-0000-0000-0000-000000000004','b0000001-0000-0000-0000-000000000d04','a0000001-0000-0000-0000-000000000002','ThS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','ThS.BS Phạm Thị Trang chuyên điều trị các bệnh lý da liễu mạn tính.',12,350000,'https://randomuser.me/api/portraits/women/4.jpg',4.7,245),
('c0000001-0000-0000-0000-000000000005','b0000001-0000-0000-0000-000000000d05','a0000001-0000-0000-0000-000000000002','Dr.','Germany','Phòng khám Đa khoa Quốc tế MediBook','Dr. Hans Müller là chuyên gia Da liễu tại Charité Berlin.',18,1200000,'https://randomuser.me/api/portraits/men/5.jpg',4.8,178),
('c0000001-0000-0000-0000-000000000006','b0000001-0000-0000-0000-000000000d06','a0000001-0000-0000-0000-000000000003','BS.CKII','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','BS.CKII Lê Hoàng Nam chuyên về phẫu thuật hàm mặt, cấy ghép Implant.',14,300000,'https://randomuser.me/api/portraits/women/6.jpg',4.6,167),
('c0000001-0000-0000-0000-000000000007','b0000001-0000-0000-0000-000000000d07','a0000001-0000-0000-0000-000000000003','Dr.','Japan','Phòng khám Đa khoa Quốc tế MediBook','Dr. Tanaka Yuki là chuyên gia hàng đầu Nhật Bản về nha khoa thẩm mỹ.',20,1300000,'https://randomuser.me/api/portraits/men/7.jpg',4.9,290),
('c0000001-0000-0000-0000-000000000008','b0000001-0000-0000-0000-000000000d08','a0000001-0000-0000-0000-000000000004','PGS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','PGS.TS Vũ Đức Hải chuyên phẫu thuật nội soi mũi xoang.',20,400000,'https://randomuser.me/api/portraits/women/8.jpg',4.8,278),
('c0000001-0000-0000-0000-000000000009','b0000001-0000-0000-0000-000000000d09','a0000001-0000-0000-0000-000000000004','Dr.','Korea','Phòng khám Đa khoa Quốc tế MediBook','Dr. Kim Seo-jun chuyên về phẫu thuật tai giữa và cấy ốc tai điện tử.',16,1100000,'https://randomuser.me/api/portraits/men/9.jpg',4.7,156),
('c0000001-0000-0000-0000-000000000010','b0000001-0000-0000-0000-000000000d10','a0000001-0000-0000-0000-000000000005','BS.CKI','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','BS Hoàng Thị Linh chuyên về căng da mặt, tiêm filler, botox.',10,500000,'https://randomuser.me/api/portraits/women/10.jpg',4.7,389),
('c0000001-0000-0000-0000-000000000011','b0000001-0000-0000-0000-000000000d11','a0000001-0000-0000-0000-000000000005','Dr.','Korea','Phòng khám Đa khoa Quốc tế MediBook','Dr. Park Ji-woo là chuyên gia thẩm mỹ hàng đầu Hàn Quốc.',15,1800000,'https://randomuser.me/api/portraits/men/11.jpg',4.9,445),
('c0000001-0000-0000-0000-000000000012','b0000001-0000-0000-0000-000000000d12','a0000001-0000-0000-0000-000000000006','GS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','GS.TS Đặng Quốc Tuấn là chuyên gia đầu ngành về Thần kinh học.',28,500000,'https://randomuser.me/api/portraits/women/12.jpg',4.9,410),
('c0000001-0000-0000-0000-000000000013','b0000001-0000-0000-0000-000000000d13','a0000001-0000-0000-0000-000000000006','Prof. Dr.','Germany','Phòng khám Đa khoa Quốc tế MediBook','Prof. Dr. Thomas Schneider là giáo sư Thần kinh học tại ĐH Hamburg.',30,2000000,'https://randomuser.me/api/portraits/men/13.jpg',5.0,380),
('c0000001-0000-0000-0000-000000000014','b0000001-0000-0000-0000-000000000d14','a0000001-0000-0000-0000-000000000007','TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','TS.BS Bùi Thanh Long chuyên phẫu thuật thay khớp háng, khớp gối.',17,400000,'https://randomuser.me/api/portraits/women/14.jpg',4.8,234),
('c0000001-0000-0000-0000-000000000015','b0000001-0000-0000-0000-000000000d15','a0000001-0000-0000-0000-000000000007','Dr.','USA','Phòng khám Đa khoa Quốc tế MediBook','Dr. James Williams là bác sĩ phẫu thuật chỉnh hình thể thao.',22,1600000,'https://randomuser.me/api/portraits/men/15.jpg',4.9,467),
('c0000001-0000-0000-0000-000000000016','b0000001-0000-0000-0000-000000000d16','a0000001-0000-0000-0000-000000000007','Dr.','Japan','Phòng khám Đa khoa Quốc tế MediBook','Dr. Sato Hiroshi chuyên về phẫu thuật cột sống ít xâm lấn.',19,1400000,'https://randomuser.me/api/portraits/women/16.jpg',4.8,310),
('c0000001-0000-0000-0000-000000000017','b0000001-0000-0000-0000-000000000d17','a0000001-0000-0000-0000-000000000008','PGS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','PGS.TS Ngô Thị Hoa là chuyên gia Nhi khoa hàng đầu.',18,350000,'https://randomuser.me/api/portraits/men/17.jpg',4.8,520),
('c0000001-0000-0000-0000-000000000018','b0000001-0000-0000-0000-000000000d18','a0000001-0000-0000-0000-000000000008','Dr.','Singapore','Phòng khám Đa khoa Quốc tế MediBook','Dr. Lim Wei Lin chuyên về Nhi khoa tổng quát và Tim bẩm sinh.',14,1000000,'https://randomuser.me/api/portraits/women/18.jpg',4.7,198),
('c0000001-0000-0000-0000-000000000019','b0000001-0000-0000-0000-000000000d19','a0000001-0000-0000-0000-000000000009','TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','TS.BS Phan Văn Đức chuyên nội soi tiêu hóa can thiệp.',16,400000,'https://randomuser.me/api/portraits/men/19.jpg',4.7,267),
('c0000001-0000-0000-0000-000000000020','b0000001-0000-0000-0000-000000000d20','a0000001-0000-0000-0000-000000000009','Dr.','USA','Phòng khám Đa khoa Quốc tế MediBook','Dr. Robert Johnson là chuyên gia Tiêu hóa tại Cleveland Clinic.',20,1500000,'https://randomuser.me/api/portraits/women/20.jpg',4.8,356),
('c0000001-0000-0000-0000-000000000021','b0000001-0000-0000-0000-000000000d21','a0000001-0000-0000-0000-00000000000a','BS.CKII','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','BS.CKII Dương Minh Anh chuyên phẫu thuật Phaco, Lasik.',15,350000,'https://randomuser.me/api/portraits/men/21.jpg',4.8,345),
('c0000001-0000-0000-0000-000000000022','b0000001-0000-0000-0000-000000000d22','a0000001-0000-0000-0000-00000000000a','Dr.','Singapore','Phòng khám Đa khoa Quốc tế MediBook','Dr. Tan Cheng Wei là chuyên gia Nhãn khoa tại SNEC.',18,1200000,'https://randomuser.me/api/portraits/women/22.jpg',4.9,412),
('c0000001-0000-0000-0000-000000000023','b0000001-0000-0000-0000-000000000d23','a0000001-0000-0000-0000-00000000000b','PGS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','PGS.TS Lý Thị Quỳnh chuyên về thai kỳ nguy cơ cao, IVF.',20,450000,'https://randomuser.me/api/portraits/men/23.jpg',4.9,567),
('c0000001-0000-0000-0000-000000000024','b0000001-0000-0000-0000-000000000d24','a0000001-0000-0000-0000-00000000000b','Dr.','France','Phòng khám Đa khoa Quốc tế MediBook','Dr. Jean Martin chuyên về y học bào thai và chẩn đoán trước sinh.',22,1400000,'https://randomuser.me/api/portraits/women/24.jpg',4.8,289),
('c0000001-0000-0000-0000-000000000025','b0000001-0000-0000-0000-000000000d25','a0000001-0000-0000-0000-00000000000c','GS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','GS.TS Hồ Chí Dũng chuyên về ung thư phổi, ung thư vú và liệu pháp miễn dịch.',25,500000,'https://randomuser.me/api/portraits/men/25.jpg',4.9,478),
('c0000001-0000-0000-0000-000000000026','b0000001-0000-0000-0000-000000000d26','a0000001-0000-0000-0000-00000000000c','Dr.','Singapore','Phòng khám Đa khoa Quốc tế MediBook','Dr. Michael Brown chuyên về xạ trị chính xác và liệu pháp nhắm đích.',20,1800000,'https://randomuser.me/api/portraits/women/26.jpg',4.9,356)
ON CONFLICT DO NOTHING;

-- 2f) Working Hours (Mon-Sat for all 26 doctors)
INSERT INTO working_hours (doctor_id, day_of_week, start_time, end_time, slot_minutes, break_start, break_end)
SELECT dp.id, dow, '08:00','17:00', 30, '12:00','13:00'
FROM doctor_profiles dp
CROSS JOIN generate_series(1,6) AS dow
ON CONFLICT ON CONSTRAINT uq_working_hours_doctor_day DO NOTHING;

-- 2g) Time Slots (14 days from today, 8-17h excluding 12-13h break, 30-min slots)
INSERT INTO time_slots (doctor_id, start_at, end_at, status)
SELECT dp.id,
  (d + t)::timestamptz,
  (d + t + interval '30 minutes')::timestamptz,
  'AVAILABLE'
FROM doctor_profiles dp
CROSS JOIN generate_series(CURRENT_DATE, CURRENT_DATE + 13, '1 day') AS d
CROSS JOIN generate_series('08:00'::time, '16:30'::time, '30 minutes'::interval) AS t
WHERE EXTRACT(DOW FROM d) BETWEEN 1 AND 6
  AND t NOT BETWEEN '12:00'::time AND '12:30'::time
ON CONFLICT ON CONSTRAINT uq_slot_doctor_start DO NOTHING;

-- 2h) 15 Appointments (5 doctors x 3 patients)
DO $$
DECLARE
  v_doc_id UUID;
  v_slot_id UUID;
  v_patient_id UUID;
  v_docs UUID[];
  v_patients UUID[];
  i INT; j INT;
  v_count INT := 0;
BEGIN
  SELECT array_agg(id ORDER BY id) INTO v_docs FROM doctor_profiles LIMIT 5;
  SELECT array_agg(id ORDER BY id) INTO v_patients FROM app_users WHERE role='USER';

  FOR i IN 1..5 LOOP
    FOR j IN 1..3 LOOP
      SELECT id INTO v_slot_id FROM time_slots
        WHERE doctor_id = v_docs[i] AND status = 'AVAILABLE'
        ORDER BY start_at LIMIT 1 OFFSET (j-1);
      IF v_slot_id IS NOT NULL THEN
        v_count := v_count + 1;
        INSERT INTO appointments (doctor_id, patient_id, time_slot_id, status, patient_note)
        VALUES (v_docs[i], v_patients[j], v_slot_id, 'BOOKED', 'Lịch hẹn mẫu số ' || v_count);
        UPDATE time_slots SET status='BOOKED' WHERE id=v_slot_id;
      END IF;
    END LOOP;
  END LOOP;
  RAISE NOTICE 'Seeded % appointments', v_count;
END $$;
