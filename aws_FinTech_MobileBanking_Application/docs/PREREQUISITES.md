# Prerequisites Guide

## System Requirements

### Development Machine

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| CPU | 4 cores | 8+ cores |
| RAM | 16 GB | 32 GB |
| Storage | 50 GB SSD | 100 GB SSD |
| OS | Linux/macOS/Windows (WSL2) | Linux/macOS |

---

## Required Tools

### 1. Runtime Environments

#### Java 21 LTS (OpenJDK)
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# macOS (Homebrew)
brew install openjdk@21

# Verify installation
java -version
# Expected: openjdk version "21.x.x"
```

#### Node.js 20.x LTS
```bash
# Using nvm (recommended)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
nvm install 20
nvm use 20

# Verify installation
node --version  # Expected: v20.x.x
npm --version   # Expected: 10.x.x
```

### 2. Container Tools

#### Docker 24.x
```bash
# Ubuntu
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER

# Verify installation
docker --version  # Expected: Docker version 24.x.x
```

#### Docker Compose 2.x
```bash
# Included with Docker Desktop or install separately
docker compose version  # Expected: Docker Compose version v2.x.x
```

### 3. Kubernetes Tools

#### kubectl 1.28+
```bash
# Linux
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# macOS
brew install kubectl

# Verify installation
kubectl version --client
```

#### Helm 3.x
```bash
# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# macOS
brew install helm

# Verify installation
helm version  # Expected: version.BuildInfo{Version:"v3.x.x"...}
```

### 4. Infrastructure Tools

#### Terraform 1.6+
```bash
# Linux
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform

# macOS
brew tap hashicorp/tap
brew install hashicorp/tap/terraform

# Verify installation
terraform version  # Expected: Terraform v1.6.x
```

#### AWS CLI 2.x
```bash
# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# macOS
brew install awscli

# Verify installation
aws --version  # Expected: aws-cli/2.x.x
```

### 5. Build Tools

#### Gradle 8.x (for backend)
```bash
# Usually managed via Gradle Wrapper (./gradlew)
# Manual installation optional

# macOS
brew install gradle

# Verify
gradle --version  # Expected: Gradle 8.x
```

---

## AWS Account Requirements

### Required AWS Services Access

Your AWS account must have access to the following services:

| Service | Purpose |
|---------|---------|
| EC2 | Compute resources |
| EKS | Kubernetes cluster |
| RDS | PostgreSQL database |
| S3 | Static assets, logs |
| CloudFront | CDN |
| API Gateway | API management |
| SQS | Message queues |
| Secrets Manager | Secrets storage |
| ECR | Container registry |
| IAM | Access management |
| VPC | Networking |
| CloudWatch | Logging & monitoring |
| WAF | Web application firewall |
| Shield | DDoS protection |
| GuardDuty | Threat detection |

### Required IAM Permissions

Create an IAM user or role with the following managed policies (for development):

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
        "iam:*",
        "cloudwatch:*",
        "logs:*",
        "waf:*",
        "wafv2:*",
        "shield:*",
        "guardduty:*",
        "config:*",
        "cloudtrail:*",
        "elasticloadbalancing:*",
        "autoscaling:*",
        "kms:*"
      ],
      "Resource": "*"
    }
  ]
}
```

**⚠️ Production Note:** In production, use fine-grained IAM policies following the principle of least privilege.

### AWS CLI Configuration

```bash
# Configure AWS credentials
aws configure

# Enter:
# - AWS Access Key ID: <your-access-key>
# - AWS Secret Access Key: <your-secret-key>
# - Default region: us-east-1 (or your preferred region)
# - Default output format: json
```

**🔒 Security:** Never commit AWS credentials. Use IAM roles for EC2/EKS workloads in production.

---

## IDE Recommendations

### Recommended IDEs

1. **IntelliJ IDEA** (Ultimate or Community) - For Java/Spring Boot
2. **VS Code** - For frontend and general development

### VS Code Extensions

```bash
# Install recommended extensions
code --install-extension dbaeumer.vscode-eslint
code --install-extension esbenp.prettier-vscode
code --install-extension ms-azuretools.vscode-docker
code --install-extension hashicorp.terraform
code --install-extension redhat.java
code --install-extension vscjava.vscode-spring-boot-dashboard
```

---

## Network Requirements

### Ports to Open (Local Development)

| Port | Service |
|------|---------|
| 3000 | React Frontend |
| 8080-8089 | Spring Boot Services |
| 5432 | PostgreSQL |
| 9200 | Elasticsearch |
| 9090 | Prometheus |
| 3001 | Grafana |

### Firewall Rules

Ensure your firewall allows outbound connections to:
- AWS services (*.amazonaws.com)
- npm registry (registry.npmjs.org)
- Maven Central (repo.maven.apache.org)
- Docker Hub (hub.docker.com)

---

## Verification Checklist

Run this script to verify all prerequisites:

```bash
#!/bin/bash
echo "=== Prerequisites Check ==="

# Java
echo -n "Java 21: "
java -version 2>&1 | grep -q "21" && echo "✓" || echo "✗"

# Node.js
echo -n "Node.js 20: "
node -v 2>&1 | grep -q "v20" && echo "✓" || echo "✗"

# Docker
echo -n "Docker: "
docker --version 2>&1 | grep -q "24" && echo "✓" || echo "✗"

# kubectl
echo -n "kubectl: "
kubectl version --client 2>&1 | grep -q "v1.2" && echo "✓" || echo "✗"

# Terraform
echo -n "Terraform: "
terraform version 2>&1 | grep -q "v1.6" && echo "✓" || echo "✗"

# AWS CLI
echo -n "AWS CLI: "
aws --version 2>&1 | grep -q "aws-cli/2" && echo "✓" || echo "✗"

# Helm
echo -n "Helm: "
helm version 2>&1 | grep -q "v3" && echo "✓" || echo "✗"

echo "=== Check Complete ==="
```

Save as `scripts/check-prerequisites.sh` and run:
```bash
chmod +x scripts/check-prerequisites.sh
./scripts/check-prerequisites.sh
```
