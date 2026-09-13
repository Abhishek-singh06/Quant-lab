# QuantLab Architecture

## System Overview

QuantLab is a quantitative market intelligence and decision-support platform
for Indian equity markets (NSE/BSE). It provides real-time market data,
technical analysis, screening, and portfolio management capabilities.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              React + TypeScript + Vite                   │ │
│  │         Tailwind CSS · shadcn/ui · Recharts             │ │
│  │           Framer Motion · Lucide Icons                   │ │
│  └───────────────────────┬─────────────────────────────────┘ │
└──────────────────────────┼──────────────────────────────────┘
                           │ REST API (/api/*)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                       │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │           Spring Boot 3 · Java 21                        │ │
│  │     Spring Security · Spring Data JPA · Actuator        │ │
│  │              Redis Cache Integration                     │ │
│  └──────┬──────────────────┬───────────────────────────────┘ │
└─────────┼──────────────────┼────────────────────────────────┘
          │                  │
          ▼                  ▼
┌──────────────┐    ┌──────────────────────────────────────────┐
│  PostgreSQL  │    │          Quant Service                    │
│   Database   │    │  ┌────────────────────────────────────┐   │
│              │    │  │   FastAPI · Python 3.12             │   │
│  • Market    │    │  │   Pandas · NumPy · Scikit-learn    │   │
│    data      │    │  │                                    │   │
│  • User      │    │  │   • Technical Analysis             │   │
│    data      │    │  │   • Screening Engine               │   │
│  • Analytics │    │  │   • Quantitative Models            │   │
│              │    │  └────────────────────────────────────┘   │
└──────────────┘    └──────────────────────────────────────────┘
          ▲
          │
┌──────────────┐
│    Redis     │
│    Cache     │
│              │
│  • Session   │
│  • Market    │
│    quotes    │
│  • Rate      │
│    limiting  │
└──────────────┘
```

## Service Communication

| From           | To             | Protocol | Port  |
|----------------|----------------|----------|-------|
| Frontend       | Backend        | HTTP     | 8080  |
| Backend        | PostgreSQL     | TCP      | 5432  |
| Backend        | Redis          | TCP      | 6379  |
| Backend        | Quant Service  | HTTP     | 8000  |

## Directory Structure

```
Quant-lab/
├── frontend/              # React + Vite + TypeScript
│   ├── src/
│   │   ├── components/    # UI and layout components
│   │   ├── lib/           # Utilities and API client
│   │   ├── pages/         # Page components
│   │   ├── services/      # API service layer
│   │   ├── types/         # TypeScript type definitions
│   │   └── hooks/         # Custom React hooks
│   └── Dockerfile
├── backend/               # Spring Boot + Java 21
│   ├── src/main/java/com/quantlab/
│   │   ├── config/        # Security, Redis, Web config
│   │   ├── controller/    # REST controllers
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Data access layer
│   │   ├── service/       # Business logic
│   │   ├── marketdata/    # Real Market Data Pipeline (Part 3)
│   │   │   ├── alert/     # Alerting abstractions
│   │   │   ├── config/    # Market data properties
│   │   │   ├── controller/# Market data REST APIs
│   │   │   ├── detector/  # Stale, duplicate, calendar & missing data detectors
│   │   │   ├── entity/    # Market quotes, ingestion runs, errors, sources
│   │   │   ├── model/     # Canonical domain models
│   │   │   ├── normalizer/# Canonical normalizer
│   │   │   ├── provider/  # MarketDataProvider interface & adapters (NSE, MOCK)
│   │   │   ├── repository/# JPA repositories
│   │   │   ├── resilience/# Token-bucket rate limiter & retry executor
│   │   │   ├── service/   # Ingestion and query services
│   │   │   └── validator/ # Strict OHLC and financial validators
│   │   ├── warehouse/     # Historical Data Warehouse (Part 4)
│   │   │   ├── controller/# Warehouse REST APIs
│   │   │   ├── entity/    # Instruments, History, Raw/Adj Prices, Corp Actions, Indices
│   │   │   ├── model/     # CorporateActionType, AdjustmentMethodology, TimeGranularity
│   │   │   ├── repository/# JPA Repositories
│   │   │   └── service/   # CorporateActionAdjustments, PointInTimeUniverse, GapDetection
│   │   ├── news/          # News & Corporate Intelligence Pipeline (Part 5)
│   │   │   ├── controller/# News & Timeline REST APIs
│   │   │   ├── entity/    # NewsArticle, CorporateEvent, Entities, IngestionRuns
│   │   │   ├── intelligence/# EntityResolution, Deduplication, EventDecay, NLPProcessor
│   │   │   ├── model/     # EventType, SentimentLabel, SourceCredibility, Timeline
│   │   │   ├── provider/  # NewsDataProvider, CorporateFilingsProvider, NSE Adapter
│   │   │   ├── repository/# JPA Repositories
│   │   │   └── service/   # NewsIngestionService, CorporateIntelligenceService
│   │   ├── institutional/ # Mutual Fund & Institutional Intelligence (Part 6)
│   │   │   ├── controller/# Institutional Intelligence REST APIs
│   │   │   ├── entity/    # AmcMaster, MutualFundScheme, FundPortfolioDisclosure, FundHolding, InstitutionalFlow, InstitutionalOwnership
│   │   │   ├── model/     # HoldingChangeType, PortfolioScope, InstitutionType, DTOs
│   │   │   ├── provider/  # InstitutionalDataProvider, AMFI/NSE Adapters, Mock
│   │   │   ├── repository/# JPA Repositories
│   │   │   └── service/   # HoldingChangeDetectionService, Ingestion & Analytics Services
│   │   └── global/        # Global Market Intelligence (Part 7)
│   │       ├── calendar/  # GlobalMarketCalendarService (Multi-Exchange Timezone & Hours)
│   │       ├── controller/# GlobalMarketIntelligenceController REST APIs
│   │       ├── entity/    # GlobalInstrument, GlobalMarketSnapshot, GlobalMarketRegime, GlobalMarketSource, GlobalMarketIngestionRun
│   │       ├── model/     # AssetClass, GlobalMarket, DataFreshness, SessionStatus, RegimeLabel, DTOs
│   │       ├── provider/  # GlobalMarketDataProvider, Authorized & Public Adapters, Mock
│   │       ├── repository/# JPA Repositories
│   │       └── service/   # GlobalMarketRegimeEngine, Ingestion & Analytics Services
│   └── Dockerfile
├── quant-service/         # FastAPI + Python 3.12
│   ├── app/
│   │   ├── api/           # API routes
│   │   ├── core/          # Configuration
│   │   ├── models/        # Pydantic schemas
│   │   └── services/      # Business logic
│   └── Dockerfile
├── infrastructure/
│   ├── docker/            # Database init scripts
│   ├── nginx/             # Reverse proxy config
│   └── scripts/           # Utility scripts
├── docs/                  # Documentation
├── docker-compose.yml     # Service orchestration
└── .env.example           # Environment template
```

## Health Endpoints

| Service        | Endpoint            | Port |
|----------------|---------------------|------|
| Backend        | `GET /api/health`   | 8080 |
| Quant Service  | `GET /health`       | 8000 |
| Backend Proxy  | `GET /api/quant/health` | 8080 |

## Technology Stack

### Frontend
- **React 18** — UI library
- **TypeScript** — Type safety
- **Vite** — Build tool & dev server
- **Tailwind CSS** — Utility-first styling
- **shadcn/ui** — Component library (Radix primitives)
- **Framer Motion** — Animations
- **Recharts** — Data visualization
- **Lucide** — Icons

### Backend
- **Java 21** — Language (records, pattern matching, virtual threads)
- **Spring Boot 3** — Application framework
- **Spring Security** — Authentication & authorization
- **Spring Data JPA** — Data access (Hibernate)
- **Spring Data Redis** — Caching layer
- **PostgreSQL 16** — Primary database

### Quant Service
- **Python 3.12** — Language
- **FastAPI** — Async web framework
- **Pandas** — Data manipulation
- **NumPy** — Numerical computing
- **Scikit-learn** — Machine learning utilities
- **Pydantic** — Data validation

### Infrastructure
- **Docker** — Containerization
- **Docker Compose** — Service orchestration
- **Nginx** — Reverse proxy
- **PostgreSQL 16** — Relational database
- **Redis 7** — In-memory cache

## Data Policy

> **IMPORTANT**: QuantLab is designed to work with real market data.
>
> - No fake stock predictions are generated.
> - No synthetic data is presented as real market data.
> - Mock data used in UI development is clearly labeled as `MOCK`.
> - Real data integrations will be added in subsequent phases.
