# medic — Unified Healthcare Interoperability & Telemedicine Hub

> A microservices platform connecting disparate health facilities through a standardized service layer, enabling shared patient records and a national telemedicine network.

---

## Architecture Overview

```
Internet
    │
    ▼
┌─────────────────────────────────────────────┐
│              Frontend :5173                 │
│  React • TypeScript • Tailwind CSS         │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│           API Gateway  :8080                │
│  JWT Auth • Rate Limiting • Request Routing │
└──────────────────────┬──────────────────────┘
                       │ (ClusterIP — internal only)
       ┌───────────────┼───────────────┐
       │               │               │
       ▼               ▼               ▼
┌──────────┐    ┌──────────┐    ┌──────────────┐
│ Patient  │    │   EMR    │    │ Appointment  │
│ Identity │    │ Service  │    │   Service    │
│  (MPI)   │    │  :8082   │    │    :8083     │
│  :8081   │    └──────────┘    └──────────────┘
└──────────┘
       │               │               │
       ▼               ▼               ▼
┌──────────────┐  ┌──────────┐  ┌──────────────┐
│ Telemedicine │  │ Pharmacy │  │  Analytics   │
│   Service    │  │ Service  │  │   Service    │
│    :8084     │  │  :8085   │  │    :8086     │
└──────────────┘  └──────────┘  └──────────────┘
```

### Services

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | JWT auth, rate limiting, routing |
| Patient Identity | 8081 | Master Patient Index (MPI), cross-facility deduplication |
| EMR Service | 8082 | Electronic Medical Records, standardized across facilities |
| Appointment Service | 8083 | Scheduling for in-person and telemedicine |
| Telemedicine Service | 8084 | Video session management (Jitsi/Daily.co) |
| Pharmacy Service | 8085 | e-Prescriptions, drug interaction checking |
| Analytics Service | 8086 | Anonymized public health trend detection |

---

## Tech Stack

- **Backend:** Java 21 + Spring Boot 3.2.5
- **Frontend:** React 18 + TypeScript + Vite + Tailwind CSS
- **Database:** PostgreSQL 15 (one DB per service)
- **Cache / Rate Limit:** Redis 7
- **API Gateway:** Spring Cloud Gateway
- **Auth:** JWT (RS256)
- **Migrations:** Flyway
- **Testing:** JUnit 5, Testcontainers, Mockito
- **Coverage:** JaCoCo (minimum 80%)
- **Security Scan:** OWASP Dependency Check
- **Containers:** Docker
- **Orchestration:** Kubernetes with Kustomize
- **CI/CD:** Jenkins + GitHub Actions
- **Monitoring:** Prometheus + Grafana
- **IaC:** Ansible

---

## Quick Start (Local Development)

### Prerequisites
- Docker Desktop
- Java 21+
- Maven 3.9+
- Node.js 18+ (for frontend)

### 1. Clone and start infrastructure

```bash
git clone https://github.com/your-org/medic.git
cd medic
docker-compose up -d
```

### 2. Build backend services

```bash
mvn clean package -DskipTests
```

### 3. Build and run frontend

```bash
cd medic-frontend
npm install
npm run dev
```

### 4. Run tests

```bash
# Backend unit tests
mvn test

# Backend integration tests (requires Docker for Testcontainers)
mvn verify

# Frontend tests (if added)
npm test
```

### 4. Access services

| URL | Service |
|-----|---------|
| http://localhost:5173 | Frontend (React) |
| http://localhost:8080 | API Gateway (entry point) |
| http://localhost:3000 | Grafana (admin/changeme_dev) |
| http://localhost:9090 | Prometheus |

---

## Frontend Development

The frontend is built with React 18, TypeScript, and Vite for fast development. It uses Tailwind CSS for styling, Zustand for state management, and React Query for data fetching.

### Key Features
- Responsive design for desktop and mobile
- JWT-based authentication
- Real-time appointment scheduling
- Patient record management
- Telemedicine session integration

### Development Commands
```bash
cd medic-frontend
npm install          # Install dependencies
npm run dev          # Start development server
npm run build        # Build for production
npm run preview      # Preview production build
npm run lint         # Run ESLint
```

---

## Project Structure

```
medic/
├── api-gateway/                    # Spring Cloud Gateway
├── patient-identity-service/       # MPI — most critical service
├── emr-service/                    # Electronic Medical Records
├── appointment-service/            # Scheduling
├── telemedicine-service/           # Video sessions
├── pharmacy-service/               # e-Prescriptions
├── analytics-service/              # Public health analytics
├── medic-frontend/                 # React frontend application
├── docs/                           # Documentation
├── infrastructure/
│   ├── ansible/                    # Server provisioning & hardening
│   │   ├── site.yml
│   │   ├── inventory/
│   │   └── roles/{common,security,docker}/
│   ├── kubernetes/                 # K8s manifests (Kustomize)
│   │   ├── base/                   # Deployments, Services, HPA, Secrets
│   │   └── overlays/{dev,prod}/
│   └── monitoring/
│       ├── prometheus/             # Scrape configs + alert rules
│       └── grafana/                # Dashboard + datasource provisioning
├── docker-compose.yml              # Full local stack
├── Jenkinsfile                     # CI/CD pipeline
├── .github/workflows/ci.yml        # GitHub Actions for PRs
└── owasp-suppressions.xml
```

---

## Setting Up GitHub & VS Code

### Step 1 — Initialize Git

```bash
cd medic
git init
git add .
git commit -m "feat: initial medic scaffold"
```

### Step 2 — Create GitHub repository

1. Go to https://github.com/new
2. Create repo named `medic` (private)
3. Do **not** initialize with README (we already have one)

### Step 3 — Push to GitHub

```bash
git remote add origin https://github.com/YOUR_USERNAME/medic.git
git branch -M main
git push -u origin main
```

### Step 4 — VS Code setup

Install these extensions for the best experience:
- **Extension Pack for Java** (Microsoft)
- **Spring Boot Extension Pack** (VMware)
- **Kubernetes** (Microsoft)
- **Docker** (Microsoft)
- **GitLens** (GitKraken)
- **YAML** (Red Hat)

---

## Kubernetes Deployment

### Prerequisites
- `kubectl` connected to your cluster
- `kustomize` installed

### Deploy to production

```bash
# Apply secrets first (fill in real values)
kubectl apply -f infrastructure/kubernetes/base/secrets/secrets.yml

# Deploy everything
kubectl apply -k infrastructure/kubernetes/overlays/prod

# Watch rollout
kubectl get pods -n medic -w
```

### Verify all pods are running

```bash
kubectl get all -n medic
```

---

## Ansible — Provision VPS

```bash
cd infrastructure/ansible

# Test connectivity
ansible all -i inventory/hosts.ini -m ping

# Full provisioning
ansible-playbook -i inventory/hosts.ini site.yml

# Security hardening only
ansible-playbook -i inventory/hosts.ini site.yml --tags security
```

---

## Jenkins Setup

1. Install Jenkins on your CI server
2. Install plugins: Pipeline, Docker Pipeline, Kubernetes CLI, JaCoCo, HTML Publisher
3. Add credentials:
   - `docker-registry-credentials` — Docker Hub login
   - `k8s-kubeconfig` — Kubernetes kubeconfig file
   - `nvd-api-key` — Free key from https://nvd.nist.gov/developers/request-an-api-key
4. Create a Pipeline job pointing to this repo's `Jenkinsfile`

---

## Scrum Roles

| Role | Responsibility |
|------|---------------|
| Product Owner | Healthcare administrator — defines feature priority |
| Scrum Master | Removes blockers, runs ceremonies |
| Dev Team (2–4) | Implements services, writes tests, reviews PRs |

**Sprint length:** 2 weeks  
**Definition of Done:** Code reviewed, 80%+ coverage, all tests passing, OWASP scan clean

---

## Security Notes

- All inter-service communication is **internal only** (ClusterIP) — only the API Gateway is exposed
- JWT tokens carry `X-User-Id` and `X-User-Role` headers forwarded to downstream services
- All secrets are stored as Kubernetes Secrets (never in Git)
- HTTPS is enforced via Ingress TLS (configure cert-manager for Let's Encrypt)
- Ansible enforces: root login disabled, password auth disabled, fail2ban active, UFW firewall

---

## What to Build Next (Backlog)

- [ ] Auth Service: full login/register with JWT issuance
- [ ] Frontend authentication integration
- [ ] Notification Service: SMS alerts for appointments (Twilio/Africa's Talking)
- [ ] Ingress with TLS (cert-manager + Let's Encrypt)
- [ ] AI symptom triage endpoint in Analytics Service
- [ ] USSD interface for feature phone access in rural areas
- [ ] OpenAPI / Swagger docs for all services
- [ ] Frontend testing with Jest and React Testing Library
- [ ] Mobile-responsive design improvements
