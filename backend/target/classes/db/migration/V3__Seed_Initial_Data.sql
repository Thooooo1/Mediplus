-- Consolidation of init.sql and fix_seed.sql
-- Created: 2026-02-27

-- 1. Specialties
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

-- 2. Users (Admin, Doctors, Patients)
-- Password "123": $2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W
-- (Note: Using the existing hash which corresponds to Password@123 for now, or updating to 123)

INSERT INTO app_users (id, email, full_name, password_hash, role, enabled) VALUES
('b0000001-0000-0000-0000-000000000001','admin@medibook.vn','Admin','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','ADMIN',true),
('b0000001-0000-0000-0000-000000000d01','minh.nguyen@medibook.vn','Nguyễn Văn Minh','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d02','huong.tran@medibook.vn','Trần Thị Hương','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-000000000d03','david.miller@medibook.vn','David Miller','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','DOCTOR',true),
('b0000001-0000-0000-0000-0000000000e1','user1@medibook.vn','Bệnh nhân 1','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true)
ON CONFLICT (email) DO NOTHING;

-- 3. Doctor Profiles
INSERT INTO doctor_profiles (id, user_id, specialty_id, title, country, clinic_name, bio, years_experience, consult_fee_vnd, avatar_url, rating, rating_count) VALUES
('c0000001-0000-0000-0000-000000000001','b0000001-0000-0000-0000-000000000d01','a0000001-0000-0000-0000-000000000001','PGS.TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','PGS.TS Nguyễn Văn Minh là chuyên gia hàng đầu về Tim mạch can thiệp, với hơn 20 năm kinh nghiệm.',22,450000,'/assets/doctors/male_1.jpg',4.9,312),
('c0000001-0000-0000-0000-000000000002','b0000001-0000-0000-0000-000000000d02','a0000001-0000-0000-0000-000000000001','TS.BS','Vietnam','Phòng khám Đa khoa Quốc tế MediBook','TS.BS Trần Thị Hương chuyên về Siêu âm tim và Tim mạch dự phòng.',15,400000,'/assets/doctors/female_1.jpg',4.8,198),
('c0000001-0000-0000-0000-000000000003','b0000001-0000-0000-0000-000000000d03','a0000001-0000-0000-0000-000000000001','Dr.','USA','Phòng khám Đa khoa Quốc tế MediBook','Dr. David Miller is a leading interventional cardiologist at Mayo Clinic.',25,1500000,'/assets/doctors/male_2.jpg',4.9,520)
ON CONFLICT (user_id) DO NOTHING;

-- 4. Time Slots
INSERT INTO time_slots (doctor_id, start_at, end_at, status)
SELECT dp.id,
  (d + t) AT TIME ZONE 'Asia/Ho_Chi_Minh',
  (d + t + interval '30 minutes') AT TIME ZONE 'Asia/Ho_Chi_Minh',
  'AVAILABLE'
FROM doctor_profiles dp
CROSS JOIN generate_series(CURRENT_DATE, CURRENT_DATE + 13, interval '1 day') AS d
CROSS JOIN generate_series('08:00'::time, '16:30'::time, interval '30 minutes') AS t
WHERE EXTRACT(DOW FROM d) BETWEEN 1 AND 6
  AND (t < '12:00'::time OR t >= '13:00'::time)
ON CONFLICT ON CONSTRAINT uq_slot_doctor_start DO NOTHING;
