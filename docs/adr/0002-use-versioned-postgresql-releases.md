# Publish versioned data releases in PostgreSQL

## Context

The production release needs a shared store for the importer and administrator approval. Both must be able to work with a candidate while visitors read the active release. The first catalogue uses a read-only SQLite snapshot. [SQLite catalogue decision](0001-serve-the-catalogue-from-a-snapshot.md)

## Decision

Use PostgreSQL for the catalogue, observations, assessments and release history. Manage schema changes with Flyway migrations in version control. A single Spring Boot app serves React, the REST API and release approval. A separate Java command imports the official files in batches. The official files remain the source records for rebuilding the data served by the app. [Designing Data-Intensive Applications, 2nd ed., chapter 1](https://www.oreilly.com/library/view/designing-data-intensive-applications/9781098119058/ch01.html) · [Chapter 11](https://www.oreilly.com/library/view/designing-data-intensive-applications/9781098119058/ch11.html)

Each import writes under its own release ID and records the input URL and checksum. An authorised administrator can approve a candidate after validation. Update the active release and record the approval in one transaction. If the transaction fails, the API continues to serve the previous release.

## Planned verification

Start PostgreSQL in Testcontainers, run Flyway and create an active release. Submit an invalid import and verify that the API returns the same data. Check that an authorised administrator can approve a validated candidate and that the app rejects approval by visitors and users without the role. [Spring Boot Testcontainers support](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
