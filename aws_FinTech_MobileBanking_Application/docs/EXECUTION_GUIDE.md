# Execution Guide - Step by Step

This guide walks you through the complete process from cloning the repository to accessing the deployed application.

---

## Table of Contents

1. [Clone Repository](#step-1-clone-repository)
2. [Environment Setup](#step-2-environment-setup)
3. [Local Development](#step-3-local-development)
4. [Build Docker Images](#step-4-build-docker-images)
5. [Push to ECR](#step-5-push-to-ecr)
6. [Apply Terraform](#step-6-apply-terraform)
7. [Deploy to EKS](#step-7-deploy-to-eks)
8. [Access Application](#step-8-access-application)
9. [Test Banking Flows](#step-9-test-banking-flows)

---

## Step 1: Clone Repository

```bash
# Clone the repository
git clone https://github.com/your-org/fintech-mobile-banking.git
cd fintech-mobile-banking

# Verify structure
ls -la
# Expected directories: frontend, backend, infrastructure, docs, scripts
```

---

## Step 2: Environment Setup

### 2.1 Create Environment File

```bash
# Copy template (NEVER commit .env to Git!)
cp .env.example .env

# Edit with your values
nano .env  # or vim .env
```

### 2.2 Key Variables to Set

```bash
# Database (use placeholders for local, Secrets Manager for prod)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=fintech_banking_db
DB_USERNAME=fintech_user
DB_PASSWORD=<generate-secure-password>

# JWT (generate a secure 256-bit key)
JWT_SECRET_KEY=<generate-secure-key>

# AWS (from your AWS account)
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=<your-account-id>
```

### 2.3 Generate Secure Keys

```bash
# Generate JWT secret
openssl rand -base64 32

# Generate database password
openssl rand -base64 24
```

**⚠️ CRITICAL:** Never commit actual secrets. Use these for local dev only. Production uses AWS Secrets Manager.

---

## Step 3: Local Development

### 3.1 Start Infrastructure Services

```bash
# Start PostgreSQL and Elasticsearch
docker-compose up -d postgres elasticsearch

# Verify services are running
docker-compose ps

# Wait for services to be healthy
docker-compose logs -f postgres  # Wait for "ready to accept connections"
docker-compose logs -f elasticsearch  # Wait for "started"
```

### 3.2 Run Database Migrations

```bash
cd backend
./gradlew :account-service:flywayMigrate
./gradlew :auth-service:flywayMigrate
# ... run for each service
```

### 3.3 Start Backend Services

```bash
# Terminal 1: Auth Service
cd backend/auth-service
../gradlew bootRun

# Terminal 2: Account Service
cd backend/account-service
../gradlew bootRun

# Terminal 3: Transaction Service
cd backend/transaction-service
../gradlew bootRun

# ... repeat for other services
```

Or use the combined approach:
```bash
cd backend
./gradlew bootRun --parallel
```

### 3.4 Start Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Application available at http://localhost:3000
```

### 3.5 Verify Local Setup

```bash
# Health check backend
curl http://localhost:8080/actuator/health

# Health check frontend
curl http://localhost:3000

# Test API endpoint
curl http://localhost:8080/api/v1/health
```

---

## Step 4: Build Docker Images

### 4.1 Build Backend Images

```bash
# Build all backend services
cd backend

# Auth Service
docker build -t fintech/auth-service:latest -f auth-service/Dockerfile auth-service/

# Account Service
docker build -t fintech/account-service:latest -f account-service/Dockerfile account-service/

# Transaction Service
docker build -t fintech/transaction-service:latest -f transaction-service/Dockerfile transaction-service/

# Payment Service
docker build -t fintech/payment-service:latest -f payment-service/Dockerfile payment-service/

# Cards Service
docker build -t fintech/cards-service:latest -f cards-service/Dockerfile cards-service/

# Beneficiary Service
docker build -t fintech/beneficiary-service:latest -f beneficiary-service/Dockerfile beneficiary-service/

# KYC Service
docker build -t fintech/kyc-service:latest -f kyc-service/Dockerfile kyc-service/

# Notification Service
docker build -t fintech/notification-service:latest -f notification-service/Dockerfile notification-service/
```

### 4.2 Build Frontend Image

```bash
cd frontend
docker build -t fintech/frontend:latest .
```

### 4.3 Verify Images

```bash
docker images | grep fintech
```

---

## Step 5: Push to ECR

### 5.1 Create ECR Repositories (First Time Only)

```bash
# Run from infrastructure/terraform
cd infrastructure/terraform
terraform init
terraform apply -target=module.ecr
```

### 5.2 Authenticate with ECR

```bash
# Get login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com
```

### 5.3 Tag and Push Images

```bash
# Set variables
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION=us-east-1
export ECR_REGISTRY=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Tag and push each image
for service in auth-service account-service transaction-service payment-service cards-service beneficiary-service kyc-service notification-service frontend; do
  docker tag fintech/${service}:latest ${ECR_REGISTRY}/fintech-${service}:latest
  docker push ${ECR_REGISTRY}/fintech-${service}:latest
done
```

---

## Step 6: Apply Terraform

### 6.1 Initialize Terraform

```bash
cd infrastructure/terraform

# Initialize with backend (S3)
terraform init \
  -backend-config="bucket=fintech-terraform-state-${AWS_ACCOUNT_ID}" \
  -backend-config="key=terraform.tfstate" \
  -backend-config="region=${AWS_REGION}"
```

### 6.2 Plan Infrastructure

```bash
# Review what will be created
terraform plan -out=tfplan -var-file=environments/prod.tfvars

# Review the plan carefully!
```

### 6.3 Apply Infrastructure

```bash
# Apply the plan
terraform apply tfplan

# Save outputs for later
terraform output -json > terraform-outputs.json
```

### 6.4 Verify AWS Resources

```bash
# Verify EKS cluster
aws eks describe-cluster --name fintech-banking-cluster

# Verify RDS instance
aws rds describe-db-instances --db-instance-identifier fintech-banking-db

# Verify S3 buckets
aws s3 ls | grep fintech
```

---

## Step 7: Deploy to EKS

### 7.1 Configure kubectl

```bash
# Update kubeconfig
aws eks update-kubeconfig --region ${AWS_REGION} --name fintech-banking-cluster

# Verify connection
kubectl cluster-info
kubectl get nodes
```

### 7.2 Create Namespace

```bash
kubectl create namespace fintech-banking
kubectl config set-context --current --namespace=fintech-banking
```

### 7.3 Create Secrets (From AWS Secrets Manager)

```bash
# Create external secrets or use AWS Secrets Store CSI Driver
kubectl apply -f infrastructure/kubernetes/secrets/external-secrets.yaml
```

### 7.4 Deploy Services

```bash
# Deploy all services
kubectl apply -f infrastructure/kubernetes/

# Or deploy individually
kubectl apply -f infrastructure/kubernetes/auth-service/
kubectl apply -f infrastructure/kubernetes/account-service/
kubectl apply -f infrastructure/kubernetes/transaction-service/
# ... etc
```

### 7.5 Verify Deployments

```bash
# Check pods
kubectl get pods -n fintech-banking

# Check services
kubectl get svc -n fintech-banking

# Check ingress
kubectl get ingress -n fintech-banking

# Check logs
kubectl logs -f deployment/auth-service -n fintech-banking
```

---

## Step 8: Access Application

### 8.1 Get CloudFront URL

```bash
# From Terraform outputs
cat terraform-outputs.json | jq -r '.cloudfront_distribution_domain.value'

# Or from AWS Console
aws cloudfront list-distributions --query "DistributionList.Items[?Comment=='fintech-banking'].DomainName" --output text
```

### 8.2 Get API Gateway URL

```bash
# From Terraform outputs
cat terraform-outputs.json | jq -r '.api_gateway_url.value'
```

### 8.3 Access Application

```
Frontend: https://d1234567890.cloudfront.net
API: https://api123456.execute-api.us-east-1.amazonaws.com/prod
```

---

## Step 9: Test Banking Flows

### 9.1 User Registration Flow

```bash
# Register new user
curl -X POST https://<api-url>/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecureP@ss123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

### 9.2 Login Flow

```bash
# Login
curl -X POST https://<api-url>/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecureP@ss123!"
  }'

# Response includes JWT token
# Save token: export TOKEN=<jwt-token>
```

### 9.3 View Accounts

```bash
curl https://<api-url>/api/v1/accounts \
  -H "Authorization: Bearer ${TOKEN}"
```

### 9.4 Fund Transfer

```bash
curl -X POST https://<api-url>/api/v1/transactions/transfer \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "acc-123",
    "toAccountId": "acc-456",
    "amount": 100.00,
    "currency": "USD",
    "description": "Test transfer"
  }'
```

### 9.5 Check Transaction History

```bash
curl https://<api-url>/api/v1/transactions?accountId=acc-123 \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## Verification Checklist

- [ ] All pods running in EKS (`kubectl get pods`)
- [ ] All services have endpoints (`kubectl get endpoints`)
- [ ] Ingress has external IP (`kubectl get ingress`)
- [ ] CloudFront distribution active
- [ ] API Gateway stage deployed
- [ ] RDS database accepting connections
- [ ] SQS queues created
- [ ] Secrets stored in Secrets Manager
- [ ] CloudWatch logs streaming
- [ ] Prometheus scraping metrics
- [ ] Grafana dashboards accessible

---

## Next Steps

1. Configure custom domain with Route 53
2. Set up SSL certificates with ACM
3. Enable WAF rules
4. Configure alerting in Grafana
5. Set up backup policies for RDS
6. Enable CloudTrail for audit logging
