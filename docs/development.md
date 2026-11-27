# Developer guide

Commands and expected results for each stage of the [roadmap](../PLAN.md). The [README](../README.md) gives the project overview. Record completion evidence in the roadmap by linking a commit, pull request or CI run, or by recording the local command and its result.

Run commands from the repository root unless a section gives another directory. The shell examples use Bash or Zsh.

## Stage 0 — Repository audit and scope

### Check the tools and branch

```sh
git status --short --branch
git log -1 --oneline
java -version
(cd backend && ./mvnw -v)
sqlite3 --version
python3 --version
```

Use JDK 21. The backend Maven Wrapper pins Maven 3.10.1. SQLite and Python are used for the manual checks in this guide. The migration branch is `spring-migration`. For approved tasks, use a short-lived branch from it and review the diff and checks before merging back. [Review workflow](../PLAN.md#review-and-evidence)

### Run the original Javalin site

The original site reads `database/ctg.db` relative to the repository root. If the site is already running on port 7000, start with the HTTP checks below. To build and start it, use system Maven from the repository root.

```sh
mvn -B clean package &&
  java -jar target/studio-project-0.1-SNAPSHOT-jar-with-dependencies.jar
```

Open <http://localhost:7000> or check the seven GET routes from a second terminal.

```sh
for route in / /info.html /page3.html /page4.html /page5.html /page6.html /resources.html; do
  curl -fsS -o /dev/null -w "$route HTTP %{http_code}\n" "http://localhost:7000$route"
done
```

Expect HTTP 200 for all seven routes. These checks cover loading the pages. Form submissions and browser rendering can be checked separately when comparing behaviour during the migration.

## Stage 1 — Spring Boot foundation and CI

### Build and run Spring Boot

Start from the repository root with JDK 21. Build the JAR and launch it from `backend/` so the default SQLite path resolves. The example uses port 8081 to let it run alongside an instance on port 8080. If the intended app is already running on 8081, use the endpoint checks below. To run another instance, choose a free port and use it in the subsequent commands.

```sh
cd backend
./mvnw -B clean verify &&
  java -jar target/ctg-outcome-tracker-0.1.0-SNAPSHOT.jar \
    --server.address=127.0.0.1 --server.port=8081
```

The catalogue baseline passes two tests with zero failures and produces the executable JAR. The health test checks `/actuator/health`. The catalogue test calls the API against the SQLite catalogue and checks all 23 IDs in order, plus selected definitions and source links. [Health test](../backend/src/test/java/org/ctgtracker/TrackerApplicationTest.java) · [Catalogue test](../backend/src/test/java/org/ctgtracker/controller/TargetControllerTest.java)

### Check the API

Leave the server running and open a second terminal.

```sh
curl -fsS http://127.0.0.1:8081/actuator/health
curl -fsS http://127.0.0.1:8081/api/targets | python3 -m json.tool --no-ensure-ascii
```

The health response should include `"status":"UP"`. The catalogue response is a JSON list. Find Target 8 and check its definition against the database query in Stage 2.

Use this check to verify the number of entries and their source links.

```sh
curl -fsS http://127.0.0.1:8081/api/targets | python3 -c '
import json
import sys
from collections import Counter

targets = json.load(sys.stdin)
ids = {target["id"] for target in targets}
assert len(targets) == len(ids) == 23
assert all(isinstance(target_id, str) for target_id in ids)
assert {"8", "9b", "pr4"} <= ids
assert Counter(target["category"] for target in targets) == {
    "socio-economic": 19, "priority-reform": 4,
}
assert all(target["sourceUrl"] for target in targets)
print("PASS: 23 targets, 19 socio-economic and 4 Priority Reforms, with source links")
'
```

The route is handled by [TargetController](../backend/src/main/java/org/ctgtracker/controller/TargetController.java), which calls [TargetService](../backend/src/main/java/org/ctgtracker/service/TargetService.java). [TargetRepository](../backend/src/main/java/org/ctgtracker/repository/TargetRepository.java) runs the SQL query. Spring serialises a list of [TargetResponse](../backend/src/main/java/org/ctgtracker/model/dto/TargetResponse.java) objects into the JSON.

### Check the database path and packaged runtime

If the app starts but catalogue requests fail, check the working directory and the SQLite file. Run the JAR from `backend/`. The default path in [application.properties](../backend/src/main/resources/application.properties) is `../database/releases/catalogue.sqlite` relative to the app's working directory.

After a runtime dependency update, rebuild the JAR and inspect the packaged Tomcat version from the repository root.

```sh
jar tf backend/target/ctg-outcome-tracker-0.1.0-SNAPSHOT.jar |
  grep 'BOOT-INF/lib/tomcat-embed-core-'
```

Compare the version with the approved upgrade and repeat the health and catalogue checks. [Tomcat security advisories](https://tomcat.apache.org/security-11.html)

### CI acceptance — planned

The baseline workflow will run `./mvnw -B clean verify` from `backend/` on a clean Linux runner with JDK 21. Record a successful GitHub Actions run for the workflow after the maintainer pushes the branch. Use the same local build above to diagnose failures.

## Stage 2 — PostgreSQL and Flyway

### Inspect the current SQLite catalogue

Git tracks the two SQL scripts and the SQLite file. [Schema](../backend/src/main/resources/db/schema.sql) · [Seed definitions](../backend/src/main/resources/db/targets.sql) · [SQLite snapshot](../database/releases/catalogue.sqlite)

```sh
git ls-files -- backend/src/main/resources/db/schema.sql \
  backend/src/main/resources/db/targets.sql database/releases/catalogue.sqlite

sqlite3 -readonly -header -column database/releases/catalogue.sqlite \
  "SELECT category, COUNT(*) AS targets FROM targets GROUP BY category;
   SELECT id, title, definition FROM targets WHERE id = '8';"
```

Expect 19 socio-economic targets and four Priority Reforms. The second query should return Target 8, **Employment**, with the policy target of 62% by 2031. Compare this row with the JSON from Stage 1.

The SQLite file is built ahead of time by applying the schema and seed SQL. Spring opens it in read-only mode with `mode=ro`. To see the creation step, build a copy into a temporary file.

```sh
candidate=$(mktemp "${TMPDIR:-/tmp}/ctg-catalogue.XXXXXX")
cat backend/src/main/resources/db/schema.sql backend/src/main/resources/db/targets.sql |
  sqlite3 -bail "$candidate"
sqlite3 -readonly -header -column "$candidate" \
  'PRAGMA integrity_check; SELECT category, COUNT(*) AS targets FROM targets GROUP BY category;'
```

Expect `ok` and the same category counts. [SQLite snapshot decision](adr/0001-serve-the-catalogue-from-a-snapshot.md)

### PostgreSQL acceptance — planned

Run `docker version` and confirm that both **Client** and **Server** are reported before starting Testcontainers work. After the migration is implemented, document the local PostgreSQL start and connection commands here. The tests must show that Flyway can initialise an empty database, a second start preserves the rows and `GET /api/targets` returns the same 23 IDs and fields. [PostgreSQL release decision](adr/0002-use-versioned-postgresql-releases.md)

## Stage 3 — Official data and REST API

### Understand the source URLs

The `sourceUrl` in the catalogue points to the National Agreement page behind the definition. The API reads the title and definition from SQLite and returns that link for reference. The planned importer uses a different source, a pinned Productivity Commission CSV download. It will process a local copy and record the original URL and checksum in the release manifest. [National Agreement](https://www.closingthegap.gov.au/national-agreement/targets) · [Productivity Commission dashboard](https://www.pc.gov.au/closing-the-gap-data/dashboard/)

### Download and verify the Target 8 input

Download the exact CSV linked from outcome area 8 of the July 2026 report. Keep using the same terminal for the next command so `target8_csv` points to the downloaded file. [July 2026 report](https://www.pc.gov.au/closing-the-gap-data/annual-data-report/2026/) · [Target 8 specifications](https://www.pc.gov.au/closing-the-gap-data/dashboard/outcome-area/economic-participation/)

```sh
target8_csv=$(mktemp "${TMPDIR:-/tmp}/ctg-target8.XXXXXX")
curl -fsSL 'https://assets.pc.gov.au/2026-07/adcr-2026-ctg08-economic-participation-dataset.csv?VersionId=DQUlwYoMS0V2rdKXq_yUGykY9imKN6w8' -o "$target8_csv"
shasum -a 256 "$target8_csv"
```

The expected SHA-256 is `34790086f44285d1aa0ac6cd07244bbe11512a4c41e3d3805dfdbf8bb9d15fe0`. This command downloads the source file for inspection. The following check reads the file and prints the selected national values.

```sh
python3 - "$target8_csv" <<'PY'
import csv
import sys

with open(sys.argv[1], encoding="utf-8-sig", newline="") as csv_file:
    rows = [
        row for row in csv.DictReader(csv_file)
        if row["Table_Number"] == "CtG8A.1"
        and row["Age"] == "25-64 years old"
        and row["Sex"] == "All people"
        and row["Indigenous_Status"] == "Aboriginal and Torres Strait Islander people"
        and row["Remoteness"] == "All areas"
        and row["Unit"] == "%"
    ]

actual = sorted(
    (row["Year"], row["Aust"]) for row in rows
    if row["Description7"] == "Actual" and int(row["Year"]) >= 2016
)
assert actual == [("2016", "51.0"), ("2021", "55.7")]
assert any(
    (row["Year"], row["Description7"], row["Aust"]) == ("2026", "Trajectory", "58.3")
    for row in rows
)
for year, value in actual:
    print(f"{year}: {value}% (observed)")
print("2026: 58.3% (trajectory)")
PY
```

The **62% by 2031** figure is the policy target and already appears in the definition. The observed percentages and trajectory will be loaded by the batch importer.

### Importer and detail API acceptance — planned

Add the import command and the detail endpoint checks after implementation. Verify that the importer stores a candidate in PostgreSQL, includes the source URL and checksum, and keeps observations and modelled figures distinct. A failed import must leave the active release readable. Once the detail API is available, check the response against its OpenAPI contract, including an unknown ID and targets with missing published data.

## Stage 4 — Sign-in and authorisation

**Planned.** Document the administrator setup and sign-in steps once implemented. Verify that visitors can browse, an administrator can approve a validated candidate and the approval records who approved it and when. Verify that requests from visitors, users without the role and session-based writes without a CSRF token are rejected. [Acceptance criteria](../PLAN.md#stage-4--sign-in-and-authorisation)

## Stage 5 — React frontend

**Planned.** Add the frontend install, run and test commands after the first React change. Verify the catalogue and detail views using Target 8 with an assessment, Target 17 with a figure and no assessment, and Target 9b with an unavailable figure. Check a direct link, refresh, keyboard access and a narrow viewport. [Acceptance criteria](../PLAN.md#stage-5--react-frontend)

## Stage 6 — Docker, CI and release checks

**Planned.** Record the Docker build command, the PostgreSQL connection settings and the browser smoke-test command once verified. The packaged app must serve the UI and API from the same origin and work against a fresh PostgreSQL database. Record the CI run, image size and memory use. [Acceptance criteria](../PLAN.md#stage-6--docker-ci-and-release-checks)

## Stage 7 — Deploy and release

**Planned.** After approval, record the deployment, initial data load and recovery commands here. Check the HTTPS demo from a fresh browser, time the first request after idle and verify that the previous release can be recovered. Record the release tag, demo URL and walkthrough in the roadmap. [Acceptance criteria](../PLAN.md#stage-7--deploy-and-release)
