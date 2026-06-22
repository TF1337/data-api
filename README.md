# data-api

VGWM Data API — a Kotlin/Ktor backend service that exposes a REST API for tours,
checkpoints, observations, locations, categories, company info and users, with
JWT authentication and an integration to a separate Chatbot API.

This project was originally bootstrapped with the
[Ktor Project Generator](https://start.ktor.io).

## Overview

- **Language:** Kotlin (JVM toolchain 21)
- **Framework:** [Ktor](https://ktor.io) server, running on the Netty engine
- **Build tool / package manager:** Gradle (Kotlin DSL) with the Gradle wrapper
- **Group / Version:** `nl.freek.vgwm` / `0.0.1`
- **Persistence:** [Exposed](https://github.com/JetBrains/Exposed) (DAO + JDBC) over
  SQLite, with HikariCP connection pooling
- **Dependency Injection:** [Koin](https://insert-koin.io)
- **Serialization:** kotlinx.serialization (JSON) via Ktor Content Negotiation
- **Auth:** JWT bearer authentication (java-jwt), password hashing via BCrypt
- **HTTP client:** Ktor client (CIO) used to call the Chatbot API
- **Logging:** Logback + Ktor Call Logging

## Requirements

- JDK 21 (the Kotlin JVM toolchain targets 21)
- No local Gradle install required — use the bundled wrapper (`gradlew` / `gradlew.bat`)
- SQLite — no external DB server needed; the database file is created automatically

## Setup & Run

The project uses the Ktor Gradle plugin. From the repository root:

```bash
# Windows (PowerShell / cmd)
.\gradlew.bat run

# macOS / Linux
./gradlew run
```

On a successful start you will see output similar to:

```
INFO  Application - Application started in 0.303 seconds.
INFO  Application - Responding at http://0.0.0.0:8080
```

The service listens on port `8080` by default (configurable via the `PORT`
environment variable; see below).

### Important: JWT_SECRET is required

`JWT_SECRET` has no default and the application fails fast on startup if it is
missing or blank. Set it before running, e.g.:

```bash
# PowerShell
$env:JWT_SECRET = "change-me"; .\gradlew.bat run

# bash
JWT_SECRET=change-me ./gradlew run
```

## Build tasks / Scripts

Common Gradle tasks (provided by the Kotlin JVM and Ktor plugins):

| Task | Description |
|------|-------------|
| `./gradlew run` | Build and run the server (`io.ktor.server.netty.EngineMain`). |
| `./gradlew build` | Compile, assemble and run checks. |
| `./gradlew test` | Run tests (JUnit 5 platform). _TODO: no test sources currently exist under `src/test`._ |
| `./gradlew buildFatJar` | Build a runnable fat JAR (Ktor plugin). |
| `./gradlew buildImage` / `./gradlew runDocker` | Build / run a Docker image (Ktor plugin). _TODO: no `Dockerfile` present; verify image config._ |
| `./gradlew clean` | Remove build outputs. |

Run `./gradlew tasks` to see the full list.

## Entry points

- **Main class:** `io.ktor.server.netty.EngineMain` (configured in `build.gradle.kts`)
- **Ktor module:** `nl.freek.vgwm.ApplicationKt.module`
  (`src/main/kotlin/Application.kt`), referenced from `src/main/resources/application.yaml`
- On startup the application:
  1. Loads configuration from environment variables (`AppConfig.fromEnvironment()`)
  2. Initializes the database (`DatabaseConfig.initialize`) and creates missing
     tables/columns via Exposed `SchemaUtils`
  3. Seeds initial data if needed (`DeHeusSeedData.seedIfNeeded()`)
  4. Installs Koin and configures call logging, status pages, HTTP/CORS,
     serialization, auth and routing

## Environment variables

All configuration is read from environment variables in
`src/main/kotlin/config/AppConfig.kt`:

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `JWT_SECRET` | **Yes** | — | Secret used to sign/verify JWT tokens. Startup fails if missing/blank. |
| `JWT_ISSUER` | No | `vgwm-data-api` | JWT issuer claim. |
| `JWT_AUDIENCE` | No | `vgwm-android` | JWT audience claim. |
| `JWT_EXPIRATION_HOURS` | No | `24` | Token lifetime in hours (positive integer). |
| `DB_URL` | No | `jdbc:sqlite:./vgwm.db` | JDBC URL for the database. |
| `DB_USER` | No | _(empty)_ | Database user. |
| `DB_PASSWORD` | No | _(empty)_ | Database password. |
| `CHATBOT_API_URL` | No | `http://127.0.0.1:8081` | Base URL of the Chatbot API the Data API calls. |
| `UPLOAD_DIR` | No | `./uploads` | Directory for uploaded files (e.g. photos). |
| `PORT` | No | `8080` | HTTP port (positive integer). |

## API endpoints

Routing is wired in `src/main/kotlin/plugins/ConfigureRouting.kt`. The following
route groups are registered (see `src/main/kotlin/api/route/`):

- `healthRoutes` — health check
- `authRoutes` — login / authentication
- `companyInfoRoutes`
- `locationRoutes`
- `categoryRoutes`
- `checkpointRoutes`
- `observationRoutes`
- `tourRoutes`
- `userRoutes`
- `photoRoutes` — photo upload/serving

_TODO: document concrete HTTP methods, paths and request/response payloads per route._

## Tests

- Test dependencies are configured (`ktor-server-test-host`, `kotlin-test-junit5`,
  Kotest assertions) and `tasks.test` uses the JUnit 5 platform.
- _TODO: there are currently no test sources under `src/test`. Add tests and run
  them with `./gradlew test`._

## Project structure

```
data-api/
├── build.gradle.kts          # Build config, dependencies, main class, JVM toolchain
├── settings.gradle.kts       # Gradle settings
├── gradle.properties         # Gradle & Kotlin properties
├── gradle/libs.versions.toml # Version catalog
├── gradlew / gradlew.bat     # Gradle wrapper
├── data/
│   └── vgwm.db               # Default SQLite database file
└── src/
    └── main/
        ├── kotlin/
        │   ├── Application.kt # Ktor module / entry point
        │   ├── api/
        │   │   ├── dto/       # Data transfer objects + mappers
        │   │   └── route/     # HTTP route definitions
        │   ├── config/        # AppConfig, DatabaseConfig
        │   ├── data/          # entity, table, repository, mapper, security, seed, external
        │   ├── di/            # Koin module (AppModule.kt)
        │   ├── domain/        # Domain entities, value objects, services, repositories
        │   └── plugins/       # Ktor plugins (Auth, HTTP/CORS, Logging, Routing, etc.)
        └── resources/
            ├── application.yaml  # Ktor deployment config (port, module)
            └── logback.xml       # Logging config
```

## Features

Selected Ktor features included in this project:

| Name | Description |
|------|-------------|
| Authentication / JWT | Bearer JWT authentication for protected routes. |
| Content Negotiation | Automatic JSON (de)serialization via kotlinx.serialization. |
| CORS | Cross-origin resource sharing support. |
| Status Pages | Centralized error/exception handling. |
| Call Logging | Request logging. |
| Static Content / Photos | Serves uploaded files. |
| Koin (DI) | Dependency injection. |
| Exposed + SQLite + HikariCP | Database access and pooling. |

## License

_TODO: No `LICENSE` file found in the repository. Add a license file and state
the license here._

## Useful links

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- [Exposed](https://github.com/JetBrains/Exposed)
- [Koin](https://insert-koin.io)
