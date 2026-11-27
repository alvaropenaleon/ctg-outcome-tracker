# Closing the Gap Outcome Tracker

## Development workflow

- Inspect the working tree and relevant code before editing. Preserve existing edits. The maintainer reviews the diff and handles staging, commits and pushes.
- Keep each change small enough to review and run on its own. For work that spans several changes, outline the steps and the checks for each one. Use `PLAN.md` when the maintainer requests a handover between sessions.
- Keep Javalin runnable until the replacement has been verified. Use commit `285b983` as the reference for the original design and behaviour.
- Obtain approval before making a destructive change, publishing externally, creating paid resources or making a substantial change to the scope.
- Use subagents when requested. Give each a specific task and a completion criterion. Assign separate files for edits, keep review assignments read-only and verify findings before acting on them.
- Write documentation in plain British English. Use direct instructions for setup and testing. Explain what each decision solves and how to verify it.

## Design and SOLID

- The production backend uses Java 21, Spring Boot MVC, Spring JDBC, PostgreSQL and Flyway. Deploy one app that serves React and the API from the same origin. Run the batch importer separately.
- Put schema changes in Flyway migrations. Once a migration has run on a shared database, put the next change in a new migration.
- **Single responsibility.** Put HTTP handling in `controller/`, application rules in `service/` and parameterised SQL in `repository/`. Keep data models in `model/` and the JSON contract in `model/dto/`.
- **Open/closed.** Use the same catalogue and detail flow for all targets. Extend it where a target or data source needs different behaviour.
- **Liskov substitution.** If two implementations can stand in for each other, they should agree on what they return and how they handle missing data and errors. Run the same contract tests against both.
- **Interface segregation.** Give callers the operations they need. Use a concrete service for a single implementation. Add an interface when there's a shared contract or a boundary worth isolating.
- **Dependency inversion.** Use constructor injection. Keep HTTP, database and file handling outside the application rules.
- Check the code and libraries already in use before adding a dependency or abstraction. Use the simplest solution that meets the requirements and preserves the behaviour callers rely on.

## Official data and releases

- Get definitions from the National Agreement and observations and assessments from the Productivity Commission. Keep target IDs as strings, including `9a`, `9b`, `15a`, `15b` and `pr1`–`pr4`.
- Give each figure a source, reporting period and unit. Record the publication date separately. Keep observations, policy targets, projections and assessments distinct in the data model and on screen. A trajectory row in Target 8 is a projection.
- The official files are the source records. PostgreSQL stores what the app serves. Record the exact input URLs and checksums, then load and validate each candidate under its own release ID. Publish a validated candidate in one transaction and keep the last release available for recovery.
- Anyone can read the active catalogue and target details. Use Spring Security for sign-in and check the administrator role on the server before approving a release. Protect session-based writes from CSRF, record who published the release and when, and keep credentials out of Git.
- Record significant architecture decisions in `docs/adr/`. Include the reason for each decision and a test or measurement that supports it.

## Debugging and verification

- Reproduce a reported bug using the supplied steps. Fix the cause and repeat the steps. If the failure recurs, review the output and revise the approach before retrying.
- Run `./mvnw verify` from `backend/` after backend changes. Use JUnit and Mockito for application rules. Run Flyway against PostgreSQL in Testcontainers and use it to test the API, queries, authorisation and failed imports.
- For a runtime change, start the packaged app and check the affected endpoint. Follow the run commands in the [README](README.md) and use a free port when another server is running.
- Open the affected screen in a browser after UI changes. Check the cases that matter for that screen, such as missing data, opening or refreshing a detail link, or submitting a form twice.
- Identify the files to review and report the commands run, their results and any outstanding checks. Suggest a commit message.
- Record corrections that apply to future tasks under the relevant rule.
