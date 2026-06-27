-- Seed ADMIN user (password is 'password' hashed with BCrypt strength 12)
INSERT INTO users (full_name, email, password_hash, role, account_status, created_at)
VALUES ('System Admin', 'admin@campusgate.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'ADMIN', 'ACTIVE', CURRENT_TIMESTAMP);

-- Seed Gates
INSERT INTO gates (gate_name, location, is_operational) VALUES 
('Main Gate', 'North Entrance', true),
('North Gate', 'Science Block Entrance', true),
('Library Gate', 'Library Entrance', true);

-- Seed Students
INSERT INTO users (full_name, email, password_hash, role, account_status, student_number, created_at) VALUES 
('Alice Smith', 'alice@student.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'STUDENT', 'ACTIVE', 'STU001', CURRENT_TIMESTAMP),
('Bob Jones', 'bob@student.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'STUDENT', 'ACTIVE', 'STU002', CURRENT_TIMESTAMP),
('Charlie Brown', 'charlie@student.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'STUDENT', 'ACTIVE', 'STU003', CURRENT_TIMESTAMP),
('Diana Prince', 'diana@student.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'STUDENT', 'SUSPENDED', 'STU004', CURRENT_TIMESTAMP),
('Evan Wright', 'evan@student.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'STUDENT', 'ACTIVE', 'STU005', CURRENT_TIMESTAMP);

-- Seed Security Guard
INSERT INTO users (full_name, email, password_hash, role, account_status, created_at)
VALUES ('Security Guard 1', 'guard1@campusgate.edu', '$2a$12$Ea2TjI0pDk4e3N2uJ.9QTu3c2q8Vp/bK9h4/9U/vQy.l5E/2T2J2y', 'SECURITY', 'ACTIVE', CURRENT_TIMESTAMP);
