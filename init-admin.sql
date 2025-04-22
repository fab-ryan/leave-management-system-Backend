-- Insert admin user
-- Department Defualt information
INSERT INTO departments (id, name, description, created_at, updated_at)
VALUES (
    '9d7ba396-b8c4-477e-bc69-f5b35bef6553',
    'Administration',
    'Administration Department',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
-- Insert admin employee record
INSERT INTO employees (id, name, email,phone, password, location,role,status,profile_completed,version,team, department_id, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000000',
    'Admin',
    'admin@outlook.com',
    '$2a$10$Bte6SlCh2GIzJm22jiDS7eadDX2XvLD3QBm359KGPeKc7kLkOCbTe', -- password:password
    '07822222222',
    'Kigali Rwanda',
    'ADMIN',
    'ACTIVE',
    'true',
    0,
    'ADMIN',
    '9d7ba396-b8c4-477e-bc69-f5b35bef6553',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
   
);

