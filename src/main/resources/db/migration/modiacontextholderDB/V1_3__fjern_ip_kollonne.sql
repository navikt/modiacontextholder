ALTER TABLE EVENT
  DROP CONSTRAINT EVENT_PK;

ALTER TABLE EVENT
  ADD CONSTRAINT EVENT_PK PRIMARY KEY (event_id);

ALTER TABLE EVENT
  DROP COLUMN IP;