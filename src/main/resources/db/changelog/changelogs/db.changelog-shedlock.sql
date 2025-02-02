--liquibase formatted sql

--changeset p.h:currency-exchange-shedlock-1.0.0 logicalFilePath:currency-exchange-shedlock-1.0.0
CREATE TABLE IF NOT EXISTS shedlock
(
    name                varchar(64)       NOT NULL,
    lock_until          timestamp,
    locked_at           timestamp,
    locked_by           varchar(255)      NOT NULL,
    CONSTRAINT pk_shedlock_id PRIMARY KEY (name)
);
