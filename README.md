# CreatorHire

**Creator Marketplace for Hiring Video Editors, Designers, and Scriptwriters**

> Stop fishing through generic applications. Hire creators whose style, skills, and reliability are proven.

CreatorHire is a style-first, verified, match-scored marketplace that connects clients (creators, startups, agencies, and small businesses) with creative professionals (video editors, designers, scriptwriters) and manages the workflow from brief-based job posting and match-scored applications to comparison-based shortlisting and project creation.

## Differentiators

1. **Creative Briefs** — every job requires style keywords + reference links; applications answer the brief.
2. **Match Score** — transparent rule-based scoring ranks every application.
3. **Verified Portfolio + Comparison** — skill-tagged portfolio with verification; side-by-side shortlist comparison.
4. **Reliability Record** — on-time delivery rate + response time from completed projects.
5. **Creator Radar** — discover creators by skill + availability before posting.

## Tech Stack

- **Backend**: Java 21 — Spring Boot (Spring Security + JWT, Spring Data JPA)
- **Frontend**: React + Vite + TypeScript (parallax landing, card-based discovery)
- **Database**: PostgreSQL via Spring Data JPA (H2 for local development)
- **Deployment**: Railway (Docker)

## Documentation

| Document | Description |
|----------|-------------|
| [Problem Statement](problem-statement.md) | Domain, users, problem, proposed solution, success criteria |
| [MVP Scope](docs/mvp.md) | Minimum viable product features and core workflows |
| [ER Diagram](er-diagram.md) | Entity relationship model of the core database schema |
| [Class Diagram](class-diagram.md) | Core application class structure |
| [System Architecture](system-architecture.md) | Layered architecture and deployment overview |

## Roles

- **Client** — hires creative professionals for projects
- **Creative Professional** — showcases skills/portfolio and applies for jobs
- **Admin** — manages users, moderates content, reviews reports

## Email Verification (OTP)

Registration creates an **unverified** account and emails a 6-digit code. Login is blocked until the code is verified.

| Step | Endpoint | Response |
|------|----------|----------|
| Register | `POST /api/auth/register` | `201 {message, email}` |
| Verify | `POST /api/auth/verify-otp` `{email, code}` | `200` + JWT |
| Resend | `POST /api/auth/resend-otp` `{email}` | `200` (invalidates old code, 30s UI cooldown) |
| Login (unverified) | `POST /api/auth/login` | `403` "Email not verified…" |

**Policy**: 6-digit `SecureRandom` code · 10-minute expiry (`app.otp.expiry-minutes`) · max 5 attempts (`app.otp.max-attempts`) · SHA-256 hashed storage only · single-use · resend invalidates previous codes · SMTP failure rolls back registration.

**SMTP config** (env vars only — never committed):

| Var | Purpose |
|-----|---------|
| `MAIL_HOST` / `MAIL_PORT` | SMTP server (e.g. `smtp.gmail.com` / `587`) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | credentials (Gmail app password) |
| `MAIL_FROM` | sender address |

**Security**: BCrypt passwords · server-side role whitelist (CLIENT/CREATOR only — ADMIN cannot self-register) · no OTP in API responses or logs · SMTP creds from env only.

## Deployment

- See [docs/DEPLOY.md](docs/DEPLOY.md) for Vercel (frontend) + Railway (backend + PostgreSQL) deployment.
- Live URLs: `UNKNOWN` until TASK-031 cloud verification completes with Vercel + Railway.