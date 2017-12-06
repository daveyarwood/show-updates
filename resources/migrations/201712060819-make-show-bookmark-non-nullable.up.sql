PRAGMA foreign_keys=off;
--;;
ALTER TABLE show RENAME TO temp_show;
--;;
CREATE TABLE show (
  id         INTEGER PRIMARY KEY
  , name     TEXT    NOT NULL
  , bookmark INTEGER NOT NULL
  , tvmazeid INTEGER UNIQUE NOT NULL
);
--;;
-- Skipping the data copy part here because there is no data yet; if there were,
-- we would have to figure out what to put when bookmark is null.
-- INSERT INTO show
-- SELECT name, bookmark, tvmazeid
-- FROM temp_show;
DROP TABLE temp_show;
--;;
PRAGMA foreign_keys=on;

