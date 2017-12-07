PRAGMA foreign_keys=off;
--;;
ALTER TABLE show RENAME TO temp_show;
--;;
CREATE TABLE show (
  id         INTEGER PRIMARY KEY
  , name     TEXT    NOT NULL
  , bookmark INTEGER NOT NULL
  , tvmazeid INTEGER UNIQUE NOT NULL
  , summary  TEXT
  , imageurl TEXT
);
--;;
INSERT INTO show
SELECT id, name, bookmark, tvmazeid, null, null
FROM temp_show;
--;;
DROP TABLE temp_show;
--;;
PRAGMA foreign_keys=on;

