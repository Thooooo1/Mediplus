-- Fix password to "123" for all seeded users
-- Hash sourced from database_updates.sql
UPDATE app_users 
SET password_hash = '$2a$10$8.tJ78q.wXm4g.nKk7vW6O.6yE6uN6uN6uN6uN6uN6uN6uN6uN6uN' 
WHERE email LIKE '%@medibook.vn%';
