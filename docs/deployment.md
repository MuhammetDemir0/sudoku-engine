# Live Deployment

This project is configured for Render because it supports Docker-based Java
applications, provides a managed `PORT` environment variable, restarts unhealthy
web services, and can use `/actuator/health` as the health endpoint.

## Runtime Configuration

The application reads the HTTP port from `PORT` and falls back to `8080` for
local development:

```properties
server.port=${PORT:8080}
```

The Docker image starts with the `production` Spring profile by default. The
production profile disables the H2 console, limits public actuator exposure, and
keeps logging at `INFO`.

## Render Deployment

The repository includes `render.yaml`, so a Render Blueprint can create the web
service from the repository root.

1. Connect the GitHub repository to Render.
2. Create a new Blueprint from `render.yaml`.
3. Deploy the `sudoku-engine` web service.
4. Confirm that `/actuator/health` returns `{"status":"UP"}`.
5. Open the service URL and verify the frontend loads.
6. Call one API endpoint, for example `POST /api/v1/puzzles/generate`.

Render injects `PORT` automatically. The service also sets
`SPRING_PROFILES_ACTIVE=production`.

## Repository Website Field

After Render creates the service URL, add that URL to the GitHub repository
Website field:

```bash
gh repo edit MuhammetDemir0/sudoku-engine --homepage https://<render-service-url>
```

Replace `https://<render-service-url>` with the actual Render URL.

## Restart Recovery

The Dockerfile and Render service both use `/actuator/health`. If the service
becomes unhealthy, the platform can restart it and health checks will verify
that the application recovered.
