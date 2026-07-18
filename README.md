# Sudoku Engine

[![CI](https://github.com/MuhammetDemir0/sudoku-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/MuhammetDemir0/sudoku-engine/actions/workflows/ci.yml)

A full-stack Sudoku generation, solving, hinting, validation, and algorithm visualization platform built with Java 21, Spring Boot, Maven, Docker, and vanilla JavaScript.

## Project Layout

```text
sudoku-engine/
+-- backend/                 # Spring Boot API, domain, services, tests, Maven build
|   +-- pom.xml
|   +-- src/
+-- frontend/                # Browser UI source served by the Spring Boot app
|   +-- index.html
|   +-- css/
|   +-- js/
+-- docs/                    # Project documentation
+-- Dockerfile               # Production container build
+-- docker-compose.yml       # Local one-command container startup
+-- .github/workflows/ci.yml # GitHub Actions CI pipeline
```

The frontend is kept as a top-level source folder for clarity. During the Maven build, `backend/pom.xml` copies `frontend/` into the packaged Spring Boot static resources, so no separate frontend server is required.

## Quick Start

### Run With Docker Compose

```bash
docker compose up --build
```

Open `http://localhost:8080`.

### Run Locally With Maven

```bash
cd backend
mvn spring-boot:run
```

Open `http://localhost:8080`.

### Optional Database Tools

The application uses H2 by default. PostgreSQL and pgAdmin remain available through an optional Compose profile:

```bash
docker compose --profile database up --build
```

PostgreSQL runs on `localhost:5432`; pgAdmin runs on `localhost:5050`.

## Build And Test

From `backend/`:

```bash
mvn clean verify
```

From the repository root:

```bash
docker build -t sudoku-engine:local .
```

The CI pipeline runs compile, tests, verification, packaging, and Docker build checks on every push and pull request.

## API

Main endpoints are available under `/api/v1/puzzles`:

```text
POST /api/v1/puzzles/generate
POST /api/v1/puzzles/solve
POST /api/v1/puzzles/validate
POST /api/v1/puzzles/hint
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html`.
