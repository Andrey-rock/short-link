-- liquibase formatted sql

-- changeset andrey-rock:1

CREATE TABLE IF NOT EXISTS links
(
    code CHAR(10) PRIMARY KEY,
    link TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE
);