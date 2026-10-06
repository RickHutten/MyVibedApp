# Personal Ambient Dashboard

A personal dashboard for a tablet or screen in the home that continuously shows useful information for the current moment.

## Concept

The app combines important daily information in one place instead of requiring the user to open several separate apps.

It may show:

- Current weather and forecast
- To-do tasks
- Calendar and schedule
- When to leave for work based on live travel data
- Reminders and things the user asked it to remember
- Context-aware suggestions and alerts

The dashboard should update in real time and prioritize what is relevant now.

## Example

> Leave in 15 minutes. Traffic is heavier than usual. Rain is expected this afternoon. You have two tasks due today.

## Target Experience

The primary display is a tablet mounted or placed somewhere in the home. The application should also work in a normal web browser.

## Base Technology

- Backend: Spring Boot with Java 25
- Frontend: Angular
- Database: PostgreSQL
- Real-time updates between backend and frontend

## Local development

Run the backend with `./mvnw spring-boot:run` from `backend/` and the frontend
with `npm start` from `frontend/`. The Angular development server proxies
`/api` requests to the backend on port 8080.

On a Sopra-managed macOS device, allow Java to use certificates from the
system keychain when starting the backend:

```sh
JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=KeychainStore ./mvnw spring-boot:run
```

### Local PostgreSQL

The backend uses PostgreSQL 18.6. Build and start the local database image from
the repository root:

```sh
export POSTGRES_PASSWORD='choose-a-local-password'
docker build -t myvibedapp-postgres docker/postgres
docker run --name myvibedapp-postgres \
  --env POSTGRES_PASSWORD \
  --publish 5432:5432 \
  --detach \
  myvibedapp-postgres
```

Start the backend with the single local profile and the same password:

```sh
SPRING_PROFILES_ACTIVE=local \
POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
./mvnw spring-boot:run
```

The local profile's connection settings are in
`backend/src/main/resources/application-local.properties`; the file is ignored
by Git and reads the password from `POSTGRES_PASSWORD`.

### Backend tests

The backend integration tests use Testcontainers with PostgreSQL, so `./mvnw test`
requires a Docker-compatible daemon. Run it from `backend/`. This project uses
Podman without Testcontainers Desktop.

Install Podman's macOS Docker-socket helper once so IntelliJ, Maven, and
Testcontainers can discover the same global socket:

```sh
PODMAN_HELPER="$(brew --prefix podman)/bin/podman-mac-helper"
sudo "$PODMAN_HELPER" install
podman machine start podman-machine-default
ls -l /var/run/docker.sock
```

If the helper reports that it cannot claim the global Docker socket after a
Homebrew/Podman upgrade, reinstall it with the current Homebrew path:

```sh
PODMAN_HELPER="$(brew --prefix podman)/bin/podman-mac-helper"
sudo "$PODMAN_HELPER" uninstall
sudo "$PODMAN_HELPER" install
podman machine stop podman-machine-default
podman machine start podman-machine-default
```

Rootless Podman requires Ryuk to be disabled. Add
`TESTCONTAINERS_RYUK_DISABLED=true` to the IntelliJ JUnit/Maven run configuration
(or its template), and use the same variable from a terminal:

```sh
TESTCONTAINERS_RYUK_DISABLED=true ./mvnw test
```

No `DOCKER_HOST` or per-run socket path is needed once `/var/run/docker.sock` is
provided by the Podman helper.

### Backend static analysis

Run the automatic cleanup and checks from `backend/`:

```sh
./mvnw spotless:apply
./mvnw pmd:pmd
./mvnw verify
```

Spotless checks imports and fully qualified type names. Error Prone runs during
compilation. PMD runs broad best-practice, style, design, documentation,
error-prone, multithreading, performance, and security checks. PMD also runs
CPD to detect duplicated code. Reports are written to
`backend/target/reports/pmd.html` and `backend/target/reports/cpd.html` without
failing the default build, because framework entry points and public APIs require
review before removal or suppression.

For an enforced PMD check that fails when the report contains any violation, run:

```sh
./mvnw -q pmd:pmd pmd:check -Dpmd.failOnViolation=true -DskipTests
```

The normal Maven configuration remains report-only, while this explicit command
makes the violation count build-blocking when reviewing or validating changes.
