# Local Development Guide

Stack: **Vite dev (frontend :5173) + Spring Boot (backend :8080) + H2 in-memory (default)** or
prod-like **Postgres 16 via Docker Compose**.

## Option A — zero setup (H2, default profile)

Requirements: Java 21+, Maven 3.9+, Node 18+.

```powershell
# Terminal 1 — backend (from repo root)
cd backend
$env:JAVA_HOME = 'C:\jdk24'   # spaceless junction to a JDK 21+ install
& C:\Users\hanus\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
# → http://localhost:8080 (health: http://localhost:8080/api/health)

# Terminal 2 — frontend (from repo root)
cd frontend
npm install
npm run dev
# → http://localhost:5173 (Vite proxies /api → http://localhost:8080)
```

> PowerShell 5.1 note: no `&&` chaining — use `; if ($?) { ... }`. No Unix `head`/`find` — use `Select-Object -First N` / `findstr`.

## SMTP (required for register)

Registration **rolls back with HTTP 500** if the OTP email cannot be sent.
Set these env vars in the backend terminal **before** `spring-boot:run`:

```powershell
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = '<gmail address>'
$env:MAIL_PASSWORD = '<fresh Gmail app password>'
$env:MAIL_FROM = '<gmail address>'
```

Without them, `POST /api/auth/register` returns 500 "Could not send verification email".

## Option B — prod-like local Postgres

```powershell
# from repo root: starts postgres:16 on :5432 (db/user/pass = creatorhire)
docker compose up -d postgres

# Terminal 1 — backend with prod profile against local PG
cd backend
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/creatorhire'
$env:DB_USERNAME = 'creatorhire'
$env:DB_PASSWORD = 'creatorhire'
$env:JWT_SECRET = 'dev-only-local-secret-please-override'
$env:FRONTEND_URL = 'http://localhost:5173'
# ... plus MAIL_* above ...
& C:\Users\hanus\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run

# Terminal 2 — frontend as in Option A
```

Full-stack alternative: `docker compose up --build` → frontend :3000, backend :8080, Postgres :5432.

## Local verification checklist (TASK-020 equivalent)

1. `GET /api/health` → 200.
2. Register as CREATOR and as CLIENT → real OTP email arrives.
3. Wrong code → 409. Expired code → 409. Login before verifying → 403.
4. Resend → 200, old code invalidated, 30s UI cooldown.
5. Correct code → 200 + JWT → dashboard loads.
6. Smoke: hirer posts job → creator applies → match score shown.
7. `mvn test` → 55/55 green. `npm run build` → clean.
