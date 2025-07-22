UPDATE users SET created_at = NOW() WHERE created_at IS NULL;

