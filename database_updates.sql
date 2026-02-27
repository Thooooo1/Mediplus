-- =========================================================================
-- MEDIBOOK UNIFIED DATABASE UPDATES
-- =========================================================================
-- This file contains all the scattered SQL patches combined into one place.
-- Use this script to update existing databases with the correct bios,
-- clinic details, default passwords, and static local avatars.
-- =========================================================================

SET client_encoding = 'UTF8';

-- -------------------------------------------------------------------------
-- 1. UPDATE DOCTOR BIOS & CLINIC
-- -------------------------------------------------------------------------
UPDATE doctor_profiles SET clinic_name = 'Phòng khám Đa khoa Quốc tế MediBook (Tầng 4, Tòa nhà Y tế)';

UPDATE doctor_profiles SET bio = 'PGS.TS Nguyễn Văn Minh là chuyên gia đầu ngành về Tim mạch can thiệp tại Việt Nam. Ông có hơn 22 năm kinh nghiệm lâm sàng, từng tu nghiệp tại Pháp và Hoa Kỳ. Chuyên sâu về điều trị bệnh lý mạch vành, hở van tim và các ca can thiệp tim mạch phức tạp. Ông đã thực hiện thành công hơn 5000 ca can thiệp tim mạch.' WHERE id = 'c0000001-0000-0000-0000-000000000001';
UPDATE doctor_profiles SET bio = 'TS.BS Trần Thị Hương là chuyên gia về siêu âm tim và tim mạch dự phòng. Với 15 năm kinh nghiệm, bà tập trung vào việc tầm soát sớm các bệnh lý tim bẩm sinh và quản lý huyết áp cho người cao tuổi. Bà luôn tận tâm tư vấn lối sống lành mạnh để bảo vệ trái tim khỏe mạnh cho cộng đồng.' WHERE id = 'c0000001-0000-0000-0000-000000000002';
UPDATE doctor_profiles SET bio = 'Dr. David Miller is a world-renowned interventional cardiologist from Mayo Clinic, USA. He specializes in advanced cardiac imaging and minimally invasive heart procedures. With a focus on patient-centered care, Dr. Miller brings international standards of heart treatments to MediBook patients.' WHERE id = 'c0000001-0000-0000-0000-000000000003';
UPDATE doctor_profiles SET bio = 'ThS.BS Phạm Thị Trang là bác sĩ giỏi chuyên điều trị các bệnh lý da liễu mạn tính như vảy nến, chàm và viêm da cơ địa. Bà có kinh nghiệm 12 năm và nổi tiếng với phương pháp kết hợp y học hiện đại với chăm sóc da chuyên sâu, giúp bệnh nhân hồi phục làn da khỏe mạnh và tự tin.' WHERE id = 'c0000001-0000-0000-0000-000000000004';
UPDATE doctor_profiles SET bio = 'Dr. Hans Müller brings German precision to dermatology. As a senior consultant from Charité Berlin, he specializes in skin cancer screening and laser dermatology. Dr. Müller is passionate about utilizing the latest laser technologies for therapeutic and aesthetic skin treatments.' WHERE id = 'c0000001-0000-0000-0000-000000000005';
UPDATE doctor_profiles SET bio = 'BS.CKII Lê Hoàng Nam là chuyên gia hàng đầu về phẫu thuật hàm mặt và cấy ghép Implant tại Việt Nam. Với bàn tay vàng và đôi mắt thẩm mỹ, ông đã mang lại nụ cười rạng rỡ cho hàng nghìn khách hàng. Ông đặc biệt am hiểu về các kỹ thuật trồng răng không đau và phục hình răng sứ cao cấp.' WHERE id = 'c0000001-0000-0000-0000-000000000006';
UPDATE doctor_profiles SET bio = 'Dr. Tanaka Yuki is a distinguished cosmetic dentist from Tokyo, Japan. He is an expert in "Minimal Intervention" dentistry, focusing on preserving natural tooth structure while achieving perfect aesthetics. His work is characterized by the meticulous Japanese standards of quality and precision.' WHERE id = 'c0000001-0000-0000-0000-000000000007';
UPDATE doctor_profiles SET bio = 'PGS.TS Vũ Đức Hải là bậc thầy về phẫu thuật nội soi mũi xoang và bệnh lý Tai Mũi Họng phức tạp. Ông hiện là cố vấn chuyên môn tại nhiều bệnh viện lớn. Với hơn 20 năm kinh nghiệm, ông áp dụng công nghệ nội soi tiên tiến nhất để giúp bệnh nhân điều trị triệt để các bệnh lý mũi xoang mạn tính.' WHERE id = 'c0000001-0000-0000-0000-000000000008';
UPDATE doctor_profiles SET bio = 'Dr. Kim Seo-jun is a prominent ENT specialist from Samsung Medical Center, Korea. He specializes in middle ear surgery and cochlear implants. Dr. Kim is known for his highly successful micro-surgeries that restore hearing and improve quality of life for both children and adults.' WHERE id = 'c0000001-0000-0000-0000-000000000009';
UPDATE doctor_profiles SET bio = 'BS Hoàng Thị Linh chuyên sâu về thẩm mỹ nội khoa với các kỹ thuật căng da mặt bằng chỉ, tiêm filler và botox tự nhiên. Bà đề cao vẻ đẹp hài hòa, giúp khách hàng trẻ hóa mà vẫn giữ được nét riêng. Bà luôn cập nhật những xu hướng làm đẹp mới nhất từ các hội nghị quốc tế.' WHERE id = 'c0000001-0000-0000-0000-000000000010';
UPDATE doctor_profiles SET bio = 'Dr. Park Ji-woo is a lead plastic surgeon from Gangnam, Korea. He is an artist in facial contouring and rhinoplasty. With over 15 years of experience in the "Beauty Capital of the World," Dr. Park brings the most sophisticated Korean beauty standards to MediBook.' WHERE id = 'c0000001-0000-0000-0000-000000000011';
UPDATE doctor_profiles SET bio = 'GS.TS Đặng Quốc Tuấn là giáo sư đầu ngành về thần kinh học tại Việt Nam. Ông có kiến thức sâu rộng về các bệnh lý não bộ, cột sống và mất ngủ kinh niên. Ông đã đào tạo hàng thế hệ bác sĩ thần kinh và trực tiếp điều trị những ca bệnh khó, hiếm gặp nhất.' WHERE id = 'c0000001-0000-0000-0000-000000000012';
UPDATE doctor_profiles SET bio = 'Prof. Dr. Thomas Schneider is a renowned Neurologist from University Medical Center Hamburg, Germany. He specializes in neuro-oncology and degenerative disorders like Parkinson''s. His research-based approach ensures patients receive the most current and effective neurological therapies.' WHERE id = 'c0000001-0000-0000-0000-000000000013';
UPDATE doctor_profiles SET bio = 'TS.BS Bùi Thanh Long là chuyên gia trong lĩnh vực phẫu thuật thay khớp. Ông từng công tác tại các trung tâm chỉnh hình lớn, thực hiện hàng nghìn ca thay khớp háng, khớp gối nhân tạo giúp bệnh nhân vận động trở lại bình thường. Ông cũng chuyên sâu về điều trị loãng xương và thoái hóa khớp.' WHERE id = 'c0000001-0000-0000-0000-000000000014';
UPDATE doctor_profiles SET bio = 'Dr. James Williams is a top orthopaedic sports medicine surgeon from New York. He has worked with professional athletes and specializes in arthroscopic surgery of the shoulder, knee, and hip. His goal is to return patients to their active lifestyles as quickly and safely as possible.' WHERE id = 'c0000001-0000-0000-0000-000000000015';
UPDATE doctor_profiles SET bio = 'Dr. Sato Hiroshi is a specialist in minimally invasive spine surgery from Japan. He utilizes advanced endoscopic techniques to treat herniated discs and spinal stenosis, resulting in less pain and faster recovery for his patients compared to traditional open surgeries.' WHERE id = 'c0000001-0000-0000-0000-000000000016';
UPDATE doctor_profiles SET bio = 'PGS.TS Ngô Thị Hoa là chuyên gia nhi khoa với tấm lòng yêu trẻ vô hạn. Bà am hiểu tâm lý trẻ nhỏ và chuyên điều trị các bệnh lý hô hấp, tiêu hóa và dinh dưỡng ở trẻ. Bà là người bạn đồng hành tin cậy của các bậc phụ huynh trong hành trình chăm sóc sức khỏe con yêu.' WHERE id = 'c0000001-0000-0000-0000-000000000017';
UPDATE doctor_profiles SET bio = 'Dr. Lim Wei Lin is a senior paediatrician from Singapore. She specializes in general paediatrics and congenital heart diseases. Dr. Lim is passionate about preventative health in children and providing holistic care that addresses both physical and emotional needs.' WHERE id = 'c0000001-0000-0000-0000-000000000018';
UPDATE doctor_profiles SET bio = 'TS.BS Phan Văn Đức là chuyên gia về nội soi tiêu hóa can thiệp. Ông có kỹ năng điêu luyện trong việc tầm soát ung thư sớm đường tiêu hóa và điều trị các bệnh lý gan mật. Ông luôn ưu tiên các phương pháp điều trị nhẹ nhàng nhưng mang lại hiệu quả cao nhất cho bệnh nhân.' WHERE id = 'c0000001-0000-0000-0000-000000000019';
UPDATE doctor_profiles SET bio = 'Dr. Robert Johnson is an esteemed Gastroenterologist from Cleveland Clinic, USA. He specializes in inflammatory bowel disease (IBD) and irritable bowel syndrome (IBS). Dr. Johnson employs a comprehensive approach integrating advanced medical therapies with specialized dietary guidance.' WHERE id = 'c0000001-0000-0000-0000-000000000020';
UPDATE doctor_profiles SET bio = 'BS.CKII Dương Minh Anh là bác sĩ nhãn khoa tài hoa, chuyên thực hiện các ca phẫu thuật Phaco điều trị đục thủy tinh thể và Lasik điều chỉnh tật khúc xạ. Bà tỉ mỉ trong từng khâu thăm khám để đảm bảo mang lại thị lực tối ưu nhất cho đôi mắt của bệnh nhân.' WHERE id = 'c0000001-0000-0000-0000-000000000021';
UPDATE doctor_profiles SET bio = 'Dr. Tan Cheng Wei is a senior consultant ophthalmologist from Singapore. He specializes in retinal disorders and glaucoma management. Dr. Tan is dedicated to preserving vision through advanced diagnostic technologies and personalized treatment plans for chronic eye conditions.' WHERE id = 'c0000001-0000-0000-0000-000000000022';
UPDATE doctor_profiles SET bio = 'PGS.TS Lý Thị Quỳnh là chuyên gia về sản khoa và hỗ trợ sinh sản (IVF). Bà đã giúp hàng nghìn cặp vợ chồng hiếm muộn đón con yêu chào đời. Bà cũng chuyên quản lý các thai kỳ nguy cơ cao, đảm bảo sự an toàn cho cả mẹ và bé trong suốt chín tháng mười ngày.' WHERE id = 'c0000001-0000-0000-0000-000000000023';
UPDATE doctor_profiles SET bio = 'Dr. Jean Martin is a fetal medicine specialist from Paris, France. He specializes in prenatal diagnosis and fetal ultrasound. Dr. Martin provides expert care and guidance for complex fetal conditions, ensuring the best possible start in life for the new generation.' WHERE id = 'c0000001-0000-0000-0000-000000000024';
UPDATE doctor_profiles SET bio = 'GS.TS Hồ Chí Dũng là giáo sư đầu ngành ung bướu Việt Nam. Ông chuyên sâu về điều trị các loại ung thư bằng liệu pháp miễn dịch và liệu pháp nhắm đích tiên tiến. Ông không chỉ điều trị bằng thuốc mà còn truyền sức mạnh tinh thần giúp bệnh nhân ung thư vượt qua nghịch cảnh.' WHERE id = 'c0000001-0000-0000-0000-000000000025';
UPDATE doctor_profiles SET bio = 'Dr. Michael Brown is a renowned Oncologist from Singapore. He specializes in precision radiotherapy and chemotherapy. His approach combines cutting-edge clinical technology with deep empathy, providing patients with personalized comprehensive cancer care.' WHERE id = 'c0000001-0000-0000-0000-000000000026';

-- -------------------------------------------------------------------------
-- 2. UPDATE USER EMAILS
-- -------------------------------------------------------------------------
UPDATE app_users SET email = 'admin@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000001';
UPDATE app_users SET email = 'minh.nguyen@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d01';
UPDATE app_users SET email = 'huong.tran@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d02';
UPDATE app_users SET email = 'david.miller@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d03';
UPDATE app_users SET email = 'trang.pham@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d04';
UPDATE app_users SET email = 'hans.muller@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d05';
UPDATE app_users SET email = 'nam.le@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d06';
UPDATE app_users SET email = 'yuki.tanaka@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d07';
UPDATE app_users SET email = 'hai.vu@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d08';
UPDATE app_users SET email = 'seojun.kim@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d09';
UPDATE app_users SET email = 'linh.hoang@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d10';
UPDATE app_users SET email = 'jiwoo.park@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d11';
UPDATE app_users SET email = 'tuan.dang@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d12';
UPDATE app_users SET email = 'thomas.schneider@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d13';
UPDATE app_users SET email = 'long.bui@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d14';
UPDATE app_users SET email = 'james.williams@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d15';
UPDATE app_users SET email = 'hiroshi.sato@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d16';
UPDATE app_users SET email = 'hoa.ngo@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d17';
UPDATE app_users SET email = 'weilin.lim@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d18';
UPDATE app_users SET email = 'duc.phan@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d19';
UPDATE app_users SET email = 'robert.johnson@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d20';
UPDATE app_users SET email = 'minhanh.duong@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d21';
UPDATE app_users SET email = 'chengwei.tan@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d22';
UPDATE app_users SET email = 'quynh.ly@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d23';
UPDATE app_users SET email = 'jean.martin@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d24';
UPDATE app_users SET email = 'dung.ho@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d25';
UPDATE app_users SET email = 'michael.brown@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000d26';

UPDATE app_users SET email = 'user1@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000p01';
UPDATE app_users SET email = 'user2@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000p02';
UPDATE app_users SET email = 'user3@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000p03';
UPDATE app_users SET email = 'user4@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000p04';
UPDATE app_users SET email = 'user5@medibook.vn' WHERE id = 'b0000001-0000-0000-0000-000000000p05';

-- -------------------------------------------------------------------------
-- 3. UPDATE PASSWORDS TO A STANDARD
-- -------------------------------------------------------------------------
-- Update hash of '123'
UPDATE app_users SET password_hash = '$2a$10$8.tJ78q.wXm4g.nKk7vW6O.6yE6uN6uN6uN6uN6uN6uN6uN6uN6uN' WHERE email LIKE '%@medibook.vn';

-- -------------------------------------------------------------------------
-- 4. FIX DOCTOR AVATARS (Local Static Images)
-- -------------------------------------------------------------------------
WITH female_docs AS (
  SELECT dp.user_id, ROW_NUMBER() OVER (ORDER BY u.id) as rn
  FROM doctor_profiles dp
  JOIN app_users u ON dp.user_id = u.id
  WHERE u.full_name LIKE '%Thị%' OR u.full_name = 'Park Ji-woo' OR u.full_name = 'Tanaka Yuki'
)
UPDATE doctor_profiles dp SET avatar_url = '/assets/doctors/female_' || fd.rn || '.jpg'
FROM female_docs fd WHERE dp.user_id = fd.user_id;

WITH male_docs AS (
  SELECT dp.user_id, ROW_NUMBER() OVER (ORDER BY u.id) as rn
  FROM doctor_profiles dp
  JOIN app_users u ON dp.user_id = u.id
  WHERE u.full_name NOT LIKE '%Thị%' AND u.full_name != 'Park Ji-woo' AND u.full_name != 'Tanaka Yuki'
)
UPDATE doctor_profiles dp SET avatar_url = '/assets/doctors/male_' || md.rn || '.jpg'
FROM male_docs md WHERE dp.user_id = md.user_id;

UPDATE doctor_profiles dp SET avatar_url = '/assets/doctors/c_m_1.jpg' FROM app_users u WHERE dp.user_id = u.id AND u.full_name = 'Robert Johnson';
UPDATE doctor_profiles dp SET avatar_url = '/assets/doctors/a_m_8.jpg' FROM app_users u WHERE dp.user_id = u.id AND u.full_name = 'Tan Cheng Wei';
UPDATE doctor_profiles dp SET avatar_url = '/assets/doctors/c_m_2.jpg' FROM app_users u WHERE dp.user_id = u.id AND u.full_name = 'Michael Brown';
UPDATE doctor_profiles dp SET avatar_url = '/assets/doctors/a_m_2.jpg' FROM app_users u WHERE dp.user_id = u.id AND u.full_name LIKE '%Vũ Đức Hải%';
