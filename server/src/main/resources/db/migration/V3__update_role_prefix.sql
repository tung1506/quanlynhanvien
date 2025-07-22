-- Update existing users to have ROLE_ prefix if they don't already
UPDATE users
SET role = CONCAT('ROLE_', role)
WHERE role NOT LIKE 'ROLE_%';

