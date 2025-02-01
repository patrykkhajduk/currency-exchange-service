--liquibase formatted sql

--changeset p.h:currency-exchange-currency-exchange-account-1.0.0 logicalFilePath:currency-exchange-currency-exchange-account-1.0.0

CREATE TABLE IF NOT EXISTS currency_exchange_account
(
    id                                      varchar(64)     NOT NULL,
    owner_first_name                        varchar(64)     NOT NULL,
    owner_last_name                         varchar(64)     NOT NULL,
    created_date                            timestamp       NOT NULL,
    last_modified_date                      timestamp,
    CONSTRAINT pk_currency_exchange_account PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS currency_exchange_account_owner_first_name_index ON currency_exchange_account (owner_first_name);
CREATE INDEX IF NOT EXISTS currency_exchange_account_owner_last_name_index ON currency_exchange_account (owner_last_name);

CREATE TABLE IF NOT EXISTS currency_exchange_account_aud
(
    rev                                    integer         NOT NULL,
    revtype                                 smallint        NOT NULL,
    id                                      varchar(64)     NOT NULL,
    owner_first_name                        varchar(64),
    owner_last_name                         varchar(64),
    created_date                            timestamp,
    last_modified_date                      timestamp,
    CONSTRAINT pk_currency_exchange_account_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_currency_exchange_account_aud FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE IF NOT EXISTS currency_exchange_account_balance
(
    id                                      varchar(64)     NOT NULL,
    currency_exchange_account_id            varchar(64)     NOT NULL,
    currency                                varchar(3)      NOT NULL,
    amount                                  numeric(10,2)   NOT NULL,
    created_date                            timestamp       NOT NULL,
    last_modified_date                      timestamp,
    CONSTRAINT pk_currency_exchange_account_balance PRIMARY KEY (id),
    CONSTRAINT fk_currency_exchange_account_currency_exchange_account_id FOREIGN KEY (currency_exchange_account_id) REFERENCES currency_exchange_account (id)
);
CREATE INDEX IF NOT EXISTS currency_exchange_account_currency_exchange_account_id_index ON currency_exchange_account_balance (currency_exchange_account_id);

CREATE TABLE IF NOT EXISTS currency_exchange_account_balance_aud
(
    rev                                     integer         NOT NULL,
    revtype                                 smallint        NOT NULL,
    id                                      varchar(64)     NOT NULL,
    currency_exchange_account_id            varchar(64),
    currency                                varchar(3),
    amount                                  numeric(10,2),
    created_date                            timestamp,
    last_modified_date                      timestamp,
    CONSTRAINT pk_currency_exchange_account_balance_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_currency_exchange_account_balance_aud FOREIGN KEY (rev) REFERENCES revinfo (rev)
);