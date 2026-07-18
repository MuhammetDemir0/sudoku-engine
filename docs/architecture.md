# Architecture

Sudoku Engine is a single Spring Boot application that serves both the REST API
and the static browser UI. The frontend source is kept in the top-level
`frontend/` directory, then copied into Spring Boot static resources during the
Maven build.

## Runtime View

```mermaid
flowchart LR
    Browser[Browser UI] -->|HTTP + JSON| Controller[PuzzleController]
    Controller --> DTO[Request/Response DTOs]
    Controller --> Services[Domain Services]
    Services --> Domain[Domain Models]
    Services --> Config[Central Configuration]
    Controller --> Errors[Global API Exception Handler]
    Services --> Solver[Backtracking + MRV Solvers]
    Services --> Generator[Unique Puzzle Generator]
    Services --> Validator[Sudoku Validator]
```

## Repository Layout

```text
sudoku-engine/
+-- backend/
|   +-- src/main/java/com/sudokuengine
|   |   +-- controller/
|   |   +-- dto/
|   |   +-- model/
|   |   +-- service/
|   |   +-- config/
|   +-- src/main/resources/
+-- frontend/
|   +-- index.html
|   +-- css/
|   +-- js/
+-- docs/
```

## Layer Responsibilities

| Layer | Responsibility |
| --- | --- |
| Frontend | Renders the board, handles keyboard and mobile interaction, calls API endpoints, and displays user feedback. |
| Controller | Exposes HTTP endpoints and keeps transport concerns out of the domain model. |
| DTO | Defines request and response shapes with Bean Validation annotations. |
| Service | Implements Sudoku solving, validation, generation, difficulty analysis, and hints. |
| Model | Represents boards, puzzles, hints, solving steps, metrics, and violations. |
| Config | Keeps thresholds, OpenAPI metadata, and environment-specific properties centralized. |
| Exception handling | Maps validation, domain, JSON, and unexpected failures to consistent API errors. |

## Static Frontend Packaging

```mermaid
flowchart TD
    Frontend[frontend/] --> Copy[Maven resources plugin]
    Copy --> Static[target/classes/static]
    Static --> Jar[Spring Boot JAR]
    Jar --> Browser[localhost:8080]
```

The frontend is modular JavaScript without a separate build server. This keeps
local execution and deployment simple: one Spring Boot process serves both
`/api/v1/puzzles/**` and `/`.

## Environment Profiles

```mermaid
flowchart LR
    External[SPRING_PROFILES_ACTIVE] --> Dev[dev]
    External --> Prod[prod]
    External --> Postgres[postgres]
    Dev --> H2[H2 in-memory database]
    Prod --> Safe[Restricted actuator details + production logging]
    Postgres --> EnvSecrets[Database values from environment variables]
```

Profiles are selected externally. Production secrets are not committed; database
credentials are read from environment variables.
