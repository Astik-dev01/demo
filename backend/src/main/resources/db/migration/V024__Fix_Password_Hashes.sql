-- V024: Fix password hashes for all users
-- Password: password123
-- BCrypt hash generated with cost factor 10

UPDATE sys_users
SET password_hash = '$2a$10$IDNqxrGzeIYGJJS8wERJt.4elNtRbasBdI5HQgSYefdOWPlP4wKmu'
WHERE password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye1mGqMk0mIVPJYT2YKQXJhOLBQn9gK2q';
