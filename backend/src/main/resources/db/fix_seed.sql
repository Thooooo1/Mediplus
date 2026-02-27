-- Fix: Insert 10 patients with valid hex UUIDs
INSERT INTO app_users (id, email, full_name, password_hash, role, enabled) VALUES
('b0000001-0000-0000-0000-0000000000e1','user1@medibook.vn','Bệnh nhân 1','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e2','user2@medibook.vn','Bệnh nhân 2','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e3','user3@medibook.vn','Bệnh nhân 3','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e4','user4@medibook.vn','Bệnh nhân 4','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e5','user5@medibook.vn','Bệnh nhân 5','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e6','user6@medibook.vn','Bệnh nhân 6','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e7','user7@medibook.vn','Bệnh nhân 7','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e8','user8@medibook.vn','Bệnh nhân 8','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000e9','user9@medibook.vn','Bệnh nhân 9','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true),
('b0000001-0000-0000-0000-0000000000ea','user10@medibook.vn','Bệnh nhân 10','$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W','USER',true)
ON CONFLICT (email) DO NOTHING;

-- Fix: Generate time slots for 14 days
INSERT INTO time_slots (doctor_id, start_at, end_at, status)
SELECT dp.id,
  (d + t) AT TIME ZONE 'Asia/Ho_Chi_Minh',
  (d + t + interval '30 minutes') AT TIME ZONE 'Asia/Ho_Chi_Minh',
  'AVAILABLE'
FROM doctor_profiles dp
CROSS JOIN generate_series(CURRENT_DATE, CURRENT_DATE + 13, interval '1 day') AS d
CROSS JOIN generate_series('08:00'::time, '16:30'::time, interval '30 minutes') AS t
WHERE EXTRACT(DOW FROM d) BETWEEN 1 AND 6
  AND t < '12:00'::time OR t >= '13:00'::time
ON CONFLICT ON CONSTRAINT uq_slot_doctor_start DO NOTHING;

-- Fix: Seed 15 appointments
DO $$
DECLARE
  v_doc_id UUID;
  v_slot_id UUID;
  v_patient_id UUID;
  v_count INT := 0;
  doc_rec RECORD;
  i INT := 0;
  j INT;
BEGIN
  FOR doc_rec IN (SELECT id FROM doctor_profiles ORDER BY id LIMIT 5) LOOP
    i := i + 1;
    FOR j IN 1..3 LOOP
      SELECT id INTO v_slot_id FROM time_slots
        WHERE doctor_id = doc_rec.id AND status = 'AVAILABLE'
        ORDER BY start_at
        LIMIT 1 OFFSET (j-1);

      SELECT id INTO v_patient_id FROM app_users
        WHERE role = 'USER'
        ORDER BY id
        LIMIT 1 OFFSET (j-1);

      IF v_slot_id IS NOT NULL AND v_patient_id IS NOT NULL THEN
        v_count := v_count + 1;
        INSERT INTO appointments (doctor_id, patient_id, time_slot_id, status, patient_note)
        VALUES (doc_rec.id, v_patient_id, v_slot_id, 'BOOKED', 'Lịch hẹn mẫu số ' || v_count);
        UPDATE time_slots SET status = 'BOOKED' WHERE id = v_slot_id;
      END IF;
    END LOOP;
  END LOOP;
  RAISE NOTICE 'Seeded % appointments', v_count;
END $$;
