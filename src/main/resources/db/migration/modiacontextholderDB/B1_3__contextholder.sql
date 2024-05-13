CREATE TABLE EVENT (
    event_id          BIGSERIAL          PRIMARY KEY,
    veileder_ident    VARCHAR(7)         NOT NULL,
    event_type        VARCHAR(50)        NOT NULL,
    verdi             VARCHAR(255)       NOT NULL,
    created           TIMESTAMP          NOT NULL
);