-- liquibase formatted sql

-- changeset andrey-rock:1

CREATE TABLE IF NOT EXISTS links
(
    code CHAR(10) PRIMARY KEY,
    link TEXT NOT NULL,
    created_at TIMESTAMP,
    expires_at TIMESTAMP
);