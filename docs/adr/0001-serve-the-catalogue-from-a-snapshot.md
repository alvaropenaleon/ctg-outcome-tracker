# Serve the catalogue from a SQLite snapshot

Spring Boot reads the 23 target definitions from `database/releases/catalogue.sqlite`. The catalogue is small, and updating it means releasing a new snapshot, so the app can read a local file without running a separate database service. SQLite opens the file with `mode=ro`.

The National Agreement is the source of record. We keep the summaries and their source URLs in SQL so changes can be reviewed in a diff. The SQLite file is derived from that SQL and is the copy the app serves. This follows the distinction between systems of record and derived data in [Designing Data-Intensive Applications, 2nd ed., chapter 1](https://www.oreilly.com/library/view/designing-data-intensive-applications/9781098119058/ch01.html).

Build a candidate in a separate file so an error leaves the serving copy available. From the repo root, use `sqlite3` to rebuild and inspect it.

```sh
candidate=$(mktemp "${TMPDIR:-/tmp}/ctg-catalogue.XXXXXX")
cat backend/src/main/resources/db/schema.sql backend/src/main/resources/db/targets.sql | sqlite3 -bail "$candidate"
sqlite3 -readonly "$candidate" 'PRAGMA integrity_check; SELECT category, count(*) FROM targets GROUP BY category;'
```

Expect `ok`, four Priority Reform targets, and 19 socio-economic targets. The HTTP integration test reads the serving snapshot and checks the full list of string IDs and a sample of definitions and source links.
