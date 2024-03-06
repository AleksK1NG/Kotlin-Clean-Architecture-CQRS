CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS microservices;


CREATE TABLE IF NOT EXISTS microservices.accounts
(
    id               UUID PRIMARY KEY,
    email            VARCHAR(60) UNIQUE,
    phone            VARCHAR(60) UNIQUE,
    country          VARCHAR(255),
    city             VARCHAR(255),
    post_code        VARCHAR(255),

    bio              VARCHAR,
    image_url        VARCHAR,

    balance_amount   BIGINT                   NOT NULL DEFAULT 0,
    balance_currency VARCHAR(10),

    status           VARCHAR(60),
    version          BIGINT                   NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS accounts_email_idx ON microservices.accounts (email) include (version);


CREATE TABLE IF NOT EXISTS microservices.outbox_table
(
    event_id     UUID PRIMARY KEY,
    event_type   VARCHAR(250)             NOT NULL CHECK ( event_type <> '' ),
    aggregate_id VARCHAR(250)             NOT NULL CHECK ( aggregate_id <> '' ),
    version      BIGINT                   NOT NULL,
    data         BYTEA,
    timestamp    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS outbox_table_aggregate_id_idx ON microservices.outbox_table (aggregate_id) include (version);