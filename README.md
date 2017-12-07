# show-updates

Hacking together a little application to help us keep track of the TV shows we
watch and notify us when there are new episodes to watch.

Keeping notes here in the README.

## Technologies

* [TVmaze API](http://www.tvmaze.com/api) - a free, easy to use REST API
  providing information about TV shows, episodes, networks, etc.

* [SQLite](https://www.sqlite.org/) - a lightweight, self-contained SQL database
  engine. Using it here as a convenient, but robust way to store data about the
  shows we watch and how caught up we are. The database is kept in a tiny file
  in the local filesystem.

* [yogthos/migratus](https://github.com/yogthos/migratus) for database
  migrations.

* [yada](https://juxt.pro/yada/index.html), a batteries-included library for
  building comprehensive web APIs in Clojure. This is my first time using yada,
  and I'm finding it quite novel and refreshing.

* I'm not sure yet what technology I want to use to build the web frontend.
  I'm considering Hoplon, vs. exploring a ClojureScript React wrapper library of
  some kind. Hoplon would be easiest, since I already know how to use it and I'm
  happy with it.

## Implementation notes

* SQLite database file
  * Keeps track of shows we watch, how caught up we are, and any new episodes
    available since we last watched the show.
  * `show` table
    * `name` (from TVmaze database)
    * `tvmazeid` (the show's ID from the TVmaze database)
    * `bookmark` (a Unix timestamp representing the date of the last episode
      we watched)
      * any episode aired after this timestamp is considered unwatched
    * Can also add `image` (an external URL), `summary`, and other information
      about the show available via the TVmaze API.
  * `episode` table (any record in this table is an unwatched episode)
    * `showid` (the show's ID from the TVmaze database)
      * can query for unwatched episodes by joining on this column
    * `season` (integer)
    * `number` (integer)
    * `airdate` (integer)
    * `name`
    * `summary`
    * `image` (an external URL) also available, among other information from
      TVmaze API
  * `email` table
    * records in this table represent email addresses to notify when there are
      new episodes

* Server
  * Minimal REST API for interacting with the database and the TVmaze API
    (mainly just to search for shows and add them to the database with the
    correct TVmaze IDs and other information about the show from the TVmaze
    API).
  * `/shows`
    * GET, no parameters
    * list of shows we're watching
    * `/show-search`
      * GET with query parameter: `?query=dexter`
      * passes through relevant info from search results of TVmaze API
  * `/add-show`
    * POST with JSON body like `{"tvmazeid": 12345}`
    * Fetches show information from TVmaze API and adds it to our database of
      shows we're following.
  * TODO:
    * A DELETE endpoint to mark an episode as watched.
      * Sets `bookmark` column on the `show` record to on/after the date of that
        episode.
      * Deletes the episode from the `episode` table.
        * ...along with any previous episodes.
    * A list endpoint for email addresses.
    * A PUT endpoint to add email addresses to notify when there are new
      episodes.
    * A DELETE endpoint for email addresses.
    * Authorization of some kind

* Database updater service
  * Periodically checks the TVmaze API for episodes newer than the `bookmark`
    column on each `show` table record that aren't already in the `episodes`
    table from a previous run.
  * Adds unwatched episodes as records in the `episode` table.
  * Notifies us somehow, e.g. via email.
    * To prevent notifying more than once, an email is only sent when an episode
      is added to the table. If the episode is already in the table, then we
      don't add another record or send another email.
  * Could use Quartz scheduler to implement this as a cron task.
  * The TVmaze API is rate limited to allow "at _least_ **20 calls every 10
    seconds** per IP address." They do note that for popular endpoints like
    _shows_ or _episodes_, there is caching and I am not likely to ever hit the
    limit. If I do hit the limit, I will get a 429. They recommend to just back
    off for a few seconds if I get a 429, so that should be sufficient.

* Frontend
  * A web frontend that will make it easy to:
    * See which shows have new episodes.
    * Add a new show that we want to keep up with.
    * Remove a show that we don't want to keep up with.
    * Mark episodes as watched.
