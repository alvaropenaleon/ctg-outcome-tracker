# Closing the Gap production release

This three-week roadmap covers the migration from Javalin to a public Spring Boot and React release. The app must show where its figures come from and allow administrators to approve validated releases. Verification includes checking that visitors can still read the active release after a failed import.

Stages 0–2 are planned for week one, stages 3–5 for week two and stages 6–7 for week three. The maintainer reviews the evidence before approving the next stage.

## Current baseline

Development takes place on `spring-migration`. The catalogue endpoint, `GET /api/targets`, was added in commit `4528d16`. It reads all 23 definitions from `database/releases/catalogue.sqlite` in read-only mode. The Javalin site runs on port 7000 and provides a reference for the React design.

The maintainer commits and pushes the current work before switching computers. On the other machine, clone or update `spring-migration` from `origin`, install JDK 21 and run `(cd backend && ./mvnw verify)` from the repository root. For a fresh Codex session, use `AGENTS.md`, this plan and the recent commits to identify the next task. [Run commands](README.md) · [Agent instructions](AGENTS.md)

## Review and evidence

The maintainer approves the scope and acceptance criteria before work starts. Create a short-lived branch from `spring-migration` for each task. The maintainer reviews the diff and checks before merging the pull request into `spring-migration`. Merge into `main` after the maintainer verifies the release.

Tick an item when there is evidence for it. Record the commit, pull request or CI run. For a local check, record the command and result. Use the task review checklist before merging.

## Stage 0 — Repository audit and scope

- [x] Check the branches and baseline. The 27 November 2026 audit found `main` at `285b983` and `spring-migration` at `c99eee1`. The legacy app files matched the baseline.
- [x] Record what the two apps do. Javalin has seven pages, including LGA and state explorers and gap-score comparisons. Spring Boot has a health endpoint and the target catalogue.
- [x] Build both apps from a clean checkout. `mvn clean package` at the root built the Javalin JAR. `./mvnw clean verify` from `backend/` built the Spring Boot JAR and passed both integration tests.
- [x] Check the running apps and record what to preserve. All seven legacy GET routes returned HTTP 200 on port 7000. The packaged Spring app returned `UP` and 23 string IDs on port 8081. Keep Javalin available for comparison and use official national target figures in the new app.
- [ ] Review the audit and approve the first CI task in Stage 1.

Review the behaviours covered by the tests before approving the next stage. The audit covered the legacy GET routes. Form submissions and browser rendering remain open checks.

## Stage 1 — Spring Boot foundation and CI

- [x] Establish a runnable Java 21 Spring Boot app with the Maven Wrapper, Actuator and a health integration test. See commit `5289507`.
- [x] Establish the controller, service, repository, model and DTO layers with a real catalogue request backed by SQLite. See commit `4528d16` and the [SQLite decision](docs/adr/0001-serve-the-catalogue-from-a-snapshot.md).
- [ ] **Next task.** On `ci/spring-backend`, add a GitHub Actions workflow for pushes and pull requests. A clean Linux runner on a maintained JDK 21 build must run `./mvnw -B clean verify` from `backend/`, pass both tests and build the JAR. Record the first successful run after the maintainer pushes the branch.
- [ ] Patch the Spring Boot runtime in a separate task. The audit found Tomcat 11.0.27 in the JAR. Check the published fixes, confirm the patched version in the rebuilt JAR and rerun the catalogue and health checks. [Tomcat 11 security advisories](https://tomcat.apache.org/security-11.html)
- [ ] Document which settings the app takes from its environment. Check that a clean checkout builds and starts using the README commands.

A successful CI run and a reproducible build are the acceptance criteria. Review the Maven version pinned by the wrapper, what `verify` runs and how Spring connects the application layers.

## Stage 2 — PostgreSQL and Flyway

- [ ] Confirm `docker version` reports a server. During the audit, the installed CLI could not reach the daemon. Review the first PostgreSQL schema with the maintainer, including string target IDs, display order and constraints. [PostgreSQL release decision](docs/adr/0002-use-versioned-postgresql-releases.md)
- [ ] Add Flyway migrations for the initial schema and the 23 definitions. Verify that they initialise an empty PostgreSQL database.
- [ ] Point the existing repository at PostgreSQL. Verify that `GET /api/targets` returns the same 23 string IDs in the same order, with `id`, `category`, `title`, `definition` and `sourceUrl`.
- [ ] Use PostgreSQL Testcontainers to test the migrations, catalogue query and constraints, including duplicate IDs. Verify that a second start against the same database keeps all 23 rows.
- [ ] Update the README with the local PostgreSQL setup and run commands. Follow them from a clean checkout and document where a later schema change goes.

The same catalogue test must pass against both a fresh database and an existing database. Review what Flyway tracks and why the database enforces the constraints.

## Stage 3 — Official data and REST API

- [x] Verify the exact Target 8 input. The download command, checksum and checked figures are below.
- [ ] Build a separate Java command that loads Target 8 into a candidate PostgreSQL release. Test that it reads the two baseline-to-latest observations and keeps the 2026 modelled figures out of the observation series.
- [ ] Record each release's input URL and SHA-256. Store observations, policy targets, projections and published assessments distinctly. Test that a failed import leaves the active release readable through the API.
- [ ] Add `GET /api/targets/{id}` with a documented JSON response and a `404` for an unknown ID. The maintainer implements or pairs on the first detail endpoint and its test to understand the full request path.
- [ ] Write the REST contract in `docs/api/openapi.yaml` and add examples in `docs/api.md`. Check the documented paths, responses, missing-data cases and errors against the API tests.
- [ ] Add the remaining national headlines and published assessments from the pinned July 2026 report. Verify coverage of 18 socio-economic targets with figures and 15 with assessments. Keep Target 9b in the catalogue with unavailable data. [July 2026 report, section 1.1](https://assets.pc.gov.au/2026-07/closing-the-gap-annual-data-compilation-july-2026.pdf?VersionId=sR70aMYImFC.fHxOHFvTn_G9y7S_ye1d#page=12)

Repeat the Target 8 import from the pinned file and check the API against the report. Review the row filter, the response DTO and how the active release stays available after a failed import.

### Target 8 input and expected results

Use the CSV from outcome area 8 of the 29 July 2026 report. The Target 8 page gives the population and measure to look for. [Annual Data Compilation Report](https://www.pc.gov.au/closing-the-gap-data/annual-data-report/2026/) · [Target 8 specifications](https://www.pc.gov.au/closing-the-gap-data/dashboard/outcome-area/economic-participation/)

```sh
target8_csv=$(mktemp "${TMPDIR:-/tmp}/ctg-target8.XXXXXX")
curl -fsSL 'https://assets.pc.gov.au/2026-07/adcr-2026-ctg08-economic-participation-dataset.csv?VersionId=DQUlwYoMS0V2rdKXq_yUGykY9imKN6w8' -o "$target8_csv"
shasum -a 256 "$target8_csv"
```

The SHA-256 for this download is `34790086f44285d1aa0ac6cd07244bbe11512a4c41e3d3805dfdbf8bb9d15fe0`. Record the URL and hash in the release manifest.

- Start with `Table_Number = CtG8A.1`, `Age = 25-64 years old`, `Sex = All people`, `Indigenous_Status = Aboriginal and Torres Strait Islander people` and `Remoteness = All areas`. Read the national `Aust` value. For observations, take `Unit = %` and `Description7 = Actual` from the 2016 baseline onwards.
- Expect 51.0% in 2016 and 55.7% in 2021. The CSV also contains earlier observations, numerator and denominator counts, a 58.3% trajectory for 2026 and a 60.4% regression estimate for 2026. The policy target is 62% by 2031.
- Keep the published assessment with its assessment period and report date. Give each displayed figure its source, reporting period and unit. Record the publication date separately.

## Stage 4 — Sign-in and authorisation

- [ ] Agree how to create and recover the administrator account. Record who can read targets or approve a release and how credentials are supplied.
- [ ] Add Spring Security sessions. Verify that visitors can read targets and a valid administrator sign-in creates a working session.
- [ ] Add administrator-only approval. Verify that the active release and the record of who approved it and when are written in one transaction. Keep the previous release available for recovery.
- [ ] Use JUnit and Mockito to check that the approval service rejects an unvalidated release and leaves the active release alone.
- [ ] Use PostgreSQL Testcontainers to check successful approval and rejection of anonymous requests, signed-in users without the role and session-based writes without a CSRF token.

The test results must show a successful approval and the expected rejections. Review how the server checks identity and permission to publish, and what the CSRF check protects.

## Stage 5 — React frontend

- [ ] Set up React, TypeScript and Vite. Verify that a clean `npm ci` and `npm run build` work from `frontend/`.
- [ ] Build a catalogue view for all 23 targets and one reusable detail view. Check that both read the documented API.
- [ ] Verify Target 8 with an assessment, Target 17 with a figure and no assessment, and Target 9b with no published figure. Check the source, period and unit beside each figure shown.
- [ ] Add the administrator screen for sign-in, review and approval of a validated candidate. Confirm that approving one changes what the public view shows.
- [ ] Check loading, empty and error states. Open and refresh a direct detail link. Check keyboard access and a narrow viewport.
- [ ] Run browser tests for public browsing and administrator approval. Compare the reused design elements against the running Javalin site.

In a browser, follow a catalogue link, refresh the detail screen and publish a release. Review where the UI keeps its state and how it gets each value from the API.

## Stage 6 — Docker, CI and release checks

- [ ] Build a multi-stage Docker image with the React build and Spring Boot JAR. Run PostgreSQL separately and verify that the UI and API work from the same origin.
- [ ] Run backend tests, frontend checks and the image build in GitHub Actions. Record a passing run for the combined release branch.
- [ ] Check that Actuator reports the app's health and the logs explain a failed start. Put the release ID and validation result in structured import logs. Check that the logs and repository contain no database passwords or admin credentials.
- [ ] Start the image against a fresh PostgreSQL database using the documented settings. Run browser smoke tests for public browsing, a detail page and administrator approval.
- [ ] Record the image size and memory use and review the dependency scan before release. Compare the measurements with the limits of the proposed host.

A developer with Docker must be able to reproduce the app from a checkout. Review what the image contains and where the app gets its configuration.

## Stage 7 — Deploy and release

- [ ] Recheck the hosting limits and get approval before publishing externally or creating paid resources. The proposed free demo uses Render with Neon PostgreSQL. At the last check, Render's free database expired after 30 days and Neon Free offered 1 GB per project with compute suspension after five idle minutes. [Render free services](https://render.com/docs/free) · [Render compute plans](https://render.com/docs/compute-plans) · [Neon plans](https://neon.com/docs/introduction/plans)
- [ ] Configure the hosted database and application secrets. Run Flyway and check it recorded the expected migrations.
- [ ] Deploy over HTTPS, then load and approve the first official data release. Verify from a fresh browser that visitors see it and that administrators can sign in.
- [ ] Time the first request after the app has been idle. Record the result in the demo notes.
- [ ] Check the installation, API and architecture docs against the deployed app. Verify how to recover the previous release and record a short walkthrough for recruiters.
- [ ] Review the Git diff and release checks, merge the approved migration into `main` and tag the release. Record the tag and demo URL here.

The release is ready once the demo is verified, the build is reproducible and the previous approved release can be recovered using the documented procedure.

## Task review checklist

Copy this into each task or pull request. Record the command and result next to each check that applies.

```markdown
- [ ] The maintainer approved the scope and recorded evidence for each acceptance criterion.
- [ ] The build and relevant automated tests passed. The review records the results and any skipped checks.
- [ ] The review covers failure and boundary cases, with a manual check where applicable.
- [ ] The maintainer checked the diff for unrelated changes and security issues, and reviewed the reasons for new dependencies or abstractions.
- [ ] The documentation matches what the code does.
- [ ] The maintainer can explain what changed, why the approach was chosen and how it was verified.
- [ ] The maintainer approved the change for integration.
```

## Working away

Before going offline, build once while connected to cache the Maven distribution and dependencies. Run `./mvnw -o verify` from `backend/` to check the cache. Once the PostgreSQL tests are in place, check Docker locally and pull the test images before leaving. Save the CSV locally when working on the importer.

The branch can be opened in a browser through GitHub Codespaces. The current dev container uses Java 16. Update it to JDK 21 and the frontend's Node version, then run the checks before using Codespaces for development. [GitHub Codespaces setup](https://docs.github.com/en/codespaces/setting-up-your-project-for-codespaces)
