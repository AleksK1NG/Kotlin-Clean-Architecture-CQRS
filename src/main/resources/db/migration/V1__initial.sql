CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS microservices;


DROP TABLE IF EXISTS microservices.orders CASCADE;
DROP TABLE IF EXISTS microservices.product_items CASCADE;

CREATE TABLE IF NOT EXISTS microservices.accounts
(
    id               UUID PRIMARY KEY,
    email            VARCHAR(60),
    phone            VARCHAR(60),
    country          VARCHAR(255),
    city             VARCHAR(255),
    post_code        VARCHAR(255),

    bio              VARCHAR,
    image_url        VARCHAR,

    balance_amount   BIGINT                   NOT NULL DEFAULT 0,
    balance_currency VARCHAR(10),

    status           VARCHAR(60)              NOT NULL CHECK ( status <> '' ),
    version          BIGINT                   NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS accounts_email_idx ON microservices.accounts (email) include (version);


CREATE TABLE IF NOT EXISTS microservices.orders
(
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(60),
    phone                 VARCHAR(60),
    country               VARCHAR(255),
    city                  VARCHAR(255),
    post_code             VARCHAR(255),
    card_number           VARCHAR(255),
    transaction_id        VARCHAR(255),
    transaction_timestamp TIMESTAMP WITH TIME ZONE,
    status                VARCHAR(60)              NOT NULL CHECK ( status <> '' ),
    version               BIGINT                   NOT NULL DEFAULT 0,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS orders_email_idx ON microservices.orders (email);


CREATE TABLE IF NOT EXISTS microservices.product_items
(
    id          UUID,
    order_id    UUID REFERENCES microservices.orders (id) NOT NULL,
    title       VARCHAR(250),
    description VARCHAR(50000),
    quantity    BIGINT,
    price       BIGINT,
    currency    VARCHAR(250),
    version     BIGINT                                    NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE                  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS product_items_order_id_idx ON microservices.product_items (order_id);

ALTER TABLE microservices.product_items
    ADD CONSTRAINT product_items_id_order_id_unique UNIQUE (id, order_id);



CREATE TABLE IF NOT EXISTS microservices.outbox_table
(
    event_id     UUID PRIMARY KEY,
    event_type   VARCHAR(250)             NOT NULL CHECK ( event_type <> '' ),
    aggregate_id VARCHAR(250)             NOT NULL CHECK ( aggregate_id <> '' ),
    version      SERIAL                   NOT NULL,
    data         BYTEA,
    timestamp    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS outbox_table_aggregate_id_idx ON microservices.outbox_table (aggregate_id);