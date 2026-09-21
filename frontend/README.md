# CreatorHire Frontend

React 18 + Vite + TypeScript + Tailwind CSS (v4) + React Router + Axios.

## Prerequisites

- Node 18+ and npm

## Run (dev)

```powershell
npm install
npm run dev
```

Opens http://localhost:5173. API calls to `/api/*` are proxied to the Spring Boot backend at http://localhost:8080 (see `vite.config.ts`).

## Build

```powershell
npm run build   # type-checks (tsc -b) and emits dist/
npm run preview # serve the production build locally
```

## Structure

- `src/api/` — axios client (JWT from localStorage) + shared TypeScript types
- `src/auth/` — AuthContext (login/register/logout, `/auth/me` bootstrap)
- `src/components/` — Navbar, Layout, ProtectedRoute/RequireRole guards
- `src/App.tsx` — route tree; feature screens land in TASK-021…025
