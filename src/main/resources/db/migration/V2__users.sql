
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username)
SELECT DISTINCT author FROM messages;

-- Связь сообщения с пользователем
ALTER TABLE messages ADD COLUMN user_id BIGINT REFERENCES users(id);

UPDATE messages m
SET user_id = u.id
FROM users u
WHERE u.username = m.author;

ALTER TABLE messages ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE messages DROP COLUMN author;

-- Индекс для быстрого поиска сообщений по пользователю
CREATE INDEX idx_messages_user_id ON messages(user_id);