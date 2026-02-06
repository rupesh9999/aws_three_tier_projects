# Prerequisites

## Overview
This document outlines all required tools, configurations, and permissions needed to deploy the CommSec Trading Platform on AWS.

---

## Required Tools

### 1. AWS CLI v2
```bash
# Install AWS CLI v2
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Verify installation
aws --version
# Expected: aws-cli/2.x.x

# Configure AWS credentials
aws configure
# Enter: AWS Access Key ID, Secret Access Key, Region (us-east-1), Output format (json)
```

### 2. Terraform
```bash
# Install Terraform (v1.10.x or later)
wget -O - https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install terraform

# Verify
terraform version
# Expected: Terraform v1.10.x
```

### 3. kubectl
```bash
# Install kubectl (v1.34.x to match EKS version)
curl -LO "https://dl.k8s.io/release/v1.34.0/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify
kubectl version --client
```

### 4. Docker
```bash
# Install Docker
sudo apt-get update
sudo apt-get install -y docker.io
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

# Verify (re-login may be required)
docker --version
# Expected: Docker version 27.x.x
```

### 5. Node.js (for Frontend)
```bash
# Install Node.js 22.x LTS
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs

# Verify
node --version  # Expected: v22.x.x
npm --version   # Expected: 10.x.x
```

### 6. Java JDK 21 (for Backend)
```bash
# Install Eclipse Temurin JDK 21
sudo apt install -y wget apt-transport-https
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update && sudo apt install temurin-21-jdk

# Verify
java --version
# Expected: openjdk 21.x.x
```

### 7. Maven
```bash
# Install Maven 3.9.x
sudo apt install maven

# Verify
mvn --version
# Expected: Apache Maven 3.9.x
```

### 8. eksctl (Optional but Recommended)
```bash
# Install eksctl for EKS management
ARCH=amd64
PLATFORM=$(uname -s)_$ARCH
curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$PLATFORM.tar.gz"
tar -xzf eksctl_$PLATFORM.tar.gz -C /tmp && rm eksctl_$PLATFORM.tar.gz
sudo mv /tmp/eksctl /usr/local/bin

# Verify
eksctl version
```

---

## AWS IAM Permissions

### Minimum Required Permissions
Create an IAM user or role with the following policies:
- `AmazonEKSClusterPolicy`
- `AmazonEKSWorkerNodePolicy`
- `AmazonEKS_CNI_Policy`
- `AmazonEC2ContainerRegistryFullAccess`
- `AmazonRDSFullAccess`
- `AmazonS3FullAccess`
- `AmazonVPCFullAccess`
- `ElasticLoadBalancingFullAccess`
- `CloudFrontFullAccess`
- `AmazonRoute53FullAccess`
- `AmazonSSMFullAccess` ← **Required for secure password storage**
- `AWSKeyManagementServicePowerUser` ← **Required for SSM SecureString encryption**
- `IAMFullAccess` (for creating service roles)

### Custom Policy (Production Recommended)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:*",
        "eks:*",
        "ecr:*",
        "rds:*",
        "s3:*",
        "elasticloadbalancing:*",
        "cloudfront:*",
        "route53:*",
        "iam:CreateRole",
        "iam:AttachRolePolicy",
        "iam:PassRole",
        "iam:GetRole",
        "iam:DeleteRole",
        "iam:CreateOpenIDConnectProvider",
        "iam:DeleteOpenIDConnectProvider",
        "iam:CreatePolicy",
        "iam:DeletePolicy",
        "arc-zonal-shift:*",
        "logs:*",
        "cloudwatch:*",
        "ssm:GetParameter",
        "ssm:GetParameters",
        "ssm:PutParameter",
        "ssm:DeleteParameter",
        "kms:Decrypt",
        "kms:Encrypt",
        "kms:GenerateDataKey"
      ],
      "Resource": "*"
    }
  ]
}
```

---

## AWS Account Configuration

### 1. Enable Required Services
Ensure the following services are enabled in your AWS account:
- Amazon EKS
- Amazon RDS
- Amazon S3
- Amazon CloudFront
- AWS Application Recovery Controller

### 2. Service Quotas
Verify sufficient quotas for:
- VPCs per Region: At least 5
- Elastic IPs: At least 3
- NAT Gateways: At least 3
- EKS Clusters: At least 1
- RDS Instances: At least 1

Check quotas:
```bash
aws service-quotas list-service-quotas --service-code ec2 --region us-east-1
aws service-quotas list-service-quotas --service-code eks --region us-east-1
```

### 3. Key Pairs
Create an EC2 key pair for potential SSH access to nodes:
```bash
aws ec2 create-key-pair \
  --key-name commsec-eks-key \
  --key-type rsa \
  --query 'KeyMaterial' \
  --output text > ~/.ssh/commsec-eks-key.pem
chmod 400 ~/.ssh/commsec-eks-key.pem
```

---

## Network Requirements

### Outbound Internet Access
Required for:
- Pulling Docker images from ECR and Docker Hub
- kubectl access to EKS API server
- Terraform provider downloads

### Firewall Rules (if applicable)
Ensure access to:
- `*.amazonaws.com` (AWS services)
- `registry.k8s.io` (Kubernetes images)
- `ghcr.io` (GitHub Container Registry)

---

## Environment Variables

Set these environment variables before deployment:
```bash
# AWS Configuration
export AWS_REGION=us-east-1
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Project Configuration
export PROJECT_NAME=commsec
export ENVIRONMENT=production
```

---

## Secure Database Password Setup

> [!IMPORTANT]
> The database password is stored in **AWS SSM Parameter Store** as a **SecureString**, not in Terraform variables or state files.

### How It Works
1. **You create** the password in SSM as a SecureString (encrypted with KMS)
2. **Terraform reads** the password at apply time using `data.aws_ssm_parameter`
3. **SSM decrypts** the value using KMS (only in memory, never written to disk)
4. **Terraform sets** the RDS password, but the value is marked `sensitive`

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Step 1: Store Password                Step 2: Terraform Apply          │
│  ┌────────────────────────┐            ┌────────────────────────────┐   │
│  │ aws ssm put-parameter  │            │ data "aws_ssm_parameter"   │   │
│  │ --type "SecureString"  │ ─────────▶ │    with_decryption = true  │   │
│  │ (encrypted with KMS)   │            └──────────────┬─────────────┘   │
│  └────────────────────────┘                           │                  │
│                                                       ▼                  │
│                                        ┌────────────────────────────┐   │
│                                        │ aws_db_instance.password   │   │
│                                        │ (value never in state)     │   │
│                                        └────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Create the Password
```bash
# Generate a strong random password
export DB_PASSWORD=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 24)

# Store in SSM Parameter Store as SecureString
aws ssm put-parameter \
    --name "/commsec/production/db-password" \
    --value "$DB_PASSWORD" \
    --type "SecureString" \
    --region $AWS_REGION \
    --overwrite

# Verify it was stored (shows encrypted metadata, not value)
aws ssm describe-parameters \
    --filters "Key=Name,Values=/commsec/production/db-password" \
    --region $AWS_REGION

# To retrieve (for debugging only - normally Terraform does this)
aws ssm get-parameter \
    --name "/commsec/production/db-password" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text
```

### Security Benefits
- ✅ Password never stored in Terraform state file
- ✅ Password encrypted at rest with AWS KMS
- ✅ Access controlled via IAM policies
- ✅ Audit trail in CloudTrail
- ✅ Easy rotation without code changes

---

## Verification Checklist

Run this script to verify all prerequisites:
```bash
#!/bin/bash
echo "=== Prerequisites Verification ==="

echo -n "AWS CLI: "
aws --version 2>/dev/null && echo "✓" || echo "✗ Not installed"

echo -n "Terraform: "
terraform version 2>/dev/null | head -1 && echo "✓" || echo "✗ Not installed"

echo -n "kubectl: "
kubectl version --client --short 2>/dev/null && echo "✓" || echo "✗ Not installed"

echo -n "Docker: "
docker --version 2>/dev/null && echo "✓" || echo "✗ Not installed"

echo -n "Node.js: "
node --version 2>/dev/null && echo "✓" || echo "✗ Not installed"

echo -n "Java: "
java --version 2>/dev/null | head -1 && echo "✓" || echo "✗ Not installed"

echo -n "Maven: "
mvn --version 2>/dev/null | head -1 && echo "✓" || echo "✗ Not installed"

echo -n "AWS Credentials: "
aws sts get-caller-identity >/dev/null 2>&1 && echo "✓ Configured" || echo "✗ Not configured"

echo "=== Verification Complete ==="
```

---

## Next Steps
After completing all prerequisites, proceed to [EXECUTION-GUIDE.md](./EXECUTION-GUIDE.md) for deployment instructions.
