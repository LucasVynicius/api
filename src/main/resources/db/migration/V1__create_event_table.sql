CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE event(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    img_url VARCHAR(512),
    event_url VARCHAR(100) NOT NULL,
    date TIMESTAMP NOT NULL,
    remote BOOLEAN NOT NULL
);
