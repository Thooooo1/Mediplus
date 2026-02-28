create extension if not exists pgcrypto;

create table if not exists app_users (
  id uuid primary key default gen_random_uuid(),
  email varchar(190) not null unique,
  full_name varchar(120) not null,
  phone varchar(30),
  password_hash varchar(120) not null,
  role varchar(20) not null,
  enabled boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists specialties (
  id uuid primary key default gen_random_uuid(),
  code varchar(40) not null unique,
  name varchar(120) not null
);

create table if not exists doctor_profiles (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null unique references app_users(id),
  specialty_id uuid not null references specialties(id),
  title varchar(80),
  country varchar(80),
  clinic_name varchar(120),
  bio varchar(1000),
  years_experience int,
  consult_fee_vnd bigint,
  avatar_url varchar(500),
  rating double precision default 5.0,
  rating_count int default 0
);

create table if not exists working_hours (
  id uuid primary key default gen_random_uuid(),
  doctor_id uuid not null references doctor_profiles(id),
  day_of_week smallint not null,
  start_time time not null,
  end_time time not null,
  slot_minutes smallint not null default 30,
  break_start time,
  break_end time,
  constraint uq_working_hours_doctor_day unique (doctor_id, day_of_week)
);

create table if not exists time_slots (
  id uuid primary key default gen_random_uuid(),
  doctor_id uuid not null references doctor_profiles(id),
  start_at timestamptz not null,
  end_at timestamptz not null,
  status varchar(20) not null default 'AVAILABLE',
  created_at timestamptz not null default now(),
  constraint uq_slot_doctor_start unique (doctor_id, start_at)
);

create table if not exists appointments (
  id uuid primary key default gen_random_uuid(),
  doctor_id uuid not null references doctor_profiles(id),
  patient_id uuid not null references app_users(id),
  time_slot_id uuid not null references time_slots(id),
  status varchar(20) not null default 'BOOKED',
  patient_note varchar(1000),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  cancelled_at timestamptz
);

create table if not exists chat_messages (
  id uuid primary key default gen_random_uuid(),
  doctor_id uuid not null references doctor_profiles(id),
  patient_id uuid not null references app_users(id),
  sender varchar(10) not null,
  content varchar(1000) not null,
  is_read boolean not null default false,
  created_at timestamptz not null default now()
);

create table if not exists medical_records (
  id uuid primary key default gen_random_uuid(),
  doctor_id uuid not null references doctor_profiles(id),
  patient_id uuid not null references app_users(id),
  diagnosis varchar(255) not null,
  notes varchar(2000),
  visit_date timestamptz not null,
  type varchar(50),
  created_at timestamptz not null default now()
);

-- Indexes
create index if not exists idx_time_slots_doctor_start on time_slots(doctor_id, start_at);
create index if not exists idx_appointments_patient on appointments(patient_id);
create index if not exists idx_appointments_doctor on appointments(doctor_id);
create index if not exists idx_messages_doctor on chat_messages(doctor_id);
create index if not exists idx_messages_patient on chat_messages(patient_id);
create index if not exists idx_records_doctor on medical_records(doctor_id);
create index if not exists idx_records_patient on medical_records(patient_id);
