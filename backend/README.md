# CreatorHire Backend

Spring Boot 3.5 + Java 21 API for the CreatorHire marketplace.

## Prerequisites

- JDK 21+ to compile for release 21 (this machine builds with JDK 24, `maven.compiler.release=21`)
- Maven 3.9+ (`C:\Users\hanus\tools\apache-maven-3.9.9\bin\mvn.cmd` on this machine)

## Run (dev, H2 in-memory)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
& C:\Users\hanus\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

- API health: http://localhost:8080/api/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:creatorhire`)

## Run (prod, PostgreSQL)

Set `SPRING_PROFILES_ACTIVE=prod` and env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `FRONTEND_URL`.

## Single-origin local demo (one link for everything)

```powershell
cd ../frontend; npm run build
New-Item -ItemType Directory -Force -Path ../backend/src/main/resources/static | Out-Null
Copy-Item dist/* ../backend/src/main/resources/static/ -Recurse -Force
cd ../backend
# then run as in dev; open http://localhost:8080
```

The `static/` bundle is git-ignored build output; `SpaController` forwards frontend routes to it.

## Test

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
& C:\Users\hanus\tools\apache-maven-3.9.9\bin\mvn.cmd test
```
