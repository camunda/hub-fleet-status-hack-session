# Hub fleet status

Camunda Hub manages access policies for many runtime environments. Each environment runs an agent that applies policy versions and reports back to Hub.

**Today is Tuesday 2026-11-03, 10:15 UTC** (the application clock is fixed to that instant).

Your job: build the Hub-side API that ingests the agent reports and tells an administrator what each environment has actually applied versus what Hub wants it to have.

## Prerequisites

The JDK from `.tool-versions` (Java 25). Nothing else: Maven comes from the wrapper, the database is embedded H2, the UI has no build step.

## Run

```
./mvnw spring-boot:run
```

Open http://localhost:8080. The UI has three read-only pages: Fleet, Environment detail and Policy coverage. Until you implement the API they show a "not implemented yet" state.

## Test

```
./mvnw test
```

## Replay the agent log

`agent-log/reports.ndjson` holds the raw messages the agents sent this morning (06:00 to 10:15 UTC), one JSON object per line, in the order Hub received them. `_arrivedAt` is when Hub received the line; the rest is exactly what the agent sent.

`ReportReplayer` reads the file in order and, for each line, sets the application clock to the line's `_arrivedAt`, removes that field, and sends the rest unchanged (including `clientId`) as one synchronous HTTP request (`Content-Type: application/json`) to your ingestion endpoint. Because the server runs in the same JVM and reads the same `Clock` bean, Hub's receive time during the request equals `_arrivedAt`. There are no retries; 4xx/5xx responses and connection errors are logged (line number, `messageId`, environment, kind, status, first 200 characters of the response) and the replay continues. A summary of messages per status code is logged at the end.

You design the endpoint, so you tell the replayer where it is. `hub.replay.url` is required (there is no default); `hub.replay.method` defaults to `POST`. Without a URL the replay does not start and logs which property to set.

```
./mvnw spring-boot:run -Dspring-boot.run.arguments="--replay --hub.replay.url=http://localhost:8080/api/reports"
```

replays the log once the web server is up and keeps serving, so you can look at the result in the UI. `ReplayerTest` shows how to drive the replayer against a test endpoint. Afterwards the clock is back at 10:15. The replayer sends HTTP only; do not bypass it.

## Layout

| Path | Contents |
|---|---|
| `src/main/java/io/camunda/hub` | Spring Boot app, packaged by feature: `policy` (entities, repository, `GET /api/policies`), `fleet` (`FleetController`, stubs), `replay` (`ReportReplayer`, `ReplayRunner`), `clock` |
| `src/main/resources/db/migration` | Flyway schema and seed data (clusters, environments, link history, policies, versions) |
| `src/main/resources/static` | Plain HTML/JS UI |
| `agent-log/reports.ndjson` | The raw agent messages |
| `src/test` | A few tests on the Hub data and the stubs |

Hub data: `cluster` (one machine client `<cluster>-agent` per cluster), `environment`, `environment_policy_link` (link history), `policy` (with `copied_from_*`), `policy_version` (`content` is JSON `{"roles":[{"id","members"}]}`). An environment's desired version is the latest version of its currently linked policy.

Already implemented: `GET /api/policies` returns policies with their versions (`version`, `publishedAt`, `summary`).

## Read contract (fixed)

The UI depends on these three endpoints. They currently return `501` with an empty body. Do not change their shape.

```
GET /api/fleet/environments
[ { "id", "name", "cluster", "phase",
    "policy": { "id", "name" },
    "desiredVersion", "appliedVersion" | null,
    "status", "statusDetail", "lastHeardAt" | null } ]

GET /api/fleet/environments/{id}
  same fields plus "history": [ { "at", "summary" } ]

GET /api/policies/{policyId}/versions/{version}/coverage
{ "policy", "version", "publishedAt", "complete": true | false,
  "targets": [ { "environmentId", "name", "status", "detail" } ] }
```

Status values are your choice; the UI only treats `IN_SYNC` specially (green badge, everything else amber).

## What is yours to design

The ingestion contract, the storage and the logic are yours. You may change any code, schema or seed except the read contract above.
