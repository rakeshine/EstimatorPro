# RFP Estimator Pro

An intelligent RFP (Request for Proposal) estimation system that automates the process of analyzing RFPs and generating cost estimates.

## Project Structure

```
├── backend/               # Spring Boot backend application
├── orchestrator/          # FastAPI service for orchestrating AI agents
│   ├── agents/           # AI agent implementations
│   └── app.py            # FastAPI application
├── templates/            # Configuration templates
├── data/                 # Sample data and RFP documents
├── infra/                # Infrastructure as Code
└── README.md
```

## Prerequisites

- Docker and Docker Compose
- Java 17+ (for local backend development)
- Python 3.9+ (for local orchestrator development)
- Maven (for Java backend)

## Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd rfq-estimator-pro
   ```

2. **Start services using Docker Compose**
   ```bash
   cd infra
   docker-compose up --build
   ```

3. **Access the services**
   - Backend API: http://localhost:8080
   - Orchestrator API: http://localhost:8000
   - PGAdmin: http://localhost:5050

## API Endpoints

### Orchestrator Service
- `POST /api/estimate` - Process an RFP document
- `GET /health` - Health check endpoint

### Backend Service
- Endpoints will be documented here

## Development

### Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```

### Orchestrator (FastAPI)
```bash
cd orchestrator
uvicorn app:app --reload
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
