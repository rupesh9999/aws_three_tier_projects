# OTA Travel Application - Prerequisites

## Overview
This document outlines all prerequisites required to deploy and run the OTA Travel Application. Ensure all requirements are met before proceeding with deployment.

## Table of Contents
1. [Local Development Environment](#local-development-environment)
2. [AWS Account Requirements](#aws-account-requirements)
3. [Tools and CLI Requirements](#tools-and-cli-requirements)
4. [Network Requirements](#network-requirements)
5. [Security Requirements](#security-requirements)

---

## Local Development Environment

### Hardware Requirements
| Resource | Minimum | Recommended |
|----------|---------|-------------|
| CPU | 4 cores | 8 cores |
| RAM | 8 GB | 16 GB |
| Storage | 20 GB | 50 GB SSD |

### Operating System
- **Linux**: Ubuntu 22.04 LTS, Amazon Linux 2023, or RHEL 9
- **macOS**: Ventura (13.x) or later
- **Windows**: Windows 11 with WSL2

---

## Tools and CLI Requirements

### Required Software
| Tool | Minimum Version | Download Link |
|------|-----------------|---------------|
| Java JDK | 21 | [Adoptium Temurin](https://adoptium.net/) |
| Node.js | 20.x LTS | [nodejs.org](https://nodejs.org/) |
| npm | 10.x | Included with Node.js |
| Maven | 3.9.x | [maven.apache.org](https://maven.apache.org/) |
| Docker | 24.x | [docker.com](https://www.docker.com/) |
| kubectl | 1.29.x | [kubernetes.io](https://kubernetes.io/docs/tasks/tools/) |
| Terraform | 1.6.x | [terraform.io](https://www.terraform.io/) |
| AWS CLI | 2.15.x | [aws.amazon.com/cli](https://aws.amazon.com/cli/) |
| Helm | 3.14.x | [helm.sh](https://helm.sh/) |
| Git | 2.40+ | [git-scm.com](https://git-scm.com/) |

### Installation Commands

#### Ubuntu/Debian
```bash
# Java 21
sudo apt update
sudo apt install -y temurin-21-jdk

# Node.js 20.x
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Maven
sudo apt install -y maven

# Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# kubectl
curl -LO "https://dl.k8s.io/release/v1.29.0/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Terraform
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform

# AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip && sudo ./aws/install

# Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

#### macOS
```bash
# Using Homebrew
brew install openjdk@21 node@20 maven docker kubectl terraform awscli helm git

# Link Java
sudo ln -sfn $(brew --prefix)/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```

### Verify Installations
```bash
# Run verification script
java -version          # Should show openjdk 21.x
node --version         # Should show v20.x
npm --version          # Should show 10.x
mvn --version          # Should show Apache Maven 3.9.x
docker --version       # Should show Docker version 24.x
kubectl version        # Should show v1.29.x
terraform version      # Should show Terraform v1.6.x
aws --version          # Should show aws-cli/2.15.x
helm version           # Should show v3.14.x
```

---

## AWS Account Requirements

### Account Setup
1. **AWS Account**: Active AWS account with billing enabled
2. **IAM User**: Admin user with programmatic access
3. **MFA**: Multi-factor authentication enabled (recommended)

### Required AWS Services
| Service | Purpose |
|---------|---------|
| EKS | Kubernetes cluster hosting |
| ECR | Container image registry |
| RDS PostgreSQL | Primary database |
| ElastiCache Redis | Session/cache storage |
| OpenSearch | Search functionality |
| S3 | Static assets storage |
| CloudFront | CDN for frontend |
| SQS | Message queuing |
| SNS | Event notifications |
| Secrets Manager | Secure credential storage |
| Route 53 | DNS management |
| ACM | SSL/TLS certificates |
| VPC | Network isolation |

### IAM Permissions
The deployment user/role requires the following policies:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "eks:*",
        "ec2:*",
        "ecr:*",
        "rds:*",
        "elasticache:*",
        "es:*",
        "s3:*",
        "cloudfront:*",
        "sqs:*",
        "sns:*",
        "secretsmanager:*",
        "route53:*",
        "acm:*",
        "iam:*",
        "logs:*",
        "cloudwatch:*",
        "kms:*"
      ],
      "Resource": "*"
    }
  ]
}
```

### Service Quotas
Verify these service quotas in your AWS account:
| Service | Quota | Required |
|---------|-------|----------|
| VPC per region | 5 | 1 |
| EC2 instances (t3.medium) | 20 | 10 |
| RDS instances | 40 | 1 |
| ElastiCache nodes | 300 | 3 |
| S3 buckets | 100 | 2 |

---

## Network Requirements

### Outbound Access
The following endpoints must be accessible:
- `*.amazonaws.com` - AWS services
- `ghcr.io`, `gcr.io` - Container registries
- `registry.npmjs.org` - npm packages
- `repo.maven.apache.org` - Maven dependencies
- `*.github.com` - Git operations

### DNS Configuration
- Domain name for the application (e.g., `ota-travel.example.com`)
- Ability to create/modify DNS records
- Access to create ACM certificates

### Firewall Rules
| Direction | Port | Protocol | Purpose |
|-----------|------|----------|---------|
| Outbound | 443 | TCP | HTTPS |
| Outbound | 80 | TCP | HTTP |
| Outbound | 22 | TCP | SSH (optional) |
| Inbound | 443 | TCP | HTTPS (ALB) |
| Inbound | 80 | TCP | HTTP redirect |

---

## Security Requirements

### Credentials and Secrets
Prepare the following credentials before deployment:

1. **Database Credentials**
   - Master username
   - Strong password (min 16 chars, special chars)

2. **JWT Secret**
   - 256-bit secret key for JWT signing
   - Generate: `openssl rand -base64 32`

3. **Stripe API Keys** (for payments)
   - Secret key
   - Webhook secret

4. **Monitoring Credentials**
   - Grafana admin password
   - Slack webhook URL (optional)
   - PagerDuty service key (optional)

### SSL/TLS Certificates
- ACM certificate for your domain
- Wildcard certificate recommended (e.g., `*.ota-travel.example.com`)

### AWS Secrets Manager Structure
```
ota-travel/
├── production/
│   ├── database         # DB host, port, name, username, password
│   ├── redis            # Redis host, port, password
│   ├── jwt              # JWT secret
│   ├── stripe           # Stripe secret key, webhook secret
│   └── opensearch       # OpenSearch host, username, password
└── staging/
    └── (same structure)
```

#### Automated Secrets Setup
The secrets are automatically created by Terraform during infrastructure deployment. The following resources are created:

| Secret Name | Description | Auto-Generated |
|-------------|-------------|----------------|
| `ota-travel/{env}/database` | Database credentials for all services | Yes |
| `ota-travel/{env}/redis` | Redis connection details | Yes |
| `ota-travel/{env}/jwt` | JWT signing secret (256-bit) | Yes |
| `ota-travel/{env}/stripe` | Stripe API keys | No (provide your keys) |
| `ota-travel/{env}/opensearch` | OpenSearch credentials | Yes |

#### Manual Secrets Setup (Alternative)
If you need to set up secrets manually before Terraform deployment, use the provided script:

```bash
# Run the interactive secrets setup script
./scripts/setup-secrets.sh production

# Or for staging environment
./scripts/setup-secrets.sh staging
```

The script will:
1. Check AWS CLI configuration
2. Prompt for each credential (or auto-generate secure values)
3. Create secrets in AWS Secrets Manager with proper tags
4. Display a summary of created secrets

#### Updating Stripe Keys
After Terraform deployment, update the Stripe secret with your real API keys:

```bash
# Update Stripe secret with real keys
aws secretsmanager update-secret \
  --secret-id ota-travel/production/stripe \
  --secret-string '{
    "secret_key": "sk_live_your_real_key",
    "webhook_secret": "whsec_your_real_webhook_secret",
    "api_version": "2023-10-16"
  }' \
  --region us-east-2
```

#### Viewing Secrets
```bash
# List all secrets for the project
aws secretsmanager list-secrets \
  --filter Key=name,Values=ota-travel/production \
  --region us-east-2

# View a specific secret value
aws secretsmanager get-secret-value \
  --secret-id ota-travel/production/database \
  --region us-east-2
```

---

## Pre-deployment Checklist

- [ ] All required software installed and verified
- [ ] AWS account configured with necessary permissions
- [ ] AWS CLI configured with credentials (`aws configure`)
- [ ] Service quotas verified
- [ ] Domain name registered/available
- [ ] SSL certificate requested in ACM
- [ ] Secrets prepared and stored securely
- [ ] Network access verified
- [ ] Git repository access configured
- [ ] Docker Hub/ECR authentication configured

---

## Next Steps
Once all prerequisites are met, proceed to the [Execution Guide](EXECUTION-GUIDE.md).
