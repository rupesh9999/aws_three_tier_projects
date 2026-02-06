# CommSec Trading Platform - AWS Production Grade Implementation

[![AWS](https://img.shields.io/badge/AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)
[![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://reactjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)](https://kubernetes.io/)

## Overview

This project implements a **production-grade three-tier microservices trading platform** inspired by Commonwealth Bank's CommSec architecture. It demonstrates how to build a **highly available, operationally resilient** trading platform on AWS using modern cloud-native technologies.

### Key Features

- **Multi-AZ Architecture**: Deployed across 3 Availability Zones for high availability
- **ARC Zonal Shift**: Application Recovery Controller for instant failover capabilities
- **EKS-based Deployment**: Kubernetes orchestration with Amazon EKS 1.34
- **Real-time Updates**: WebSocket support for live market data
- **CloudFront CDN**: Global content delivery with edge caching
- **Production Security**: Private subnets, security groups, and encrypted data stores

---

## Architecture

```
                                    ┌─────────────────┐
                                    │   CloudFront    │
                                    │      CDN        │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┴────────────────────────┐
                    │                  AWS Cloud (us-east-1)          │
                    │  ┌──────────────────────────────────────────┐   │
                    │  │        Web Tier Application Load Balancer │   │
                    │  └────────────────────┬─────────────────────┘   │
                    │                       │                          │
                    │  ┌────────────────────┼─────────────────────┐   │
                    │  │ AZ-1a     │ AZ-1b      │ AZ-1c           │   │
                    │  │ ┌─────┐  │ ┌─────┐   │ ┌─────┐          │   │
                    │  │ │ Web │  │ │ Web │   │ │ Web │  ← EKS   │   │
                    │  │ └──┬──┘  │ └──┬──┘   │ └──┬──┘          │   │
                    │  └────┼─────┴────┼──────┴────┼──────────────┘   │
                    │       └──────────┼──────────┘                   │
                    │  ┌───────────────┴───────────────┐              │
                    │  │  App Tier Application Load Balancer │         │
                    │  └───────────────┬───────────────┘              │
                    │  ┌───────────────┼───────────────┐              │
                    │  │ ┌─────┐ ┌─────┐ ┌─────┐       │              │
                    │  │ │ App │ │ App │ │ App │ ← EKS │              │
                    │  │ └──┬──┘ └──┬──┘ └──┬──┘       │              │
                    │  └────┼──────┼───────┼──────────┘              │
                    │       └──────┼───────┘                          │
                    │  ┌───────────┴───────────┐                      │
                    │  │   RDS PostgreSQL      │                      │
                    │  │      (Multi-AZ)       │                      │
                    │  └───────────────────────┘                      │
                    └─────────────────────────────────────────────────┘
```

---

## Project Structure

```
CommBankApplicationAWSBlog/
├── README.md                    # This file
├── docs/
│   ├── PREREQUISITES.md         # Required tools and setup
│   ├── EXECUTION-GUIDE.md       # Step-by-step deployment
│   ├── RUNBOOK.md               # Operational procedures
│   ├── INTERVIEW-QA.md          # Interview preparation
│   └── architecture.drawio      # Architecture diagram
├── infrastructure/
│   └── terraform/               # IaC for AWS resources
├── frontend/                    # React trading dashboard
├── backend/
│   ├── trading-service/         # Order management
│   ├── portfolio-service/       # Holdings management
│   ├── market-data-service/     # Real-time quotes
│   └── api-gateway/             # Request routing
├── database/
│   └── scripts/                 # SQL schema scripts
├── kubernetes/                  # K8s deployment manifests
└── scripts/                     # Deployment utilities
```

---

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Frontend | React + Vite | 18.x |
| Backend | Spring Boot | 3.4.x |
| Database | PostgreSQL | 16.x |
| Container Runtime | Docker | 27.x |
| Orchestration | Amazon EKS | 1.34 |
| IaC | Terraform | 1.10.x |
| CDN | CloudFront | Latest |
| Load Balancing | ALB | Latest |
| Resilience | ARC Zonal Shift | Latest |

---

## Quick Start

### 1. Prerequisites
Ensure all tools are installed as per [docs/PREREQUISITES.md](docs/PREREQUISITES.md)

### 2. Deploy Infrastructure
```bash
cd infrastructure/terraform
terraform init
terraform plan -out=tfplan
terraform apply tfplan
```

### 3. Deploy Applications
```bash
# Configure kubectl
aws eks update-kubeconfig --name commsec-cluster --region us-east-1

# Deploy all services
kubectl apply -f kubernetes/
```

### 4. Access the Platform
```bash
# Get ALB DNS name
kubectl get ingress -n commsec
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [PREREQUISITES.md](docs/PREREQUISITES.md) | Required tools and AWS setup |
| [EXECUTION-GUIDE.md](docs/EXECUTION-GUIDE.md) | Complete deployment walkthrough |
| [RUNBOOK.md](docs/RUNBOOK.md) | Operational procedures and troubleshooting |
| [INTERVIEW-QA.md](docs/INTERVIEW-QA.md) | Technical interview preparation |

---

## Key Resilience Features

### ARC Zonal Shift
Instantly shift traffic away from impaired Availability Zones:
```bash
# Initiate zonal shift
./scripts/simulate-zonal-shift.sh start us-east-1a

# Cancel zonal shift
./scripts/simulate-zonal-shift.sh cancel
```

### Auto Scaling
- **Horizontal Pod Autoscaler**: Scales pods based on CPU/memory
- **Cluster Autoscaler**: Scales EKS nodes based on pending pods
- **LCU Reservations**: Pre-warm ALB for predictable traffic spikes

### Multi-AZ Deployment
- Web tier: 3 replicas across 3 AZs
- App tier: 3 replicas across 3 AZs
- Database: RDS Multi-AZ with automatic failover

---

## Security Features

- **Private Subnets**: All application workloads in private subnets
- **Security Groups**: Strict ingress/egress rules
- **Secrets Management**: AWS Secrets Manager for credentials
- **Encryption**: TLS in transit, AES-256 at rest
- **IAM Roles**: Least privilege access with IRSA

---

## Cost Optimization

- **Spot Instances**: Optional for non-critical workloads
- **Right-sizing**: t3.medium for web, t3.large for app tier
- **Reserved Capacity**: LCU reservations vs reactive scaling
- **S3 Lifecycle**: Automated log archival

---

## License
MIT License - See [LICENSE](LICENSE) for details

---

## Acknowledgments

Inspired by the [CommSec AWS Architecture Blog Post](https://aws.amazon.com/blogs/architecture/) describing how CommBank made their trading platform highly available and operationally resilient.
