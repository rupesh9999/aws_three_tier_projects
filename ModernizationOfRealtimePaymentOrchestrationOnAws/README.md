# Modernization of Real-Time Payment Orchestration on AWS

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg)](https://react.dev/)
[![Terraform](https://img.shields.io/badge/Terraform-1.10+-7B42BC.svg)](https://www.terraform.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Overview

A production-grade, event-driven payment orchestration platform built on AWS, designed to simulate and troubleshoot real-world payment processing challenges faced by financial institutions and big tech companies. This project demonstrates the modernization of monolithic payment systems into a cloud-native microservices architecture.

### Business Context

The global real-time payments market was valued at **USD 24.91 billion in 2024** and is projected to grow to **USD 284.49 billion by 2032** (CAGR 35.4%). This project addresses the key challenges of legacy payment systems:

- **Sequential processing bottlenecks** → Replaced with event-driven parallel processing via Amazon MSK
- **Monolithic scalability limits** → Decomposed into 6 independently scalable microservices
- **High infrastructure costs** → Optimized with serverless (Lambda) and containerized workloads (EKS)
- **Regional compliance complexity** → Tenant-based multi-region architecture with bounded contexts

## Architecture

```
┌──────────────┐    ┌───────────────┐    ┌──────────────────────────────────────┐
│   Clients    │───▶│  CloudFront   │───▶│        API Gateway (Edge)            │
│  (React UI)  │    │  Distribution │    │  /api/v1/payments/*                  │
└──────────────┘    └───────────────┘    └────────────┬─────────────────────────┘
                                                      │
                                         ┌────────────▼─────────────┐
                                         │    Amazon EKS (v1.35)    │
                                         │  ┌─────────────────────┐ │
                                         │  │ Payment Initiation  │ │
                                         │  │ Payment Execution   │ │
                                         │  │ Payment Tracking    │ │
                                         │  │ Payment Reconcil.   │ │
                                         │  │ Payment Billing     │ │
                                         │  │ Payment Risk        │ │
                                         │  └────────┬────────────┘ │
                                         └───────────┼──────────────┘
                                                     │
                              ┌───────────────────────┼───────────────────────┐
                              │                       │                       │
                    ┌─────────▼──────┐    ┌──────────▼──────┐    ┌──────────▼──────┐
                    │  Amazon MSK    │    │ RDS PostgreSQL  │    │  AWS Lambda     │
                    │  (Kafka 3.7)   │    │    (v16)        │    │ (Settlement/    │
                    │  Event Streams │    │  Multi-AZ       │    │  Notification)  │
                    └────────────────┘    └─────────────────┘    └─────────────────┘
```

### Key AWS Services

| Service | Purpose |
|---------|---------|
| **Amazon EKS 1.35** | Container orchestration for payment microservices |
| **Amazon MSK (Kafka 3.7)** | Event streaming for async payment processing |
| **Amazon RDS PostgreSQL 16** | Transactional data persistence |
| **Amazon API Gateway** | Edge-optimized REST API endpoints |
| **Amazon CloudFront** | CDN for frontend and API acceleration |
| **AWS Lambda** | Serverless settlement, notification, and risk functions |
| **Amazon ECR** | Container image registry |
| **Amazon CloudWatch** | Monitoring, logging, and alerting |

### Microservices

| Service | Port | Kafka Topic | Responsibility |
|---------|------|-------------|----------------|
| `payment-initiation` | 8081 | `payment-initiated` | Payment creation, validation, ISO 20022 compliance |
| `payment-execution` | 8082 | `payment-executed` | Routing, authorization, processing |
| `payment-tracking` | 8083 | `*` (all topics) | Lifecycle tracking, status queries |
| `payment-reconciliation` | 8084 | `payment-executed` | Settlement matching, discrepancy detection |
| `payment-billing` | 8085 | `payment-executed` | Fee calculation, invoice generation |
| `payment-risk` | 8086 | `risk-assessed` | Fraud scoring, sanctions screening |

## Quick Start

```bash
# Prerequisites: JDK 21, Maven 3.9+, Node.js 22, Docker 27+
# See docs/PREREQUISITES.md for full setup guide

# 1. Build backend
cd backend && mvn clean package -DskipTests

# 2. Build frontend
cd frontend && npm install && npm run build

# 3. Start locally with Docker Compose
docker compose up -d

# 4. Deploy to AWS (see docs/EXECUTION-GUIDE.md)
cd infrastructure/terraform
terraform init && terraform plan -out=tfplan
terraform apply tfplan
```

## Documentation

| Document | Description |
|----------|-------------|
| [PREREQUISITES.md](docs/PREREQUISITES.md) | Required tools, versions, and AWS configuration |
| [EXECUTION-GUIDE.md](docs/EXECUTION-GUIDE.md) | Step-by-step deployment commands |
| [RUNBOOK.md](docs/RUNBOOK.md) | Production incident response procedures |
| [INTERVIEW-QUESTIONS.md](docs/INTERVIEW-QUESTIONS.md) | 30+ scenario-based interview Q&A |
| [architecture.drawio](docs/architecture.drawio) | Visual architecture diagram |

## Project Structure

```
├── frontend/                    # React 19 + Vite payment dashboard
├── backend/                     # Spring Boot 3.4 multi-module Maven project
│   ├── payment-common/          # Shared models, Kafka config, security
│   ├── payment-initiation/      # Payment creation & validation
│   ├── payment-execution/       # Payment processing & routing
│   ├── payment-tracking/        # Transaction tracking & monitoring
│   ├── payment-reconciliation/  # Settlement reconciliation
│   ├── payment-billing/         # Fee calculation & billing
│   └── payment-risk/            # Fraud detection & compliance
├── infrastructure/
│   └── terraform/               # AWS IaC (VPC, EKS, RDS, MSK, Lambda)
├── kubernetes/                  # EKS deployment manifests
├── docker-compose.yml           # Local development environment
└── docs/                        # All documentation
```

## License

This project is for educational and simulation purposes. MIT License.
