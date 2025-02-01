--liquibase formatted sql

--changeset p.h:currency-exchange-audit-1.0.0 logicalFilePath:currency-exchange-audit-1.0.0
CREATE TABLE IF NOT EXISTS revinfo
(
    rev                            integer       NOT NULL,
    revtstmp                       bigint        NOT NULL,
    CONSTRAINT pk_id PRIMARY KEY (rev)
);
CREATE SEQUENCE revinfo_seq INCREMENT 50 MINVALUE 1;