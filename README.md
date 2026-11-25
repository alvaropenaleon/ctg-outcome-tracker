# Closing the Gap Outcome Tracker

A tracker for Australia's National Agreement on Closing the Gap. The original Javalin site shows local Census figures. The Spring Boot API serves a catalogue of the Agreement's 23 targets.

Use JDK 21 for both builds. The backend comes with a Maven Wrapper. Rebuilding the Javalin site requires Maven on your PATH.

## Run the Spring Boot backend

Run these commands from the repo root to build and start the backend.

```sh
cd backend
./mvnw verify
java -jar target/ctg-outcome-tracker-0.1.0-SNAPSHOT.jar --server.address=127.0.0.1
```

Leave that terminal running and check the health endpoint from a second terminal.

```sh
curl -fsS http://127.0.0.1:8080/actuator/health
```

The response should include `"status":"UP"`.

To see the catalogue, run `curl -fsS http://127.0.0.1:8080/api/targets`. Each entry has a string ID, a title, a category, a summary of the target, and a link to the official definition. The API reads the SQLite snapshot under `database/releases/`. See [how the snapshot is built](docs/adr/0001-serve-the-catalogue-from-a-snapshot.md).

## Run the original Javalin site

Open another terminal at the repo root. The app looks for its SQLite database at `database/ctg.db` relative to that directory.

```sh
mvn package
java -jar target/studio-project-0.1-SNAPSHOT-jar-with-dependencies.jar
```

Open <http://localhost:7000>. Both apps can run at the same time. Use Ctrl+C in the terminal running the app you want to stop.

## Credits

https://www.closingthegap.gov.au/national-agreement/targets

https://www.pc.gov.au/closing-the-gap-data/dashboard/

The target summaries are adapted from the National Agreement. © Commonwealth of Australia, Department of the Prime Minister and Cabinet. [Targets and outcomes](https://www.closingthegap.gov.au/national-agreement/targets) · [Priority Reforms](https://www.closingthegap.gov.au/national-agreement/priority-reforms) · [CC BY 4.0 terms](https://www.closingthegap.gov.au/copyright)

The original Javalin project is preserved at commit `285b983`. The RMIT University Studio Project starter code was written by Dr. Timothy Wiley and Prof. Santha Sumanasekara. Copyright RMIT University (c) 2021.
