-- Delete duplicate usernames keeping only the latest entry
DELETE FROM users a USING users b
WHERE a.username = b.username
AND a.id < b.id;

-- Add unique constraint
ALTER TABLE users ADD CONSTRAINT uk_username UNIQUE (username);
