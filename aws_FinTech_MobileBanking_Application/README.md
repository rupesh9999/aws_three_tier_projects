# 🏦 FinTech Mobile Banking Application

A modern, secure, multi-tier banking and payments application built with enterprise-grade architecture for financial services.

![Architecture](docs/images/architecture-diagram.png)

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION TIER                                   │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │  React 18 SPA (Mobile Banking UI)                                        │   │
│  │  • Dashboard • Transfers • Payments • Cards • Loans • Investments       │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                    │                                            │
│                           AWS CloudFront + S3                                   │
└────────────────────────────────────┼────────────────────────────────────────────┘
                                     │
┌────────────────────────────────────┼────────────────────────────────────────────┐
│                           INTEGRATION TIER                                       │
│  ┌─────────────────────────────────┴───────────────────────────────────────┐   │
│  │                      AWS API Gateway                                     │   │
│  │  • Rate Limiting • Throttling • JWT Validation • WAF Protection         │   │
│  └─────────────────────────────────┬───────────────────────────────────────┘   │
│                                    │                                            │
│  ┌────────────────────────────────────────────────────────────────────────┐    │
│  │                         AWS SQS Queues                                  │    │
│  │  • Transaction Queue • KYC Queue • Notification Queue • DLQs           │    │
│  └────────────────────────────────────────────────────────────────────────┘    │
└────────────────────────────────────┼────────────────────────────────────────────┘
                                     │
┌────────────────────────────────────┼────────────────────────────────────────────┐
│                         APPLICATION TIER (EKS)                                   │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐           │
│  │  Auth        │ │  Account     │ │  Transaction │ │  Payment     │           │
│  │  Service     │ │  Service     │ │  Service     │ │  Service     │           │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘           │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐           │
│  │  Cards       │ │  Beneficiary │ │  KYC         │ │  Notification│           │
│  │  Service     │ │  Service     │ │  Service     │ │  Service     │           │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘           │
│                    Spring Boot 3.2 Microservices                                │
└────────────────────────────────────┼────────────────────────────────────────────┘
                                     │
┌────────────────────────────────────┼────────────────────────────────────────────┐
│                              DATA TIER                                           │
│  ┌──────────────────────────┐     ┌──────────────────────────┐                 │
│  │  PostgreSQL 16 (RDS)     │     │  Elasticsearch 8.x       │                 │
│  │  • Users • Accounts      │     │  • Transaction Search    │                 │
│  │  • Transactions • Cards  │     │  • Audit Logs           │                 │
│  └──────────────────────────┘     └──────────────────────────┘                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Frontend | React | 18.2.x |
| UI Framework | Material-UI | 5.15.x |
| State Management | Redux Toolkit | 2.x |
| Backend | Spring Boot | 3.2.x |
| Java | OpenJDK | 21 LTS |
| Database | PostgreSQL | 16.x |
| Search | Elasticsearch | 8.11.x |
| Container Runtime | Docker | 24.x |
| Orchestration | Kubernetes | 1.28+ |
| Cloud | AWS (EKS, RDS, S3, SQS) | Latest |
| IaC | Terraform | 1.6.x |
| CI/CD | Jenkins + ArgoCD | Latest |
| Monitoring | Prometheus + Grafana | Latest |

## 📁 Project Structure

```
fintech-mobile-banking/
├── frontend/                    # React SPA
│   ├── src/
│   │   ├── components/         # Reusable UI components
│   │   ├── pages/              # Page components
│   │   ├── services/           # API integration
│   │   ├── store/              # Redux store
│   │   ├── hooks/              # Custom hooks
│   │   └── utils/              # Utility functions
│   ├── Dockerfile
│   └── k8s/
│
├── backend/                     # Spring Boot Microservices
│   ├── auth-service/
│   ├── account-service/
│   ├── transaction-service/
│   ├── payment-service/
│   ├── cards-service/
│   ├── beneficiary-service/
│   ├── kyc-service/
│   ├── notification-service/
│   └── common/                  # Shared libraries
│
├── database/                    # Database migrations
│   ├── migrations/             # Flyway migrations
│   └── elasticsearch/          # ES index mappings
│
├── infrastructure/              # IaC & DevOps
│   ├── terraform/              # AWS infrastructure
│   ├── kubernetes/             # K8s manifests
│   ├── docker/                 # Docker configs
│   └── ci-cd/                  # Jenkins & ArgoCD
│
├── docs/                        # Documentation
│   ├── api/                    # API documentation
│   ├── architecture/           # Architecture docs
│   └── runbooks/               # Operational runbooks
│
└── scripts/                     # Utility scripts
```

## 🚀 Quick Start

### Prerequisites

- Docker 24.x+
- Docker Compose 2.x+
- Node.js 20.x LTS
- Java 21 LTS
- kubectl 1.28+
- Terraform 1.6+
- AWS CLI 2.x

### Local Development

```bash
# Clone the repository
git clone https://github.com/your-org/fintech-mobile-banking.git
cd fintech-mobile-banking

# Copy environment template
cp .env.example .env
# Edit .env with your local settings (never commit real secrets!)

# Start infrastructure services
docker-compose up -d postgres elasticsearch

# Run backend services
cd backend
./gradlew bootRun

# Run frontend
cd frontend
npm install
npm start
```

### Docker Compose (Full Stack)

```bash
docker-compose up -d
```

Access the application at `http://localhost:3000`

## 🔐 Security Features

- **Authentication**: JWT-based with refresh tokens
- **MFA**: OTP-based multi-factor authentication
- **Authorization**: Role-based access control (RBAC)
- **Data Protection**: Field-level encryption for sensitive data
- **API Security**: Rate limiting, throttling, WAF protection
- **Secrets Management**: AWS Secrets Manager integration
- **Audit Logging**: Complete audit trail for compliance

## 📊 Monitoring & Observability

- **Metrics**: Prometheus + Grafana dashboards
- **Logging**: Centralized logging with FluentBit
- **Tracing**: Distributed tracing ready
- **Alerting**: PagerDuty/Slack integration ready

## 🏃 End-to-End User Flow

1. **User Registration** → KYC Verification → Account Creation
2. **Login** → MFA/OTP Verification → Dashboard
3. **View Accounts** → Check Balances → View Transactions
4. **Fund Transfer** → Select Beneficiary → Enter Amount → Confirm → Success
5. **Bill Payment** → Select Biller → Enter Details → Confirm → Success

## 📚 Documentation

- [Prerequisites Guide](docs/PREREQUISITES.md)
- [Execution Guide](docs/EXECUTION_GUIDE.md)
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md)
- [Operational Runbook](docs/RUNBOOK.md)
- [API Documentation](docs/api/README.md)
- [Security Guide](docs/SECURITY.md)

## 🤝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**⚠️ IMPORTANT SECURITY NOTICE**

This application handles sensitive financial data. Never:
- Commit secrets, API keys, or credentials to version control
- Use production data in non-production environments
- Disable security features in production
- Expose internal services directly to the internet

All secrets must be managed through AWS Secrets Manager or similar secure secret management solutions.
