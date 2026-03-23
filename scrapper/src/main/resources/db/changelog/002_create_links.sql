CREATE TABLE IF NOT EXISTS links(
    id BIGINT PRIMARY KEY,
    url TEXT NOT NULL UNIQUE,
    tags TEXT[],
    filters TEXT[],
    last_updated_at TIMESTAMP WITH TIME ZONE
);
