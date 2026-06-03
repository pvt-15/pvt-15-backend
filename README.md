# Skogsjakten — Backend

The backend for **Skogsjakten** ("The Forest Hunt"), a gamified nature‑education app. It is a multi‑module **Spring Boot 4 / Java 17** project built with **Maven** and backed by **MariaDB**, with **JWT‑based** authentication.

It powers the [Skogsjakten Flutter frontend](https://github.com/pvt-15/pvt-15-frontend).

## Architecture

The backend is made up of three independent services, each deployed as a WAR under its own context path:

| Module            | Context path       | Responsibility |
| ----------------- | ------------------ | -------------- |
| `auth-service`    | `/auth-service`    | Registration, login, Google login, users, JWT issuing |
| `storage-service` | `/storage-service` | Image upload and storage (Google Cloud Storage) |
| `app-service`     | `/` (ROOT)         | Pictures, challenges, quizzes, badges, gamification, AI species recognition |

The services call each other over HTTP and share a JWT secret so that tokens issued by `auth-service` are accepted everywhere. Architecture diagrams (PlantUML) are in [`docs/`](docs/).

External integrations: **Google Identity** (Google login token verification), **Google Cloud Storage** (images), **Google Vision API** and **PlantNet API** (species recognition).

## Features

- Local login and Google login, with JWT‑secured endpoints
- User management and profile images
- Image upload with an `objectKey` flow (the frontend sends `imageObjectKey` to `/pictures`, not a signed URL)
- AI‑based picture validation and categorization (Vision + PlantNet)
- Challenges, quizzes and badges, with points and levels

## Requirements

- **Java 17** (JDK)
- **MariaDB** (or MySQL) database — e.g. the DSV database, or a local container (see below)
- Maven is **not** required to be installed: use the bundled wrapper (`./mvnw`, Maven 3.9.14)
- Credentials/keys for the integrations you intend to use (Google OAuth client, Google Cloud Storage, Vision API, PlantNet API)

## Installation

```bash
git clone https://github.com/pvt-15/pvt-15-backend.git
cd pvt-15-backend
```

### Optional: local database

A Compose file is provided to spin up a local MySQL instance:

```bash
docker compose up -d
```

Adjust the database name, user and password in `compose.yaml` to taste, and point `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` at it.

## Configuration

Each service reads its settings from `src/main/resources/application.properties`, where sensitive values are placeholders (e.g. `@DB_URL@`) that are **filled at build time** by Maven from `-D` system properties. The same properties are supplied by the CI pipeline (see [`Jenkinsfile`](Jenkinsfile)).

Supply these when you build or run:

| Property (`-D…`)          | Used by                | Description |
| ------------------------- | ---------------------- | ----------- |
| `DB_URL`                  | auth, app              | JDBC URL, e.g. `jdbc:mariadb://localhost:3306/mydatabase` |
| `DB_USERNAME`             | auth, app              | Database user |
| `DB_PASSWORD`             | auth, app              | Database password |
| `JWT_SECRET`              | all three              | Shared secret for signing/verifying JWTs |
| `GOOGLE_CLIENT_ID`        | auth                   | Google OAuth client ID (must match the frontend) |
| `GCS_BUCKET_NAME`         | storage                | Google Cloud Storage bucket name |
| `GCP_PROJECT_ID`          | storage                | Google Cloud project ID |
| `GCS_CREDENTIALS_B64`     | storage                | Base64‑encoded GCS service‑account credentials JSON |
| `VISION_API_KEY`          | app                    | Google Vision API key |
| `PLANTNET_API_KEY`        | app                    | PlantNet API key |
| `ADMIN_KEY`               | app                    | Admin key for protected admin endpoints |

Inter‑service URLs default to a local Tomcat layout and only need overriding if your hosts differ:

- `STORAGE_SERVICE_BASE_URL` (auth) — default `http://localhost:8080/storage-service`
- `services.auth.base-url` (storage, app) — default `http://localhost:8080/auth-service`
- `services.storage.base-url` (app) — default `http://localhost:8080/storage-service`

> Tip: keep these values in a shell script or your IDE run configuration so you don't have to retype them.

## Usage

### Run a single service for development

From a module directory, start it with the Maven wrapper and pass the properties it needs. For example, the auth service:

```bash
cd auth-service
../mvnw spring-boot:run \
  -DDB_URL="jdbc:mariadb://localhost:3306/mydatabase" \
  -DDB_USERNAME="myuser" \
  -DDB_PASSWORD="secret" \
  -DJWT_SECRET="change-me" \
  -DGOOGLE_CLIENT_ID="<client-id>.apps.googleusercontent.com"
```

On Windows use `..\mvnw.cmd` instead of `../mvnw`.

Each service runs on port **8080** by default. To run several at once locally, give each its own port and update the inter‑service base URLs accordingly, e.g.:

```bash
../mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081" ...
```

You can also run the relevant `*ServiceApplication` main class directly from IntelliJ (set the same properties as VM options or environment variables in the run configuration).

### Build the WAR artifacts

From the repository root, build all modules:

```bash
./mvnw clean package \
  -DDB_URL="…" -DDB_USERNAME="…" -DDB_PASSWORD="…" \
  -DJWT_SECRET="…" -DGOOGLE_CLIENT_ID="…" \
  -DGCS_BUCKET_NAME="…" -DGCP_PROJECT_ID="…" -DGCS_CREDENTIALS_B64="…" \
  -DVISION_API_KEY="…" -DPLANTNET_API_KEY="…" -DADMIN_KEY="…"
```

The WARs are produced at:

```
auth-service/target/auth-service.war
storage-service/target/storage-service.war
app-service/target/*.war          # deployed as ROOT.war (context path "/")
```

## Testing

Run the full test suite from the root:

```bash
./mvnw test
```

…or for a single module:

```bash
./mvnw test -pl auth-service
```

On Windows use `mvnw.cmd`.

## Deployment

Deployment is automated with the [`Jenkinsfile`](Jenkinsfile): it injects the credentials, runs `./mvnw clean verify`, archives the WARs, then deploys them to Tomcat via the Manager API:

- `app-service` → `ROOT.war` → path `/`
- `auth-service` → path `/auth-service`
- `storage-service` → path `/storage-service`

The target is the DSV Tomcat at `https://group-6-15.pvt.dsv.su.se`. To deploy manually, build the WARs as above and upload them through the Tomcat Manager (or copy them into Tomcat's `webapps/`).

## Project structure

```
.
├── pom.xml            # Parent POM (aggregates the three modules)
├── compose.yaml       # Local MySQL for development
├── Jenkinsfile        # CI/CD: build, archive, deploy to Tomcat
├── mvnw / mvnw.cmd    # Maven wrapper
├── docs/              # PlantUML architecture diagrams
├── auth-service/      # Authentication, users, JWT, Google login
├── storage-service/   # Image upload/storage (Google Cloud Storage)
└── app-service/       # Pictures, challenges, quizzes, badges, AI recognition
```

## Notes

- Protected endpoints expect a backend JWT: `Authorization: Bearer <token>`.
- Uploaded images use an `objectKey` flow — the frontend sends `imageObjectKey` to `/pictures`, not a signed image URL.
- Admin endpoints are guarded by `ADMIN-KEY` and should not be exposed in the normal frontend app.

## Contributing

1. Create a branch for your change.
2. Run `./mvnw test` and make sure the build is green before opening a pull request.
3. Open a pull request describing what changed and why. For larger changes, open an issue first to discuss.

## Related

- Frontend: https://github.com/pvt-15/pvt-15-frontend
