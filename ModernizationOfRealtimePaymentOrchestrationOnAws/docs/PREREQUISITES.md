# Prerequisites

## Required Tools

| Tool | Version | Purpose | Install Command |
|------|---------|---------|-----------------|
| **AWS CLI** | v2.24+ | AWS resource management | `curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o awscliv2.zip && unzip awscliv2.zip && sudo ./aws/install` |
| **Terraform** | 1.10+ | Infrastructure as Code | `wget https://releases.hashicorp.com/terraform/1.10.5/terraform_1.10.5_linux_amd64.zip && unzip terraform_1.10.5_linux_amd64.zip && sudo mv terraform /usr/local/bin/` |
| **kubectl** | 1.32+ | Kubernetes cluster management | `curl -LO "https://dl.k8s.io/release/v1.32.2/bin/linux/amd64/kubectl" && chmod +x kubectl && sudo mv kubectl /usr/local/bin/` |
| **Docker** | 27+ | Container building | `sudo apt-get update && sudo apt-get install -y docker.io` |
| **JDK** | 21 (Temurin) | Java backend compilation | `sudo apt-get install -y temurin-21-jdk` or via SDKMAN: `sdk install java 21.0.6-tem` |
| **Maven** | 3.9+ | Java build tool | `sudo apt-get install -y maven` or `sdk install maven 3.9.9` |
| **Node.js** | 22 LTS | React frontend build | `curl -fsSL https://deb.nodesource.com/setup_22.x \| sudo -E bash - && sudo apt-get install -y nodejs` |
| **Helm** | 3.16+ | Kubernetes package manager | `curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 \| bash` |
| **eksctl** | 0.203+ | EKS cluster helper | `curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_Linux_amd64.tar.gz" && tar xz -C /tmp && sudo mv /tmp/eksctl /usr/local/bin` |
| **jq** | 1.7+ | JSON processing | `sudo apt-get install -y jq` |

## AWS Account Configuration

### 1. Configure AWS CLI credentials
```bash
aws configure
# AWS Access Key ID: <your-access-key>
# AWS Secret Access Key: <your-secret-key>
# Default region name: us-east-1
# Default output format: json
```

### 2. Verify AWS identity
```bash
aws sts get-caller-identity
```

### 3. Required AWS service quotas
Ensure the following service quotas are sufficient in your target region:

| Service | Quota | Minimum Required |
|---------|-------|-----------------|
| VPC | VPCs per Region | 5 |
| EC2 | Running On-Demand Standard instances (vCPUs) | 32 |
| EKS | Clusters per Region | 2 |
| RDS | DB instances | 2 |
| MSK | Clusters per Region | 1 |
| Lambda | Concurrent executions | 100 |
| ECR | Repositories per Region | 10 |

### 4. Required IAM permissions
The deploying IAM user/role needs policies for: `AmazonEKSClusterPolicy`, `AmazonVPCFullAccess`, `AmazonRDSFullAccess`, `AmazonMSKFullAccess`, `AWSLambda_FullAccess`, `AmazonAPIGatewayAdministrator`, `CloudFrontFullAccess`, `AmazonEC2ContainerRegistryFullAccess`, `IAMFullAccess`, `CloudWatchFullAccess`.

> **Tip**: For a lab/learning environment, you can use `AdministratorAccess` policy. For production, follow the principle of least privilege.

## Environment Variables

Set these before running Terraform:
```bash
export AWS_REGION="us-east-1"
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export PROJECT_NAME="payment-orchestration"
export ENVIRONMENT="production"
```

## Verification

Run the following to verify all tools are installed:
```bash
echo "=== Tool Verification ==="
aws --version
terraform --version
kubectl version --client
docker --version
java -version
mvn --version
node --version
npm --version
helm version --short
eksctl version
jq --version
echo "=== All tools verified ==="
```
