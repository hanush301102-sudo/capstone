# Deploying CreatorHire — Vercel + Railway

## Overview

This project uses **two platforms**:

| Component | Service | Purpose |
|-----------|---------|---------|
| **Frontend** | **Vercel** | React app (Vite, TypeScript, Tailwind) — auto-deploys from GitHub |
| **Backend + Database** | **Railway** | Spring Boot API + PostgreSQL — single service, Docker-driven |

Both are connected to this GitHub repo (`hanush301102-sudo/capstone`, branch `main`).

---

## 1. Frontend — Vercel

1. Go to [Vercel](https://vercel.com) → **Add New Project** → import `hanush301102-sudo/capstone`.
2. Framework Preset: **Vite** (Vercel auto-detects).
3. Root Directory: leave empty (repo root).
4. **Environment Variables** (add these in Vercel Dashboard → Settings → Variables):

| Key | Value |
|-----|-------|
| `SPRING_API_URL` | `https://backend.up.railway.app/api` (set after backend is deployed) |
| `JWT_SECRET` | a long random string (≥ 32 chars) — match backend |
| `NODE_ENV` | `production` |

5. Click **Deploy**. Vercel will build and publish the frontend URL, e.g. `https://creatorhire.vercel.app`.

6. After deploy, copy the Vercel URL and set it as the backend `FRONTEND_URL` (step 2 below).

---

## 2. Backend + Database — Railway

1. Go to [Railway](https://railway.app) → **New Project** → **Deploy from GitHub repo** → select `hanush301102-sudo/capstone`.
2. **Root Directory**: `backend` (Railway auto-detects the `Dockerfile`).
3. Railway will create a **PostgreSQL addon** automatically. If prompted, keep the default PostgreSQL service.
4. Name the service exactly **`backend`** (important — the frontend references this hostname).

5. **Environment Variables** (Railway will auto-populate some from the Postgres addon, but verify/set all):

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}?sslmode=require` |
| `DB_USERNAME` | `${POSTGRES_USER}` |
| `DB_PASSWORD` | `${POSTGRES_PASSWORD}` |
| `JWT_SECRET` | a long random string (≥ 32 chars) — match frontend |
| `FRONTEND_URL` | the Vercel frontend URL from step 1 (e.g. `https://creatorhire.vercel.app`) |

6. Deploy. Health check: `<backend-url>/api/health` → `{"status":"UP"}`.

---

## 3. Verify (TASK-031)

Walk the E2E flow on the public URLs:

`register` → `post briefed job` → `discover` → `apply with samples` → `ranked review` → `shortlist` → `hire` → `complete` → `reliability on Radar card`.

Both services should be live within ~2–5 minutes of pushing to `main`.

---

## 4. Local Development (Docker — optional)

If you want to run everything locally without Vercel/Railway:

```bash
# Start PostgreSQL + backend + frontend
docker compose up --build

# Frontend: http://localhost:3000  (served by nginx on port 80 inside container,
# but exposed by Vercel in production)
# Backend API: http://localhost:8080  →  PostgreSQL :5432
# The frontend calls http://backend:8080/api internally via Docker networking.

# For production, use Vercel + Railway as described above.
```

The `docker-compose.yml` is configured for local development. For production, use Vercel + Railway as described above.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Vercel (Frontend)                       │
│  https://creatorhire.vercel.app  ←→  HTTPS + CDN           │
│  React 18 + Vite + TS + Tailwind → index.html             │
│  /api → called directly at Railway backend                 │
└─────────────────────────────────────────────────────────────┘
                                           │ HTTPS
                                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Railway (Backend + PostgreSQL)             │
│  Docker image: creatorhire → Java 21, Spring Boot 3.5      │
│  Exposed port: 8080                                        │
│  POSTGRES_HOST / PORT / USER / PASSWORD from Railway addon   │
│  /api/health → {"status":"UP"}                             │
└─────────────────────────────────────────────────────────────┘
```

## Notes

- The frontend `api/client.ts` reads `VITE_API_BASE_URL` (or falls back to relative paths). Set `VITE_API_BASE_URL=https://backend.up.railway.app/api` in Vercel env if needed, or keep it protocol-relative.
- CORS: The backend should allow `https://*.vercel.app`. Add in `application-prod.yml` or a `@Bean` if not already present:

```yaml
cors:
  allowed-origins:
    - https://*.vercel.app
    - http://localhost:5173
```

- The `SpaController` (single-origin mode) is **not needed** when frontend is on Vercel and backend on Railway — the frontend build contains no API calls; the browser just calls `https://backend.up.railway.app/api/...` directly. The `SpaController` remains in the codebase for local Docker dev.

- To reset databases: use Railway UI → `Postgres` → `Reset`, or run migrations via Flyway if added later.