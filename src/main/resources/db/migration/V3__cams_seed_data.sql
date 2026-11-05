-- Demo seed data: specialties, one manager / receptionist / patient and ten doctors with their weekly
-- schedule. Every account uses the password "Demo@12345" (Argon2id hash below). Idempotent, so it is
-- safe on a database that was already seeded by CamsDataSeeder. cams_settings is seeded in V2.

INSERT INTO cams_specialties (name) VALUES
    ('Nội tổng quát'),
    ('Tim mạch'),
    ('Da liễu')
ON CONFLICT (name) DO NOTHING;

INSERT INTO cams_users (name, email, password_hash, role, phone, specialty_id) VALUES
    ('Quản lý Demo',   'admin@cams.local',   '$argon2id$v=19$m=16384,t=2,p=1$ZqSNVND6lK+GcEIL66EjYw$sGWv5BGAk6kiJDJWEodeZD2p72tQLHsZypltKmiYopY', 'manager',      '0900000000', NULL),
    ('Lễ tân Demo',    'staff@cams.local',   '$argon2id$v=19$m=16384,t=2,p=1$ZqSNVND6lK+GcEIL66EjYw$sGWv5BGAk6kiJDJWEodeZD2p72tQLHsZypltKmiYopY', 'receptionist', '0900000000', NULL),
    ('Bệnh nhân Demo', 'patient@cams.local', '$argon2id$v=19$m=16384,t=2,p=1$ZqSNVND6lK+GcEIL66EjYw$sGWv5BGAk6kiJDJWEodeZD2p72tQLHsZypltKmiYopY', 'patient',      '0900000000', NULL)
ON CONFLICT (email) DO NOTHING;

-- Ten doctors, cycling through the three specialties in order (1st -> Nội tổng quát, 2nd -> Tim mạch, ...).
INSERT INTO cams_users (name, email, password_hash, role, phone, specialty_id)
SELECT 'Bác sĩ Demo ' || LPAD(n::text, 2, '0'),
       'doctor' || LPAD(n::text, 2, '0') || '@cams.local',
       '$argon2id$v=19$m=16384,t=2,p=1$ZqSNVND6lK+GcEIL66EjYw$sGWv5BGAk6kiJDJWEodeZD2p72tQLHsZypltKmiYopY',
       'doctor',
       '',
       (SELECT id FROM cams_specialties
         WHERE name = CASE (n - 1) % 3 WHEN 0 THEN 'Nội tổng quát' WHEN 1 THEN 'Tim mạch' ELSE 'Da liễu' END)
FROM generate_series(1, 10) AS n
ON CONFLICT (email) DO NOTHING;

-- Default weekly template (Mon-Sat, 07:00-17:00) for every doctor that has none yet.
INSERT INTO cams_schedules (doctor_id)
SELECT id FROM cams_users WHERE role = 'doctor'
ON CONFLICT (doctor_id) DO NOTHING;
