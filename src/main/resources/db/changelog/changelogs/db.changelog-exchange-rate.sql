--liquibase formatted sql

--changeset p.h:currency-exchange-exchange-rate-1.0.0 logicalFilePath:currency-exchange-exchange-rate-1.0.0

CREATE TABLE IF NOT EXISTS exchange_rate
(
    id                                      varchar(64)     NOT NULL,
    from_currency                           varchar(3)      NOT NULL,
    to_currency                             varchar(3)      NOT NULL,
    rate                                    numeric(10,7)   NOT NULL,
    for_date                                date            NOT NULL,
    created_date                            timestamp       NOT NULL,
    last_modified_date                      timestamp,
    CONSTRAINT pk_exchange_rate PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS exchange_rate_from_currency_to_currency_index ON exchange_rate (from_currency, to_currency);
CREATE INDEX IF NOT EXISTS exchange_rate_for_date_index ON exchange_rate (for_date);

CREATE TABLE IF NOT EXISTS exchange_rate_aud
(
    rev                                     integer         NOT NULL,
    revtype                                 smallint        NOT NULL,
    id                                      varchar(64)     NOT NULL,
    from_currency                           varchar(3),
    to_currency                             varchar(3),
    rate                                    numeric(10,7),
    for_date                                date,
    created_date                            timestamp,
    last_modified_date                      timestamp,
    CONSTRAINT pk_exchange_rate_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_exchange_rate_aud FOREIGN KEY (rev) REFERENCES revinfo (rev)
);