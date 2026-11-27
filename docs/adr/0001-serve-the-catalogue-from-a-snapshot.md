# Serve the catalogue from a SQLite snapshot

## Context

The first Spring Boot catalogue serves 23 target definitions. A local database file meets the needs of this read-only catalogue without a separate database service.

## Decision

Spring Boot opens `database/releases/catalogue.sqlite` in read-only mode with `mode=ro`. The National Agreement is the source of record. The SQL contains the target summaries and source URLs so reviewers can see changes in a diff. The SQLite snapshot is built from that SQL for the API to serve. [Designing Data-Intensive Applications, 2nd ed., chapter 1](https://www.oreilly.com/library/view/designing-data-intensive-applications/9781098119058/ch01.html)

Build each candidate in a separate file so a failed build leaves the current snapshot available.

## Verification

Run these commands from the repository root to build and check a candidate.

```sh
candidate=$(mktemp "${TMPDIR:-/tmp}/ctg-catalogue.XXXXXX")
cat backend/src/main/resources/db/schema.sql backend/src/main/resources/db/targets.sql | sqlite3 -bail "$candidate"
sqlite3 -readonly "$candidate" 'PRAGMA integrity_check; SELECT category, count(*) FROM targets GROUP BY category;'
```

The check should return `ok`, four Priority Reform targets and 19 socio-economic targets. The API integration test checks all 23 IDs as strings and verifies a sample of definitions and source links.
