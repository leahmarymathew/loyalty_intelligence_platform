# Loyalty Intelligence Platform

A multi-tenant customer loyalty and engagement system for retail brands, with AI-driven recommendations and support chat layered on top. Retail brands manage points, tiers, campaigns, and offers for their customers through a shared platform with strict per-tenant data isolation.

## Architecture

Three services, one repo, one `docker compose up`:

- **core-api/** — Java 21 + Spring Boot 3 + PostgreSQL. System of record: tenants, customers, transactions, append-only points ledger, tiers, campaigns, offers, redemptions.
- **ai-service/** — Python 3.12 + FastAPI. Next-best-offer recommender and a retrieval-augmented support chatbot.
- **web/** — React 19 + TypeScript + Vite. Brand-side dashboard and a mobile-first, installable customer PWA.

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for key design decisions and [BENCHMARKS.md](docs/BENCHMARKS.md) for performance and model numbers.

## Status

Project scaffolding only — services are not yet implemented.

## Quickstart

_Coming soon: `docker compose up` once the services are built out._
