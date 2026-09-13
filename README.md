# QuantLab

**Quantitative Market Intelligence for Indian Markets**

QuantLab is a production-grade quantitative market intelligence and decision-support platform
built for Indian equity markets (NSE/BSE). It combines real-time market data, technical analysis,
screening, and portfolio management in a modern, high-performance architecture.

> ⚠️ **Data Policy:** This platform does NOT generate fake predictions or present synthetic data
> as real. Mock data used during UI development is clearly labeled as `MOCK`.

---

## Architecture

```
React (Vite + TypeScript)
  ↓ REST API
Spring Boot (Java 21 + Spring Security + JPA)
  ├── PostgreSQL (primary store)
  ├── Redis (cache)
  └── Python Quant Service (FastAPI)
```

See [docs/architecture.md](docs/architecture.md) for the full architecture guide.

---

## Tech Stack

| Layer          | Technology                                            |
|----------------|-------------------------------------------------------|
| Frontend       | React, TypeScript, Vite, Tailwind CSS, shadcn/ui      |
| Backend        | Java 21, Spring Boot 3, Spring Security, JPA          |
| Quant Service  | Python 3.12, FastAPI, Pandas, NumPy, Scikit-learn     |
| Database       | PostgreSQL 16                                         |
| Cache          | Redis 7                                               |
| Infrastructure | Docker, Docker Compose, Nginx                         |

---

## Getting Started

### Prerequisites

- **Node.js** ≥ 20
- **Java** ≥ 21
- **Python** ≥ 3.11
- **Docker** & **Docker Compose** (for containerized setup)

### Quick Start (Docker)

```bash
# 1. Clone and navigate
git clone <repository-url>
cd Quant-lab

# 2. Copy environment configuration
cp .env.example .env

# 3. Start all services
docker compose up -d

# 4. Verify services
# Frontend:      http://localhost
# Backend API:   http://localhost:8080/api/health
# Quant Service: http://localhost:8000/health
```

### Local Development

#### Frontend

```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

#### Backend

```bash
cd backend

# Ensure PostgreSQL and Redis are running (or use Docker)
docker compose up -d postgres redis

# Run the application
./gradlew bootRun
# → http://localhost:8080
```

#### Quant Service

```bash
cd quant-service

# Create virtual environment
python -m venv .venv

# Activate (Windows)
.venv\Scripts\activate

# Activate (macOS/Linux)
source .venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the application
uvicorn app.main:app --reload --port 8000
# → http://localhost:8000
```

---

## Health Endpoints

| Service       | URL                              |
|---------------|----------------------------------|
| Backend       | `GET http://localhost:8080/api/health` |
| Market Status | `GET http://localhost:8080/api/v1/market-data/status` |
| Market Quotes | `GET http://localhost:8080/api/v1/market-data/quotes/RELIANCE` |
| Ingestion Runs| `GET http://localhost:8080/api/v1/market-data/runs` |
| Quant Service | `GET http://localhost:8000/health`     |
| Quant (proxy) | `GET http://localhost:8080/api/quant/health` |

---

## Project Structure

```
Quant-lab/
├── frontend/              # React + Vite + TypeScript
├── backend/               # Spring Boot + Java 21
├── quant-service/         # FastAPI + Python 3.12
├── infrastructure/        # Docker, Nginx, scripts
├── docs/                  # Documentation
├── docker-compose.yml     # Service orchestration
├── .env.example           # Environment template
└── README.md              # This file
```

---

## Testing

### Frontend

```bash
cd frontend
npm run build        # Verify production build
npm run lint         # Lint check (if configured)
```

### Backend

```bash
cd backend
./gradlew test       # Unit tests (H2 in-memory, no external deps)
```

### Quant Service

```bash
cd quant-service
python -m pytest tests/ -v    # Unit tests (no external deps needed)
```

---

## Environment Configuration

Copy `.env.example` to `.env` and update values:

```bash
cp .env.example .env
```

**Never commit `.env` to version control.** All secrets and API keys
must be provided via environment variables.

See `.env.example` for all available configuration options.

---

## Contributing

1. Create a feature branch from `main`
2. Make your changes
3. Ensure all tests pass
4. Submit a pull request

---

## License

Private / Proprietary — All rights reserved.
