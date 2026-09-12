# Loyalty Intelligence Platform

[![CI](https://github.com/leahmarymathew/loyalty_intelligence_platform/actions/workflows/ci.yml/badge.svg)](https://github.com/leahmarymathew/loyalty_intelligence_platform/actions/workflows/ci.yml)
![core-api coverage](https://img.shields.io/badge/core--api%20coverage-86.9%25-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Python](https://img.shields.io/badge/Python-3.12-blue)
![React](https://img.shields.io/badge/React-19-61dafb)

A multi-tenant customer loyalty and engagement platform for retail brands. Brands manage points, tiers, campaigns, and offers for their customers through a shared platform with strict per-tenant data isolation, with AI-driven recommendations and support chat layered on top.

## Table of contents

- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Repository layout](#repository-layout)
- [Getting started](#getting-started)
  - [Run everything with Docker Compose](#run-everything-with-docker-compose)
  - [Run a service standalone](#run-a-service-standalone)
- [Testing](#testing)
- [Project status](#project-status)
- [Documentation](#documentation)

## Architecture

Three services, one repository:

| Service | Responsibility |
|---|---|
| **[core-api/](core-api)** | System of record. Tenants, customers, transactions, an append-only points ledger, tiers, campaigns, offers, and redemptions, with database-enforced multi-tenant isolation. |
| **[ai-service/](ai-service)** | Next-best-offer recommender and a retrieval-augmented support chatbot, consuming core-api's tenant-scoped data. |
| **[web/](web)** | Brand-side operations dashboard and a mobile-first, installable customer-facing PWA. |

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the key design decisions (why the points ledger is append-only, how row-level-security-based tenant isolation is enforced end to end, and known limitations) and [docs/BENCHMARKS.md](docs/BENCHMARKS.md) for performance and model numbers.

## Tech stack

| Layer | Choices |
|---|---|
| Core API | Java 21, Spring Boot 3, Spring Data JPA, Flyway, PostgreSQL 16 |
| AI service | Python 3.12, FastAPI, Pydantic v2, uvicorn |
| Web | React 19, TypeScript, Vite, React Router, `vite-plugin-pwa` |
| Data | PostgreSQL with row-level security for tenant isolation |
| Infra | Docker / Docker Compose, GitHub Actions CI |

## Repository layout

```
loyalty_intelligence_platform/
├── core-api/     # Java/Spring Boot system of record
├── ai-service/   # Python/FastAPI recommender + support chatbot
├── web/          # React/TypeScript dashboard + customer PWA
├── docs/         # Architecture decisions and benchmarks
└── docker-compose.yml
```

## Getting started

### Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose (for the full stack, and for `core-api`'s Testcontainers-based integration tests)
- JDK 21 and Maven 3.9+ (for `core-api` standalone)
- Python 3.12 (for `ai-service` standalone)
- Node.js 20+ (for `web` standalone)

### Run everything with Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

This brings up Postgres and `core-api`. `ai-service` and `web` have Dockerfiles ready but are not yet wired into `docker-compose.yml` — uncomment their service blocks once you're ready to run them alongside the rest of the stack.

### Run a service standalone

**core-api** (requires a running Docker daemon for the Testcontainers integration tests):

```bash
cd core-api
mvn verify
```

**ai-service:**

```bash
cd ai-service
pip install -e ".[dev]"
uvicorn ai_service.main:app --reload
```

**web:**

```bash
cd web
npm install
npm run dev
```

## Testing

| Service | Command | Status |
|---|---|---|
| core-api | `mvn verify` | 26/26 tests passing (unit + Testcontainers integration), 86.9% line coverage (JaCoCo-enforced minimum: 80%) |
| ai-service | `pytest` | 9/9 tests passing |
| web | `npm run test` | 10/10 tests passing |

## Project status

- **core-api**: schema/migrations, JPA domain entities (`Tenant`, `Customer`, `PurchaseTransaction`, `PointsLedgerEntry`), repositories, and the points ledger service (earn/redeem/adjust, idempotent, balance derived from the ledger) are in place, with database-enforced multi-tenant isolation (Postgres RLS, bound per-connection via `TenantAwareDataSource`) and an append-only ledger (trigger-enforced). Tier engine, REST layer, and campaign worker not yet built.
- **ai-service**: FastAPI skeleton in place — tenant-scoped `/recommendations/next-best-offer` and `/chat/support` endpoints with stub scoring/retrieval logic. Not yet wired to a real model, vector store, or core-api's data.
- **web**: React/Vite skeleton in place — a brand-side dashboard (tenant overview, campaigns) and an installable customer PWA (points balance, tier, recent activity), both currently backed by stub data pending core-api's REST layer.

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — key design decisions and known limitations
- [docs/BENCHMARKS.md](docs/BENCHMARKS.md) — performance and model numbers
