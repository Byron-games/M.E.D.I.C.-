# M.E.D.I.C. Frontend

React + TypeScript + Vite dashboard for the M.E.D.I.C. Healthcare platform.

## Setup

```bash
# Install dependencies
npm install

# Start dev server (proxies /api to localhost:8080)
npm run dev
```

Open http://localhost:3001

## Demo Credentials

| Username | Password  | Role           |
|----------|-----------|----------------|
| admin    | medic123  | Admin          |
| doctor   | medic123  | Clinician      |
| pharmacy | medic123  | Pharmacy       |
| analyst  | medic123  | Analyst        |

## Pages

- `/` — Dashboard & service health
- `/patients` — Patient registration & MPI search
- `/emr` — Electronic medical records
- `/appointments` — Appointment booking & calendar
- `/telemedicine` — Video session management
- `/pharmacy` — e-Prescriptions & drug interactions
- `/analytics` — Disease surveillance & outbreak alerts

## Connecting to Backend

The Vite dev server proxies all `/api/*` calls to `http://localhost:8080` (API Gateway).

Make sure `docker-compose up -d` is running before starting the frontend.

## Build for Production

```bash
npm run build
```

Output goes to `dist/` — serve with nginx or add as a Docker container.
