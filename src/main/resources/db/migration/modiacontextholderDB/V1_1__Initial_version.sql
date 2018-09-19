-- ROLLBACK-START
------------------
-- DROP SEQUENCE EVENT_ID_SEQ;
-- DROP TABLE EVENT;
---------------
-- ROLLBACK-END

CREATE SEQUENCE EVENT_ID_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE EVENT (
  event_id          NUMBER(19, 0)      NOT NULL,
  veileder_ident    VARCHAR(7)         NOT NULL,
  event_type        VARCHAR(50)        NOT NULL,
  ip                VARCHAR(255)       NOT NULL,
  verdi             VARCHAR(255)       NOT NULL,
  created           TIMESTAMP          NOT NULL,
  CONSTRAINT EVENT_PK PRIMARY KEY (event_id, ip)
);
