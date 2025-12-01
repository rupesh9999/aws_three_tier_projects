# 🌍 OTA Travel - Online Travel Agency Platform

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.1-blue)](https://react.dev)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.29-326CE5)](https://kubernetes.io)
[![Terraform](https://img.shields.io/badge/Terraform-1.6+-623CE4)](https://terraform.io)

A modern, secure, cloud-native Online Travel Agency (OTA) application built with microservices architecture on AWS EKS.

## ✨ Features

### Business Features
- 🔍 **Multi-modal Search**: Flights, Hotels, Trains, Buses
- 🛒 **Shopping Cart**: Redis-backed session storage  
- 📋 **Booking Management**: Complete booking lifecycle
- 💳 **Payment Processing**: Stripe integration ready
- 👤 **User Authentication**: JWT-based security
- 📱 **Responsive Design**: Mobile-first approach

### Technical Features
- 🏛️ **Microservices Architecture**: 5 independent, scalable services
- 🔐 **Security First**: JWT, HTTPS, AWS Secrets Manager
- 📊 **Observability**: Prometheus, Grafana, AlertManager
- 🚀 **GitOps CI/CD**: Jenkins + ArgoCD
- ☁️ **Cloud Native**: AWS EKS, fully containerized
- 🔄 **Infrastructure as Code**: Terraform managed

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   PRESENTATION TIER                                   │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                   │
│  │   CloudFront    │───▶│    S3 Bucket    │    │   React SPA     │                   │
│  │   (CDN/SSL)     │    │  (Static Host)  │    │   (Vite Build)  │                   │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              INTEGRATION & MESSAGING TIER                            │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                   │
│  │  API Gateway    │───▶│   AWS SQS       │    │     DLQ         │                   │
│  │  (REST/Auth)    │    │   (Async Jobs)  │    │  (Fault Toler.) │                   │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           APPLICATION / BUSINESS LOGIC TIER                          │
│                                    (AWS EKS Cluster)                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐         │
│  │ User Service  │  │Booking Service│  │Payment Service│  │Search Service │         │
│  │ (Spring Boot) │  │ (Spring Boot) │  │ (Spring Boot) │  │ (Spring Boot) │         │
│  └───────────────┘  └───────────────┘  └───────────────┘  └───────────────┘         │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐                            │
│  │  Cart Service │  │Notify Service │  │ API Gateway   │                            │
│  │ (Spring Boot) │  │ (Spring Boot) │  │   Service     │                            │
│  └───────────────┘  └───────────────┘  └───────────────┘                            │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                     DATA TIER                                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                   │
│  │ PostgreSQL RDS  │    │  Elasticsearch  │    │   Redis Cache   │                   │
│  │ (Primary DB)    │    │   (Search)      │    │  (Sessions)     │                   │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🗂️ Project Structure

```
aws_three_tier_OTA_project/
├── frontend/                    # React 18.3 + Vite + TypeScript
│   ├── src/
│   │   ├── components/          # Reusable UI components
│   │   ├── pages/               # Page components
│   │   ├── services/            # API integration
│   │   ├── hooks/               # Custom React hooks
│   │   ├── store/               # State management (Zustand)
│   │   └── types/               # TypeScript definitions
│   ├── Dockerfile
│   └── package.json
├── backend/                     # Spring Boot 3.3 Microservices
│   ├── user-service/
│   ├── booking-service/
│   ├── payment-service/
│   ├── search-service/
│   ├── cart-service/
│   ├── notification-service/
│   ├── api-gateway-service/
│   └── common/                  # Shared libraries
├── infrastructure/              # Terraform IaC
│   ├── modules/
│   │   ├── vpc/
│   │   ├── eks/
│   │   ├── rds/
│   │   ├── s3-cloudfront/
│   │   ├── api-gateway/
│   │   ├── sqs/
│   │   └── ecr/
│   ├── environments/
│   │   ├── dev/
│   │   ├── staging/
│   │   └── prod/
│   └── main.tf
├── kubernetes/                  # K8s Manifests
│   ├── base/
│   ├── overlays/
│   └── argocd/
├── integration/                 # API Gateway & SQS configs
│   ├── api-gateway/
│   ├── sqs/
│   └── postman/
├── database/                    # DB schemas & migrations
│   ├── migrations/
│   └── elasticsearch/
├── observability/               # Monitoring stack
│   ├── prometheus/
│   ├── grafana/
│   └── fluentbit/
├── ci-cd/                       # Jenkins & ArgoCD
│   ├── jenkins/
│   └── argocd/
└── docs/                        # Documentation
    ├── prerequisites.md
    ├── execution-guide.md
    ├── troubleshooting.md
    └── runbook.md
```

---

## 🚀 Quick Start

### Local Development

```bash
# Clone repository
git clone https://github.com/your-org/travelease.git
cd travelease

# Start infrastructure locally
docker-compose up -d postgres elasticsearch redis

# Start backend services
cd backend && ./mvnw spring-boot:run -pl user-service

# Start frontend
cd frontend && npm install && npm run dev
```

### Docker Compose (Full Stack)

```bash
docker-compose up --build
```

### Kubernetes Deployment

```bash
# Apply all manifests
kubectl apply -k kubernetes/overlays/dev/

# Verify deployment
kubectl get pods -n travelease
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Prerequisites](docs/prerequisites.md) | Required tools and setup |
| [Execution Guide](docs/execution-guide.md) | Step-by-step deployment |
| [Troubleshooting](docs/troubleshooting.md) | Common issues and fixes |
| [Runbook](docs/runbook.md) | Operations procedures |

---

## 🔧 Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Frontend | React + Vite + TypeScript | 18.3 / 5.4 / 5.6 |
| UI Framework | Tailwind CSS + Radix UI | 3.4 / Latest |
| Backend | Spring Boot | 3.3.5 |
| Language | Java | 21 LTS |
| Database | PostgreSQL | 16 |
| Search | Elasticsearch | 8.15 |
| Cache | Redis | 7.4 |
| Container | Docker | 27.x |
| Orchestration | Kubernetes | 1.31 |
| Cloud | AWS (EKS, RDS, S3, CloudFront) | Latest |
| IaC | Terraform | 1.9 |
| CI/CD | Jenkins + ArgoCD | Latest |
| Monitoring | Prometheus + Grafana | Latest |

---

## 🔐 Security Features

- ✅ JWT-based authentication with refresh tokens
- ✅ HTTPS everywhere (TLS 1.3)
- ✅ AWS IAM roles with least privilege
- ✅ Secrets management via AWS Secrets Manager
- ✅ Network policies and security groups
- ✅ Container image scanning
- ✅ OWASP dependency checks
- ✅ Rate limiting and throttling

---

## 📞 Support

For issues and questions, please open a GitHub issue or contact the platform team.

---

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.
