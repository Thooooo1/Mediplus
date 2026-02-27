-- Fix corrupted password hashes introduced in V4
-- Restore all passwords to "123" with the correct BCrypt hash from V3
UPDATE app_users 
SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMQkesxOC1v8yO3IVCFVHGMEHx7WySCVGJ1aI.W' 
WHERE email LIKE '%@medibook.vn%';
