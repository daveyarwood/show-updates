CREATE TABLE IF NOT EXISTS episode (
  id         INTEGER PRIMARY KEY
  , showid   INTEGER
  , name     TEXT    NOT NULL
  , airdate  INTEGER NOT NULL
  , season   INTEGER
  , number   INTEGER
  , summary  TEXT
  , imageurl TEXT
  , FOREIGN KEY(showid) REFERENCES show(id)
);

