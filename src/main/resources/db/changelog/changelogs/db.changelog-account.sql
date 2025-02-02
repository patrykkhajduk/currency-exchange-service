--liquibase formatted sql

--changeset p.h:currency-exchange-account-1.0.0 logicalFilePath:currency-exchange-account-1.0.0

CREATE TABLE IF NOT EXISTS account
(
    id                                      varchar(64)     NOT NULL,
    lock_version                            integer         NOT NULL,
    owner_first_name                        varchar(64)     NOT NULL,
    owner_last_name                         varchar(64)     NOT NULL,
    created_date                            timestamp       NOT NULL,
    last_modified_date                      timestamp,
    CONSTRAINT pk_account PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS account_owner_first_name_index ON account (owner_first_name);
CREATE INDEX IF NOT EXISTS account_owner_last_name_index ON account (owner_last_name);

CREATE TABLE IF NOT EXISTS account_aud
(
    rev                                     integer         NOT NULL,
    revtype                                 smallint        NOT NULL,
    id                                      varchar(64)     NOT NULL,
    lock_version                            integer,
    owner_first_name                        varchar(64),
    owner_last_name                         varchar(64),
    created_date                            timestamp,
    last_modified_date                      timestamp,
    CONSTRAINT pk_account_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_account_aud FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE IF NOT EXISTS sub_account
(
    id                                      varchar(64)     NOT NULL,
    lock_version                            integer         NOT NULL,
    main_account_id                         varchar(64)     NOT NULL,
    currency                                varchar(3)      NOT NULL,
    amount                                  numeric(10,7)   NOT NULL,
    created_date                            timestamp       NOT NULL,
    last_modified_date                      timestamp,
    CONSTRAINT pk_sub_account PRIMARY KEY (id),
    CONSTRAINT fk_sub_account_main_account_id FOREIGN KEY (main_account_id) REFERENCES account (id)
);
CREATE INDEX IF NOT EXISTS sub_account_main_account_id_index ON sub_account (main_account_id);

CREATE TABLE IF NOT EXISTS sub_account_aud
(
    rev                                     integer         NOT NULL,
    revtype                                 smallint        NOT NULL,
    id                                      varchar(64)     NOT NULL,
    lock_version                            integer,
    main_account_id                          varchar(64),
    currency                                varchar(3),
    amount                                  numeric(10,7),
    created_date                            timestamp,
    last_modified_date                      timestamp,
    CONSTRAINT pk_sub_account_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_sub_account_aud FOREIGN KEY (rev) REFERENCES revinfo (rev)
);