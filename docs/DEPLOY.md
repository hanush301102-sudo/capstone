# Deploying CreatorHire — Vercel + Render + Railway Postgres

## Overview

| Component | Service | Purpose |
|-----------|---------|---------|
| **Frontend** | **Vercel** | React + Vite + TS — auto-deploys from GitHub |
| **Backend** | **Render** | Spring Boot API (Docker) — auto-deploys from GitHub |
| **Database** | **Railway** | PostgreSQL plugin (public networking enabled) |

Repo: `hanush301102-sudo/capstone`, branch `main`.
Local setup: see [LOCAL_DEV.md](LOCAL_DEV.md).

> Railway `${{Postgres.*}}` references only resolve **inside Railway** — the Render
> backend needs the actual host/port/db/user/password values copied manually.

---

## 1. Database — Railway (Postgres only)

1. Railway → **New Project** → **Add PostgreSQL** plugin.
2. Postgres service → **Settings → Networking → Public Networking → Enable**.
3. Copy from the **Connect** tab: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`.
4. Schema is created automatically by Hibernate (`ddl-auto: update` in `application-prod.yml`) on first backend boot.

## 2. Backend — Render

1. Render → **New → Web Service** → connect `hanush301102-sudo/capstone`.
2. **Root Directory**: `backend`. **Runtime**: Docker (uses `backend/Dockerfile`).
3. **Health Check Path**: `/api/health`.
4. **Environment variables**:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://<PGHOST>:<PGPORT>/<PGDATABASE>?sslmode=require` |
| `DB_USERNAME` | `<PGUSER>` |
| `DB_PASSWORD` | `<PGPASSWORD>` |
| `JWT_SECRET` | long random string (≥ 32 chars) |
| `FRONTEND_URL` | Vercel URL from step 3 (e.g. `https://creatorhire.vercel.app`) |
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | Gmail address |
| `MAIL_PASSWORD` | fresh Gmail **app password** (registration 500s without working SMTP) |
| `MAIL_FROM` | Gmail address |

5. Deploy. Health check: `https://<render-service>.onrender.com/api/health` → `{"status":"UP"}`.

## 3. Frontend — Vercel

1. Vercel → **Add New Project** → import `hanush301102-sudo/capstone`.
2. Framework Preset: **Vite** (auto-detected). **Root Directory**: `frontend`.
3. **Environment variable**:

| Key | Value |
|-----|-------|
| `VITE_API_BASE_URL` | `https://<render-service>.onrender.com/api` |

(`frontend/src/api/client.ts` reads `VITE_API_BASE_URL`, falling back to `/api` for local dev; `frontend/vercel.json` handles SPA rewrites.)

4. Deploy → copy the Vercel URL back into Render's `FRONTEND_URL` and redeploy the backend (CORS allowlist).

---

## 4. Verify (TASK-020 + TASK-031)

On the public URLs:

- TASK-020: register as CREATOR and CLIENT → real OTP email → verify → login → dashboard. Wrong code → 409. Resend → 200. Login-before-verify → 403.
- TASK-031: `post briefed job` → `discover` → `apply with samples` → `ranked review` → `shortlist` → `hire` → `complete` → `reliability on Radar card`.
- Spot-checks: ADMIN self-registration rejected; creator cannot mutate another user's job/application; no OTP or secrets in responses/logs.

## Architecture

```
┌──────────────────────────────┐
│        Vercel (Frontend)     │
│  React + Vite → dist/        │
│  VITE_API_BASE_URL → Render  │
└──────────────┬───────────────┘
               │ HTTPS
               ▼
┌──────────────────────────────┐      ┌──────────────────────────┐
│       Render (Backend)       │─────▶│  Railway (PostgreSQL)    │
│  Docker, Spring Boot, :8080  │ JDBC │  public networking, SSL  │
│  /api/health → {"status"}    │      │  ddl-auto: update        │
└──────────────────────────────┘      └──────────────────────────┘
```
