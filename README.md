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
- **Database**: MySQL via Spring Data JPA (H2 for local development)
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

## Deployment

- See [docs/DEPLOY.md](docs/DEPLOY.md) for Railway deployment (MySQL + backend + frontend).
- Live URLs: `UNKNOWN` until TASK-031 cloud verification completes.