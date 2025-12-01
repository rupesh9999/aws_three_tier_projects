# 🎬 StreamFlix - Netflix-like Video Streaming Platform

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3-blue.svg)](https://react.dev/)
[![Terraform](https://img.shields.io/badge/Terraform-1.9-purple.svg)](https://www.terraform.io/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.31-blue.svg)](https://kubernetes.io/)

A production-ready, cloud-native video streaming platform built with modern technologies and best practices for security, scalability, and performance.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [User Journey](#user-journey)
- [Deployment](#deployment)
- [Security](#security)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION TIER                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     React 18.3 SPA                                   │   │
│  │  • User Authentication  • Video Player  • Content Browser           │   │
│  │  • Profile Management   • Search        • Watchlist                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                               │                                              │
│                    CloudFront CDN + S3 Static Hosting                       │
└───────────────────────────────┼─────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼─────────────────────────────────────────────┐
│                    INTEGRATION & API TIER                                    │
│  ┌────────────────────────────┼────────────────────────────────────────┐   │
│  │              AWS API Gateway (REST + WebSocket)                      │   │
│  │              • Rate Limiting  • WAF Integration                      │   │
│  └────────────────────────────┼────────────────────────────────────────┘   │
│                               │                                              │
│  ┌────────────────────────────┼────────────────────────────────────────┐   │
│  │                    AWS SQS Queues                                    │   │
│  │  • Transcoding Jobs  • Notifications  • Watch History               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────┼─────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼─────────────────────────────────────────────┐
│                    APPLICATION TIER (EKS)                                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │Auth Service │ │Content Svc  │ │Playback Svc │ │Search Svc   │           │
│  │(Spring Boot)│ │(Spring Boot)│ │(Spring Boot)│ │(Spring Boot)│           │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │Profile Svc  │ │Catalog Svc  │ │History Svc  │ │Notif Svc    │           │
│  │(Spring Boot)│ │(Spring Boot)│ │(Spring Boot)│ │(Spring Boot)│           │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘           │
└───────────────────────────────┼─────────────────────────────────────────────┘
                                │
┌───────────────────────────────┼─────────────────────────────────────────────┐
│                         DATA TIER                                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │  PostgreSQL     │  │  Elasticsearch  │  │  Redis Cache    │             │
│  │  (AWS RDS)      │  │  (OpenSearch)   │  │  (ElastiCache)  │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    S3 Media Storage                                  │   │
│  │  • Original Videos  • Transcoded Streams  • Thumbnails              │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3.1 | UI Framework |
| TypeScript | 5.6 | Type Safety |
| Vite | 5.4 | Build Tool |
| React Router | 6.28 | Navigation |
| TanStack Query | 5.60 | Data Fetching |
| Zustand | 5.0 | State Management |
| Tailwind CSS | 3.4 | Styling |
| Video.js | 8.18 | Video Player |

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 LTS | Runtime |
| Spring Boot | 3.3.5 | Application Framework |
| Spring Security | 6.3 | Security |
| Spring Data JPA | 3.3 | Data Access |
| Spring Cloud AWS | 3.2 | AWS Integration |

### Data Layer
| Technology | Version | Purpose |
|------------|---------|---------|
| PostgreSQL | 16 | Primary Database |
| Elasticsearch | 8.15 | Search Engine |
| Redis | 7.4 | Caching |
| Flyway | 10.20 | DB Migrations |

### Infrastructure
| Technology | Version | Purpose |
|------------|---------|---------|
| Terraform | 1.9+ | IaC |
| Kubernetes | 1.31 | Container Orchestration |
| Docker | 27+ | Containerization |
| Helm | 3.16 | K8s Package Manager |

### CI/CD & DevOps
| Technology | Version | Purpose |
|------------|---------|---------|
| Jenkins | 2.479 | CI/CD Pipeline |
| ArgoCD | 2.13 | GitOps |
| Prometheus | 2.55 | Metrics |
| Grafana | 11.3 | Dashboards |

---

## 📁 Project Structure

```
video_Streaming_Platform_aws/
├── frontend/                     # React SPA
│   ├── src/
│   │   ├── components/          # Reusable UI components
│   │   ├── pages/               # Page components
│   │   ├── hooks/               # Custom React hooks
│   │   ├── services/            # API clients
│   │   ├── store/               # State management
│   │   ├── types/               # TypeScript types
│   │   └── utils/               # Utility functions
│   ├── Dockerfile
│   └── package.json
│
├── backend/                      # Spring Boot Microservices
│   ├── auth-service/
│   ├── profile-service/
│   ├── content-service/
│   ├── media-processing-service/
│   ├── catalog-service/
│   ├── search-service/
│   ├── playback-service/
│   ├── watch-history-service/
│   ├── notification-service/
│   └── billing-service/
│
├── infrastructure/               # IaC & K8s
│   ├── terraform/
│   │   ├── modules/
│   │   ├── environments/
│   │   └── main.tf
│   ├── kubernetes/
│   │   ├── base/
│   │   └── overlays/
│   └── helm/
│
├── ci-cd/                        # CI/CD Pipelines
│   ├── jenkins/
│   └── argocd/
│
├── docs/                         # Documentation
│   ├── architecture/
│   ├── api/
│   ├── runbooks/
│   └── troubleshooting/
│
├── monitoring/                   # Observability
│   ├── prometheus/
│   └── grafana/
│
├── security/                     # Security Configs
│   ├── waf/
│   └── policies/
│
└── postman/                      # API Collections
    └── StreamFlix.postman_collection.json
```

---

## ✅ Prerequisites

### Required Tools

```bash
# Verify installations
java --version          # Java 21+
node --version          # Node.js 20+
docker --version        # Docker 27+
kubectl version         # Kubernetes 1.31+
terraform --version     # Terraform 1.9+
aws --version           # AWS CLI 2.x
helm version            # Helm 3.16+
```

### AWS Setup

1. **AWS Account** with appropriate permissions
2. **IAM User/Role** with programmatic access
3. **AWS CLI** configured with credentials

```bash
aws configure
# Enter your Access Key ID
# Enter your Secret Access Key
# Enter default region (e.g., us-east-1)
# Enter default output format (json)
```

### Required IAM Permissions

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:*",
        "eks:*",
        "rds:*",
        "s3:*",
        "cloudfront:*",
        "apigateway:*",
        "sqs:*",
        "secretsmanager:*",
        "ecr:*",
        "elasticache:*",
        "es:*",
        "wafv2:*",
        "shield:*",
        "guardduty:*",
        "securityhub:*",
        "cloudtrail:*",
        "config:*",
        "cloudwatch:*",
        "logs:*",
        "iam:*",
        "kms:*"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/your-org/streamflix.git
cd streamflix
```

### 2. Configure Environment

```bash
# Copy example environment files (NEVER commit actual secrets)
cp frontend/.env.example frontend/.env.local
cp backend/auth-service/src/main/resources/application.yml.example \
   backend/auth-service/src/main/resources/application-local.yml

# Set required environment variables
export AWS_REGION=us-east-1
export ENVIRONMENT=local
```

### 3. Start Local Development

```bash
# Start infrastructure dependencies
docker-compose -f docker-compose.local.yml up -d

# Start backend services
cd backend
./gradlew bootRun --parallel

# Start frontend
cd ../frontend
npm install
npm run dev
```

### 4. Access Application

- **Frontend**: http://localhost:5173
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🎯 User Journey

### Complete Streaming Flow

```
1. REGISTRATION & LOGIN
   └─→ User visits StreamFlix
   └─→ Creates account with email/password
   └─→ Verifies email via OTP
   └─→ Logs in and receives JWT token

2. PROFILE SELECTION
   └─→ User selects/creates viewing profile
   └─→ "Adult", "Kids", or custom profiles
   └─→ Parental controls applied per profile

3. CONTENT BROWSING
   └─→ Personalized home page loads
   └─→ Trending, New Releases, Continue Watching
   └─→ Browse by genre, search content

4. CONTENT SELECTION
   └─→ View movie/series detail page
   └─→ See metadata, cast, similar titles
   └─→ Add to watchlist

5. VIDEO PLAYBACK
   └─→ Click "Play" to start streaming
   └─→ Secure signed URL generated
   └─→ Adaptive bitrate streaming (ABR)
   └─→ Subtitles, quality selection

6. WATCH PROGRESS
   └─→ Playback position tracked
   └─→ "Continue Watching" updated
   └─→ Watch history recorded

7. SESSION END
   └─→ Progress saved automatically
   └─→ Recommendations updated
   └─→ Secure logout
```

---

## 🚢 Deployment

### Deploy to AWS (Production)

#### Step 1: Initialize Terraform

```bash
cd infrastructure/terraform/environments/prod
terraform init
terraform plan -out=tfplan
terraform apply tfplan
```

#### Step 2: Build and Push Docker Images

```bash
# Configure ECR login
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account>.dkr.ecr.us-east-1.amazonaws.com

# Build and push all services
./scripts/build-and-push.sh prod
```

#### Step 3: Deploy to EKS

```bash
# Configure kubectl
aws eks update-kubeconfig --name streamflix-prod --region us-east-1

# Deploy using ArgoCD
kubectl apply -f ci-cd/argocd/applications/
```

#### Step 4: Verify Deployment

```bash
kubectl get pods -n streamflix
kubectl get services -n streamflix
```

---

## 🔐 Security

### Secret Management

**⚠️ CRITICAL: Never commit secrets to version control**

All secrets are managed via AWS Secrets Manager:

```bash
# Store secret
aws secretsmanager create-secret \
  --name /streamflix/prod/db-password \
  --secret-string "your-secure-password"

# Reference in application
spring:
  datasource:
    password: ${sm://streamflix/prod/db-password}
```

### Security Services

| Service | Purpose |
|---------|---------|
| AWS WAF | Web Application Firewall |
| AWS Shield | DDoS Protection |
| AWS GuardDuty | Threat Detection |
| AWS Security Hub | Security Posture |
| AWS CloudTrail | Audit Logging |
| AWS Config | Compliance |

### CloudFront Signed URLs

Video content is protected using CloudFront signed URLs:

```java
// Signed URL generation (key retrieved from Secrets Manager)
CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
    distributionDomain,
    s3ObjectKey,
    keyPairId,        // From Secrets Manager
    privateKey,       // From Secrets Manager
    expirationDate
);
```

---

## 📊 Monitoring

### Prometheus Metrics

- Application metrics exposed at `/actuator/prometheus`
- Custom metrics for streaming performance

### Grafana Dashboards

| Dashboard | Metrics |
|-----------|---------|
| Streaming Health | Playback starts, buffering, errors |
| API Performance | Request rate, latency, errors |
| Infrastructure | CPU, memory, network |
| Business KPIs | Active users, watch time |

### CloudWatch Integration

- EKS container logs
- Application logs
- SQS queue metrics
- RDS performance insights

---

## 🔧 Troubleshooting

See [docs/troubleshooting/](docs/troubleshooting/) for detailed guides:

- [Streaming Issues](docs/troubleshooting/streaming.md)
- [Database Connectivity](docs/troubleshooting/database.md)
- [Kubernetes Pods](docs/troubleshooting/kubernetes.md)
- [Search Index](docs/troubleshooting/elasticsearch.md)
- [Queue Processing](docs/troubleshooting/sqs.md)

### Common Issues

```bash
# Check pod status
kubectl describe pod <pod-name> -n streamflix

# View logs
kubectl logs -f <pod-name> -n streamflix

# Check service endpoints
kubectl get endpoints -n streamflix
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

**Built with ❤️ for the streaming generation**
