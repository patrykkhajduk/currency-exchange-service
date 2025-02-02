--liquibase formatted sql

--changeset p.h:currency-exchange-exchange-rate-1.0.0 logicalFilePath:currency-exchange-exchange-rate-1.0.0

CREATE TABLE IF NOT EXISTS exchange_rate
(
    from_currency                           varchar(3)      NOT NULL,
    to_currency                             varchar(3)      NOT NULL,
    lock_version                            integer         NOT NULL,
    rate                                    numeric(10,7)   NOT NULL,
    for_date                                date            NOT NULL,
    created_date                            timestamp       NOT NULL,
    last_modified_date                      timestamp,
    CONSTRAINT pk_exchange_rate PRIMARY KEY (from_currency, to_currency)
);

CREATE TABLE IF NOT EXISTS exchange_rate_aud
(
    rev                                     integer         NOT NULL,
    revtype                                 smallint        NOT NULL,
    from_currency                           varchar(3)      NOT NULL,
    to_currency                             varchar(3)      NOT NULL,
    lock_version                            integer,
    rate                                    numeric(10,7),
    for_date                                date,
    created_date                            timestamp,
    last_modified_date                      timestamp,
    CONSTRAINT pk_exchange_rate_aud PRIMARY KEY (rev, from_currency, to_currency),
    CONSTRAINT fk_exchange_rate_aud FOREIGN KEY (rev) REFERENCES revinfo (rev)
);