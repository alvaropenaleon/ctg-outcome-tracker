# Closing the Gap Outcome Tracker

## How we work

- Check the working tree and the relevant code before editing. Preserve the user's changes and leave staging, commits, and pushes to the user.
- Work in small changes that can be reviewed and run on their own. For work spanning several changes, outline the steps and how each will be verified before editing. Use `PLAN.md` when the user asks for a persistent handoff.
- Keep the Javalin app runnable during the migration until the replacement has been verified. Keep commit `285b983` available as the visual and behavioural reference.
- Get approval before destructive actions, publishing externally, or creating paid resources. Confirm with the user when the requested scope needs to change substantially.
- Use subagents when the user requests delegation. Give each a bounded task and a completion condition. Assign distinct files to agents that edit, keep review assignments read-only, and verify findings before relying on them.

## Design and SOLID

- Use Java 21, Spring Boot MVC, Spring JDBC, and SQLite for the backend. Keep one deployable application and a separately invoked batch importer. Serve the React UI and API from the same origin in production.
- **Single responsibility.** Keep HTTP handling in `controller/`, application rules in `service/`, and parameterised SQL in `repository/`. Keep data models in `model/` and the JSON contract in `model/dto/`.
- **Open/closed.** Keep the catalogue and detail flow reusable across targets. Extend the existing flow when a target or data source actually needs different behaviour.
- **Liskov substitution.** Keep implementations of a shared contract consistent about results, missing data, and errors. Verify those expectations with the same tests when implementations are interchangeable.
- **Interface segregation.** Give callers only the operations they need. Introduce an interface when there is a real contract to share or a boundary to isolate. Use concrete services where one implementation serves the use case.
- **Dependency inversion.** Use constructor injection. Keep application rules independent of HTTP, JDBC, and the filesystem. Repositories own database access and controllers own transport details.
- Reuse code and dependencies that already fit. Check their documented capabilities before adding a library or building an abstraction. Choose the simplest implementation that meets the current requirement and preserves the behaviour callers rely on.

## Official data and releases

- Use the National Agreement for target definitions and the Productivity Commission for observations and assessments. Keep IDs as strings, including `9a`, `9b`, `15a`, `15b`, and `pr1`–`pr4`.
- Keep the source, reporting period, and unit with each published figure. Record the publication date separately. Model observations, policy targets, projections, and assessments as distinct concepts. In Target 8, trajectory rows represent projections.
- Treat official inputs as the source records and SQLite as derived serving data. Record the exact input URLs and checksums for data releases. Build and validate a candidate snapshot before replacing the approved release. The API opens the approved snapshot in read-only mode.
- Record significant architecture decisions in `docs/adr/` with the reason for the choice and a test or measurement that illustrates it.

## Debugging and verification

- Reproduce a reported bug with the user's steps when possible. Fix the cause and rerun those steps. If an approach fails repeatedly, examine the output and revise the approach before retrying.
- Run `./mvnw verify` from `backend/` after backend changes. Test the HTTP contract through the app and SQLite for catalogue and data changes. Add focused tests for application rules, query behaviour, or regressions where they need independent coverage.
- For runtime changes, check the affected endpoint on the packaged app. Use the [README](README.md) for run commands and an available port if the user already has a server running.
- For UI changes, check the affected flow in a browser. Include relevant edge cases such as missing data, a direct link or refresh on a detail page, and repeated submission on forms.
- Report the files to review, commands run, and actual results. Call out checks still pending and suggest a commit message for a completed change.
- Fold durable, repo-specific corrections into the relevant section when they apply to future work.
