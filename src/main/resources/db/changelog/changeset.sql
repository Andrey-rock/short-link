-- liquibase formatted sql

-- changeset andrey-rock:1

CREATE TABLE IF NOT EXISTS links
(
    code CHAR(6) PRIMARY KEY,
    link TEXT NOT NULL
);