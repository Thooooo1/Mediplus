-- Generate time slots using PL/pgSQL loop (reliable approach)
DO $$
DECLARE
  dp_rec RECORD;
  d DATE;
  t TIME;
  slot_count INT := 0;
BEGIN
  FOR dp_rec IN SELECT id FROM doctor_profiles LOOP
    FOR d IN SELECT dd::date FROM generate_series(CURRENT_DATE::timestamp, (CURRENT_DATE + 13)::timestamp, '1 day'::interval) dd LOOP
      -- Skip Sunday (0) in PostgreSQL DOW
      IF EXTRACT(DOW FROM d) BETWEEN 1 AND 6 THEN
        t := '08:00'::time;
        WHILE t <= '16:30'::time LOOP
          -- Skip 12:00-12:30 break
          IF t < '12:00'::time OR t >= '13:00'::time THEN
            INSERT INTO time_slots (doctor_id, start_at, end_at, status)
            VALUES (
              dp_rec.id,
              (d + t) AT TIME ZONE 'Asia/Ho_Chi_Minh',
              (d + t + interval '30 minutes') AT TIME ZONE 'Asia/Ho_Chi_Minh',
              'AVAILABLE'
            ) ON CONFLICT ON CONSTRAINT uq_slot_doctor_start DO NOTHING;
            slot_count := slot_count + 1;
          END IF;
          t := t + interval '30 minutes';
        END LOOP;
      END IF;
    END LOOP;
  END LOOP;
  RAISE NOTICE 'Generated % time slot records', slot_count;
END $$;

-- Seed 15 appointments
DO $$
DECLARE
  v_slot_id UUID;
  v_patient_id UUID;
  v_count INT := 0;
  doc_rec RECORD;
  j INT;
BEGIN
  FOR doc_rec IN (SELECT id FROM doctor_profiles ORDER BY id LIMIT 5) LOOP
    FOR j IN 0..2 LOOP
      SELECT id INTO v_slot_id FROM time_slots
        WHERE doctor_id = doc_rec.id AND status = 'AVAILABLE'
        ORDER BY start_at
        LIMIT 1 OFFSET j;

      SELECT id INTO v_patient_id FROM app_users
        WHERE role = 'USER'
        ORDER BY id
        LIMIT 1 OFFSET j;

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
