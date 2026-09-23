# Deploying CreatorHire to Railway

Three Railway services from this repo (`main` branch): PostgreSQL + backend + frontend.

## 1. Create the project

1. Go to https://railway.app → New Project → **Deploy from GitHub repo** → select `hanush301102-sudo/capstone`.
2. Add a database: **+ New → Database → PostgreSQL**. Note its connection values (or use `${{Postgres.PG*}}` references below).

## 2. Backend service

1. **+ New → GitHub Repo** (same repo) → set **Root Directory** to `backend`. Railway auto-detects the `Dockerfile`.
2. Name the service exactly **`backend`** (the frontend nginx proxies `/api/` to `http://backend:8080`).
3. Variables:
   | Key | Value |
   |-----|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DB_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}?sslmode=require` |
   | `DB_USERNAME` | `${{Postgres.PGUSER}}` |
   | `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
   | `JWT_SECRET` | a long random string (≥ 32 chars) |
   | `FRONTEND_URL` | the frontend public URL (step 3) |
4. Deploy. Health check: `<backend-url>/api/health` → `{"status":"UP"}`.

## 3. Frontend service

1. **+ New → GitHub Repo** (same repo) → **Root Directory** `frontend`. Railway auto-detects the `Dockerfile`.
2. **Settings → Networking → Generate Domain** → public URL. Copy it back into backend `FRONTEND_URL`.
3. Open the URL: landing page → register → full workflow runs against the Railway backend.

## 4. Verify (TASK-031)

Walk the E2E script from `EndToEndHiringFlowTest` on the public URLs:
register → post briefed job → discover → apply with samples → ranked review → shortlist → hire → complete → reliability on Radar card.

## Local full-stack (needs Docker)

```bash
docker compose up --build
# frontend http://localhost:3000 → backend :8080 → PostgreSQL :5432
```
