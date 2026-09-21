# AGENTS.md — CreatorHire

Working agreement for any agent implementing in this repository.

## 1. Read first

1. This file (`AGENTS.md`).
2. `problem-statement.md`, `docs/mvp.md`, `er-diagram.md`, `class-diagram.md`, `system-architecture.md`.
3. `docs/TASKS.md` — pick the next `TODO` task whose dependencies are `DONE`.
4. Existing code under `backend/` and `frontend/` before changing it.

## 2. Source of truth (precedence)

Explicit user requirement > `docs/mvp.md` > `problem-statement.md` > diagrams > `README.md` > existing code.

## 3. Rules

- Smallest correct change: touch only what the current task requires.
- Never invent requirements, fields, endpoints, or screens. Missing info = `UNKNOWN`, blocking ambiguity = `DECISION_REQUIRED`.
- Backend layering: Controller → Service → Repository → Database. No business logic in controllers.
- Ownership checks on every mutating endpoint (client owns job, creator owns application/profile).
- Validate all input (Jakarta Validation); consistent error responses; never leak stack traces or secrets.
- Never commit secrets, `.env`, credentials, or JWT keys. Use environment variables.
- Update the relevant doc when behavior changes; mark `docs/TASKS.md` status on completion.

## 4. Stack (locked)

- Backend: Java 21, Spring Boot 3.x, Spring Security + JWT, Spring Data JPA, H2 (dev) / MySQL (prod).
- Frontend: React 18 + Vite + TypeScript, React Router, Axios, Tailwind CSS.
- UI pattern: freelancer-card marketplace (card discovery, comparison view, parallax landing).

## 5. Before commit

Build passes · tests pass · diff reviewed · acceptance criteria verified · docs updated · no secrets.
Conventional commits: `feat|fix|test|docs|refactor(scope): description`.
