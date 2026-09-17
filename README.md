# EXAMCHAIN

**Adaptive Zero-Trust Security Lifecycle for Secure Examination Paper Distribution**

> One-Tap for Humans. Zero-Trust for Machines.

---

## Overview

EXAMCHAIN is an end-to-end secure examination paper lifecycle platform that protects exam papers from creation through printing to post-exam transparency. It uses cryptographic security, blockchain-anchored provenance, and zero-trust access controls.

**Current Phase:** Phase 1 — Repository & Foundation

---

## Tech Stack

| Component | Technology |
|---|---|
| Frontend | React 18, TypeScript 5, Vite 5, Tailwind CSS 3, Shadcn/ui |
| Backend | Spring Boot 4.1.x, Java 25, Maven 3.9+ |
| Database | PostgreSQL 16 |
| Identity (Phase 2) | Keycloak 24 |
| Object Storage (Phase 4) | MinIO |
| Key Management (Phase 4) | HashiCorp Vault |
| Blockchain (Phase 8) | Hyperledger Fabric 2.5 |

---

## Quick Start

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose (optional for local standalone dev)
- [Node.js 20+](https://nodejs.org/) (for frontend development)
- [Java 25+](https://adoptium.net/) (for backend development)
- [Maven 3.9+](https://maven.apache.org/) (or use included `./mvnw`)

### 1. Clone and configure

```bash
git clone <repository-url>
cd EXAMCHAIN

# Copy environment template and review values
cp .env.example .env
```

### 2. Run with Docker Compose

```bash
# Start all Phase 1 services (backend, frontend, PostgreSQL)
docker compose up --build

# Or run in background
docker compose up --build -d
```

- **Frontend:** http://localhost:80
- **Backend API:** http://localhost:8080
- **Health Check:** http://localhost:8080/api/v1/health

### 3. Local Development (without Docker)

#### Start PostgreSQL

```bash
# Using Docker for just the database
docker compose up postgres -d
```

#### Start Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend starts at http://localhost:8080.

#### Start Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts at http://localhost:5173 with API proxy to the backend.

### 4. Verify

```bash
# Health check
curl http://localhost:8080/api/v1/health
```

Expected response:
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "application": "examchain",
    "version": "0.1.0",
    "status": "UP",
    "database": "CONNECTED"
  },
  "timestamp": "2026-09-17T18:00:00Z"
}
```

---

## Project Structure

```
EXAMCHAIN/
├── backend/              # Spring Boot API (Java 25)
│   ├── src/main/java/    # Application source
│   ├── src/main/resources/  # Configuration & migrations
│   ├── src/test/         # Tests
│   ├── pom.xml           # Maven dependencies
│   └── Dockerfile
├── frontend/             # React SPA (TypeScript)
│   ├── src/              # Application source
│   ├── package.json      # npm dependencies
│   └── Dockerfile
├── blockchain/           # Hyperledger Fabric (Phase 8)
├── infrastructure/       # Service configurations
│   └── postgres/         # PostgreSQL init scripts
├── docs/                 # Project documentation
├── docker-compose.yml    # Development environment
├── .env.example          # Environment template
└── README.md             # This file
```

---

## Running Tests

```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test
```

---

## Development Phases

| Phase | Description | Status |
|---|---|---|
| **Phase 0** | Architecture & Design | ✅ Complete |
| **Phase 1** | Repository & Foundation | 🔄 In Progress |
| Phase 2 | Identity & RBAC (Keycloak) | ⏳ Pending |
| Phase 3 | Exam & Question Pool Management | ⏳ Pending |
| Phase 4 | Secure Storage & Cryptography | ⏳ Pending |
| Phase 5 | Blueprint & Paper Generation | ⏳ Pending |
| Phase 6 | Variant Fairness Engine | ⏳ Pending |
| Phase 7 | QR & Paper Provenance | ⏳ Pending |
| Phase 8 | Hyperledger Fabric | ⏳ Pending |
| Phase 9-16 | Advanced Features & Demo | ⏳ Pending |

---

## Security

- **No secrets in source code.** All secrets are managed via environment variables.
- See `.env.example` for required configuration.
- See `docs/` for architecture and security documentation.

---

## License

This project is part of an academic examination security initiative.

