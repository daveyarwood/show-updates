PRAGMA foreign_keys=off;

BEGIN TRANSACTION;

ALTER TABLE show RENAME TO temp_show;

CREATE TABLE show (
  id         INTEGER PRIMARY KEY
  , name     TEXT    NOT NULL
  , bookmark INTEGER
  , tvmazeid INTEGER UNIQUE NOT NULL
);

INSERT INTO show
SELECT name, bookmark, tvmazeid
FROM temp_show;

DROP TABLE temp_show;

COMMIT;

PRAGMA foreign_keys=on;

