# OTA Travel Application - Execution Guide

## Overview
This guide provides step-by-step instructions for deploying the OTA Travel Application to AWS using Terraform, Kubernetes, and ArgoCD.

## Table of Contents
1. [Quick Start](#quick-start)
2. [Local Development](#local-development)
3. [Infrastructure Deployment](#infrastructure-deployment)
4. [Application Deployment](#application-deployment)
5. [CI/CD Setup](#cicd-setup)
6. [Monitoring Setup](#monitoring-setup)
7. [Verification](#verification)

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/ota-travel.git
cd ota-travel

# Set up environment
export AWS_REGION=us-east-2
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export ENVIRONMENT=production
export EKS_CLUSTER_NAME=ota-production

# Deploy infrastructure
cd infrastructure/terraform
terraform init
terraform apply -auto-approve

# Configure kubectl
aws eks update-kubeconfig --name ota-production --region $AWS_REGION

# Deploy application
kubectl apply -f ../kubernetes/
```

---

## Local Development

### Backend Services

#### 1. Start Dependencies
```bash
# Start PostgreSQL and Redis using Docker Compose
docker compose -f docker-compose.dev.yml up -d postgres redis
```

#### 2. Build Common Module
```bash
cd backend
mvn clean install -pl common -am
```

#### 3. Run Individual Services
```bash
# Terminal 1 - Auth Service
cd backend/auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2 - Search Service  
cd backend/search-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 3 - Booking Service
cd backend/booking-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 4 - Payment Service
cd backend/payment-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 5 - Cart Service
cd backend/cart-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm ci

# Start development server
npm run dev

# Access at http://localhost:5173
```

### Running All Services with Docker Compose
```bash
# From project root
docker compose up --build

# Services will be available at:
# - Frontend: http://localhost:3000
# - Auth Service: http://localhost:8081
# - Search Service: http://localhost:8082
# - Booking Service: http://localhost:8083
# - Payment Service: http://localhost:8084
# - Cart Service: http://localhost:8085
```

---

## Infrastructure Deployment

### Step 1: Configure AWS Credentials
```bash
# Configure AWS CLI
aws configure

# Set environment variables
export AWS_REGION=us-east-2
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Verify access
aws sts get-caller-identity
```

### Step 2: Create Terraform Backend (Optional)
```bash
# Create S3 bucket for state
aws s3 mb s3://ota-travel-terraform-state-${AWS_ACCOUNT_ID}

make_bucket: ota-travel-terraform-state-863394984731

# Create DynamoDB table for locking
aws dynamodb create-table \
    --table-name ota-travel-terraform-locks \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```

### Step 3: Initialize Terraform
```bash
cd infrastructure/terraform

# Initialize with backend
terraform init \
    -backend-config="bucket=ota-travel-terraform-state-${AWS_ACCOUNT_ID}" \
    -backend-config="key=infrastructure/terraform.tfstate" \
    -backend-config="region=${AWS_REGION}" \
    -backend-config="dynamodb_table=ota-travel-terraform-locks"
```

### Step 4: Review and Apply Infrastructure
```bash
# Create terraform.tfvars
cat > terraform.tfvars <<EOF
aws_region    = "us-east-2"
environment   = "production"
project_name  = "ota"

vpc_cidr             = "10.0.0.0/16"
enable_nat_gateway   = true
single_nat_gateway   = false

eks_cluster_version        = "1.32"
eks_node_instance_types    = ["t3.medium"]
eks_node_desired_size      = 3
eks_node_min_size          = 2
eks_node_max_size          = 10

rds_instance_class           = "db.t3.medium"
rds_allocated_storage        = 100
rds_max_allocated_storage    = 500

elasticache_node_type        = "cache.t3.medium"
elasticache_num_cache_nodes  = 3

opensearch_instance_type     = "t3.medium.search"
opensearch_instance_count    = 2
opensearch_volume_size       = 100
EOF

# Plan deployment
terraform plan -out=tfplan

# Apply infrastructure (takes ~30 minutes)
terraform apply tfplan
```

### Step 5: Store Outputs
```bash
# Save important outputs
terraform output -json > ../outputs.json

# Get specific values
export EKS_CLUSTER_NAME=$(terraform output -raw eks_cluster_name)
export REDIS_ENDPOINT=$(terraform output -raw redis_endpoint)
export OPENSEARCH_ENDPOINT=$(terraform output -raw opensearch_endpoint)
export S3_BUCKET=$(terraform output -raw s3_bucket_name)
export API_GATEWAY_URL=$(terraform output -raw api_gateway_url)

# RDS endpoints are per-service (auth, search, booking, payment)
terraform output rds_endpoints
```

### Step 6: Verify EKS Add-ons
After infrastructure deployment, verify that all EKS add-ons are healthy:

```bash
# Configure kubectl
aws eks update-kubeconfig --name ota-production --region us-east-2

# Check add-on status
aws eks list-addons --cluster-name ota-production
aws eks describe-addon --cluster-name ota-production --addon-name aws-ebs-csi-driver --query 'addon.status'

# Verify EBS CSI driver pods are running
kubectl get pods -n kube-system -l app.kubernetes.io/name=aws-ebs-csi-driver

# If EBS CSI controller pods are in CrashLoopBackOff, restart them:
kubectl rollout restart deployment ebs-csi-controller -n kube-system
```

> **Note:** If the EBS CSI driver shows CREATING status for more than 10 minutes, or controller pods are crashing, see the [Troubleshooting Guide](TROUBLESHOOTING.md#ebs-csi-driver-stuck-in-creating-state--crashloopbackoff) for resolution steps.

---

## Application Deployment

### Step 1: Configure kubectl
```bash
aws eks update-kubeconfig \
    --name ota-production \
    --region ${AWS_REGION}

# Verify connection
kubectl cluster-info
kubectl get nodes
```

### Step 2: Verify Secrets in AWS Secrets Manager
Secrets are automatically created by Terraform. Verify they exist:

```bash
# List all secrets for this project
aws secretsmanager list-secrets --filter Key=name,Values=ota/production --region us-east-2

# The following secrets should exist:
# - ota/production/database  (DB credentials for all services)
# - ota/production/redis     (Redis connection info)
# - ota/production/jwt       (JWT signing secret)
# - ota/production/stripe    (Stripe API keys - update with real values)
# - ota/production/opensearch (OpenSearch credentials)

# To update Stripe credentials with real values:
aws secretsmanager update-secret \
    --secret-id ota/production/stripe \
    --secret-string '{
        "secret_key": "sk_live_xxxxx",
        "webhook_secret": "whsec_xxxxx",
        "api_version": "2023-10-16"
    }' \
    --region us-east-2
```

### Step 3: Install Cluster Add-ons
```bash
# Ensure environment variables are set for add-on installation
export AWS_REGION=us-east-2
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export EKS_CLUSTER_NAME=ota-production

# Install AWS Load Balancer Controller
helm repo add eks https://aws.github.io/eks-charts
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=${EKS_CLUSTER_NAME} \
    --set region=${AWS_REGION} \
    --set serviceAccount.create=true \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set serviceAccount.annotations."eks\.amazonaws\.com/role-arn"=arn:aws:iam::${AWS_ACCOUNT_ID}:role/ota-production-aws-lb-controller-role

# Install External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets \
    -n external-secrets --create-namespace

# Install metrics-server
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### Step 4: Build Applications
```bash
# Build frontend
cd frontend
npm install
npm run build
cd ..

# Build backend services
cd backend
mvn clean package -DskipTests
cd ..
```

### Step 5: Build and Push Docker Images
```bash
# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | \
    docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Build and push all services
for service in frontend auth-service search-service booking-service payment-service cart-service; do
    if [ "$service" == "frontend" ]; then
        CONTEXT_PATH="frontend"
    else
        CONTEXT_PATH="backend/${service}"
    fi
    
    IMAGE_NAME="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/ota-production/${service}"
    
# Build and push all services
for service in frontend auth-service search-service booking-service payment-service cart-service; do
    if [ "$service" == "frontend" ]; then
        CONTEXT_PATH="frontend"
    else
        CONTEXT_PATH="backend/${service}"
    fi
    
    IMAGE_NAME="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/ota-production/${service}"
    
    docker build -t ${IMAGE_NAME}:latest -f ${CONTEXT_PATH}/Dockerfile ${CONTEXT_PATH}
    docker push ${IMAGE_NAME}:latest
done
```

### Step 6: Deploy Kubernetes Resources
```bash
cd infrastructure/kubernetes

# Create namespace
kubectl apply -f namespaces.yaml

# Deploy ConfigMaps
kubectl apply -f configmaps.yaml

# Deploy External Secrets
kubectl apply -f external-secrets.yaml

# Wait for secrets to sync
kubectl wait --for=condition=Ready externalsecret/database-credentials -n production --timeout=120s

# Deploy services
kubectl apply -f deployments/

# Deploy ingress
kubectl apply -f ingress.yaml

# Verify deployments
kubectl get pods -n production
kubectl get svc -n production
kubectl get ingress -n production
```

---

## CI/CD Setup

### Jenkins Setup

#### 1. Deploy Jenkins to Kubernetes
```bash
helm repo add jenkins https://charts.jenkins.io
helm install jenkins jenkins/jenkins \
    -n jenkins --create-namespace \
    -f cicd/jenkins/values.yaml
```

#### 2. Configure Jenkins
```bash
# Get admin password
kubectl exec -n jenkins -it svc/jenkins -c jenkins -- \
    cat /run/secrets/additional/chart-admin-password

# Access Jenkins UI
kubectl port-forward -n jenkins svc/jenkins 8080:8080

# Configure:
# 1. Install suggested plugins
# 2. Configure AWS credentials
# 3. Configure GitHub webhook
# 4. Create pipeline job pointing to Jenkinsfile
```

### ArgoCD Setup

#### 1. Install ArgoCD
```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for ArgoCD to be ready
kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=300s
```

#### 2. Access ArgoCD
```bash
# Get initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Port forward for access
kubectl port-forward svc/argocd-server -n argocd 8443:443

# Access at https://localhost:8443
# Username: admin
```

#### 3. Configure ArgoCD Applications
```bash
# Apply ArgoCD configurations
kubectl apply -f cicd/argocd/applications.yaml

# Verify applications
kubectl get applications -n argocd
```

---

## Monitoring Setup

### Install Prometheus Stack
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus prometheus-community/kube-prometheus-stack \
    -n monitoring --create-namespace \
    -f infrastructure/helm/monitoring-values.yaml
```

### Access Dashboards
```bash
# Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
# Access at http://localhost:3000 (admin/your-password)

# Prometheus
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
# Access at http://localhost:9090

# AlertManager
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-alertmanager 9093:9093
# Access at http://localhost:9093
```

---

## Verification

### Health Checks
```bash
# Check all pods are running
kubectl get pods -n production -w

# Check service endpoints
kubectl get endpoints -n production

# Check ingress status
kubectl describe ingress ota-ingress -n production
```

### API Tests
```bash
# Get ALB DNS name
ALB_DNS=$(kubectl get ingress ota-ingress -n production -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

# Test health endpoints
curl -s https://${ALB_DNS}/api/v1/auth/actuator/health | jq
curl -s https://${ALB_DNS}/api/v1/search/actuator/health | jq
curl -s https://${ALB_DNS}/api/v1/bookings/actuator/health | jq
curl -s https://${ALB_DNS}/api/v1/payments/actuator/health | jq
curl -s https://${ALB_DNS}/api/v1/cart/actuator/health | jq

# Test authentication
curl -X POST https://${ALB_DNS}/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"Test123!","firstName":"Test","lastName":"User"}'
```

### Performance Tests
```bash
# Install k6 for load testing
brew install k6  # or apt install k6

# Run load test
k6 run tests/load/search-test.js
```

---

## Cleanup

### Destroy Infrastructure
```bash
# Delete Kubernetes resources
kubectl delete -f infrastructure/kubernetes/

# Destroy Terraform infrastructure
cd infrastructure/terraform
terraform destroy -auto-approve

# Note: The S3 bucket name used for state is configured in main.tf backend block
# Current bucket: ota-travel-terraform-state-863394984731
# Delete S3 bucket (empty first)
aws s3 rm s3://ota-travel-terraform-state-${AWS_ACCOUNT_ID} --recursive
aws s3 rb s3://ota-travel-terraform-state-${AWS_ACCOUNT_ID}

# Note: DynamoDB locking is deprecated - using S3 native locking instead
# If you have an old DynamoDB table, delete it:
# aws dynamodb delete-table --table-name ota-travel-terraform-locks
```

---

## Next Steps
- Review [Troubleshooting Guide](TROUBLESHOOTING.md) for common issues
- Consult [Runbook](RUNBOOK.md) for operational procedures
