# Loyalty Intelligence Platform

A multi-tenant customer loyalty and engagement system for retail brands, with AI-driven recommendations and support chat layered on top. Retail brands manage points, tiers, campaigns, and offers for their customers through a shared platform with strict per-tenant data isolation.

## Architecture

Three services, one repo, one `docker compose up`:

- **core-api/** — Java 21 + Spring Boot 3 + PostgreSQL. System of record: tenants, customers, transactions, append-only points ledger, tiers, campaigns, offers, redemptions.
- **ai-service/** — Python 3.12 + FastAPI. Next-best-offer recommender and a retrieval-augmented support chatbot.
- **web/** — React 19 + TypeScript + Vite. Brand-side dashboard and a mobile-first, installable customer PWA.

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for key design decisions and [BENCHMARKS.md](docs/BENCHMARKS.md) for performance and model numbers.

## Status

- **core-api**: schema/migrations, JPA domain entities (Tenant, Customer, PurchaseTransaction, PointsLedgerEntry), repositories, and the points ledger service (earn/redeem/adjust, idempotent, balance derived from the ledger) are in place, with database-enforced multi-tenant isolation (Postgres RLS, bound per-connection via `TenantAwareDataSource`) and an append-only ledger (trigger-enforced). 26/26 tests passing (unit + Testcontainers integration) at 86.9% line coverage. Tier engine, REST layer, and campaign worker not yet built.
- **ai-service**, **web**: not yet started.

## Quickstart

_Coming soon: `docker compose up` once the services are built out. For now, `core-api` can be exercised directly via `mvn verify` (requires Docker for the Testcontainers tests)._
