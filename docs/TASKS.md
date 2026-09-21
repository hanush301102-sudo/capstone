# TASKS.md — CreatorHire

Dependency-aware task plan. Status values: `TODO` | `DOING` | `DONE`.

## Phase A — Documentation baseline (current)

| ID | Title | Dependencies | Status |
|----|-------|--------------|--------|
| TASK-001 | Repository audit | — | DONE |
| TASK-002 | Reposition docs (problem statement + tagline) | TASK-001 | DONE |
| TASK-003 | Rewrite MVP scope (5 differentiators) | TASK-002 | DONE |
| TASK-004 | Rework ER diagram (15 tables) + PNG | TASK-003 | DONE |
| TASK-005 | Rework class diagram (Matching/Reliability/Radar) + PNG | TASK-003 | DONE |
| TASK-006 | Rework system architecture + PNG | TASK-003 | DONE |
| TASK-007 | TASKS.md + AGENTS.md baseline | TASK-001 | DONE |

## Phase B — Backend (Spring Boot, Java 21)

| ID | Title | Dependencies | Status |
|----|-------|--------------|--------|
| TASK-010 | Project scaffold (Initializr, profiles H2/MySQL, OpenAPI) | TASK-007 | DONE |
| TASK-011 | Entities + repositories (15 tables) | TASK-010 | DONE |
| TASK-012 | Auth (JWT, roles, register/login) | TASK-011 | DONE |
| TASK-013 | Job + brief + Creator Radar | TASK-012 | DONE |
| TASK-014 | Application + MatchScoringService + samples | TASK-013 | TODO |
| TASK-015 | Project + ReliabilityService | TASK-014 | TODO |
| TASK-016 | Portfolio + verification + notifications + reports + admin | TASK-012 | TODO |
| TASK-017 | Backend tests (auth, job→apply→hire flow, scoring) | TASK-016 | TODO |

## Phase C — Frontend (React + Vite + TS)

| ID | Title | Dependencies | Status |
|----|-------|--------------|--------|
| TASK-020 | Scaffold (router, auth context, API client, Tailwind) | TASK-007 | TODO |
| TASK-021 | Landing (parallax) + auth screens | TASK-020 | TODO |
| TASK-022 | Card-based discovery (Creator Radar + job cards, filters) | TASK-020 | TODO |
| TASK-023 | Client dashboards (post job, ranked apps, comparison, projects) | TASK-022 | TODO |
| TASK-024 | Creator dashboards (profile, portfolio, apply, tracking) | TASK-022 | TODO |
| TASK-025 | Admin screens + notifications | TASK-020 | TODO |

## Phase D — Deploy & verify

| ID | Title | Dependencies | Status |
|----|-------|--------------|--------|
| TASK-030 | Dockerfiles + Railway (MySQL, backend, frontend) | TASK-017, TASK-025 | TODO |
| TASK-031 | End-to-end verification on public URL | TASK-030 | TODO |

## Definition of Done (per task)

Requirement implemented · architecture respected · validation + authorization verified · tests added and passing · build passes · diff reviewed · docs updated · no secrets committed.
