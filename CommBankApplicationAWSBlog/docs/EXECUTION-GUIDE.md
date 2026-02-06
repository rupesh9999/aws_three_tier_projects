# Execution Guide

## Overview
This guide provides step-by-step commands to deploy the CommSec Trading Platform on AWS. Follow each section in order.

---

## Table of Contents
1. [Prerequisites Verification](#1-prerequisites-verification)
2. [Environment Setup](#2-environment-setup)
3. [Infrastructure Deployment](#3-infrastructure-deployment)
4. [ECR Repository Setup](#4-ecr-repository-setup)
5. [Build and Push Images](#5-build-and-push-images)
6. [EKS Configuration](#6-eks-configuration)
7. [Application Deployment](#7-application-deployment)
8. [Verification](#8-verification)
9. [Cleanup](#9-cleanup)

---

## 1. Prerequisites Verification

```bash
# Navigate to project directory
cd /home/ubuntu/aws_three_tier_projects/CommBankApplicationAWSBlog

# Verify all tools are installed
echo "=== Checking Prerequisites ==="
aws --version
terraform version
kubectl version --client
docker --version
node --version
java --version
mvn --version

# Verify AWS credentials
aws sts get-caller-identity
```

---

## 2. Environment Setup

```bash
# Set environment variables
export AWS_REGION=us-east-1
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export PROJECT_NAME=commsec
export ENVIRONMENT=production
export EKS_CLUSTER_NAME=commsec-cluster

# Generate database password (save this securely!)
export DB_PASSWORD=$(openssl rand -base64 24 | tr -dc 'a-zA-Z0-9' | head -c 24)
echo "Database Password: $DB_PASSWORD"
echo "SAVE THIS PASSWORD SECURELY!"

# Create parameter store entry for DB password
aws ssm put-parameter \
    --name "/commsec/production/db-password" \
    --value "$DB_PASSWORD" \
    --type "SecureString" \
    --region $AWS_REGION \
    --overwrite

# Verify
echo "AWS Account: $AWS_ACCOUNT_ID"
echo "Region: $AWS_REGION"
echo "Cluster: $EKS_CLUSTER_NAME"
```

---

## 3. Infrastructure Deployment

### 3.1 Initialize Terraform
```bash
cd infrastructure/terraform

# Initialize Terraform
terraform init

# Validate configuration
terraform validate
```

### 3.2 Review Infrastructure Plan
```bash
# Generate execution plan
terraform plan -out=tfplan

# Review the plan output carefully
# Expected resources:
# - VPC with subnets
# - Internet Gateway, NAT Gateways
# - EKS Cluster with node groups
# - RDS PostgreSQL (Multi-AZ)
# - Application Load Balancers
# - S3 bucket for binaries
# - CloudFront distribution
# - ECR repositories
```

### 3.3 Apply Infrastructure
```bash
# Apply the plan (this takes 20-30 minutes)
terraform apply tfplan

# Save outputs for later use
terraform output -json > ../terraform-outputs.json

# Get specific outputs
export VPC_ID=$(terraform output -raw vpc_id)
export EKS_CLUSTER_NAME=$(terraform output -raw eks_cluster_name)
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export CLOUDFRONT_DOMAIN=$(terraform output -raw cloudfront_domain_name 2>/dev/null || echo "Not enabled")

echo "VPC ID: $VPC_ID"
echo "EKS Cluster: $EKS_CLUSTER_NAME"
echo "RDS Endpoint: $RDS_ENDPOINT"
echo "CloudFront: $CLOUDFRONT_DOMAIN"
```

---

## 4. ECR Repository Setup

```bash
# Return to project root
cd /home/ubuntu/aws_three_tier_projects/CommBankApplicationAWSBlog

# Get ECR login token
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Verify ECR repositories exist (created by Terraform)
aws ecr describe-repositories --region $AWS_REGION --query 'repositories[?starts_with(repositoryName, `commsec`)].repositoryName' --output table
```

---

## 5. Build and Push Images

### 5.1 Build Frontend
```bash
cd frontend

# Install dependencies
npm install

# Build production bundle
npm run build

# Build Docker image
docker build -t commsec-frontend:latest .

# Tag and push to ECR
docker tag commsec-frontend:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-frontend:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-frontend:latest

cd ..
```

### 5.2 Build Backend Services
```bash
# Build Trading Service
cd backend/trading-service
mvn clean package -DskipTests
docker build -t commsec-trading-service:latest .
docker tag commsec-trading-service:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-trading-service:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-trading-service:latest
cd ../..

# Build Portfolio Service
cd backend/portfolio-service
mvn clean package -DskipTests
docker build -t commsec-portfolio-service:latest .
docker tag commsec-portfolio-service:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-portfolio-service:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-portfolio-service:latest
cd ../..

# Build Market Data Service
cd backend/market-data-service
mvn clean package -DskipTests
docker build -t commsec-market-data-service:latest .
docker tag commsec-market-data-service:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-market-data-service:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-market-data-service:latest
cd ../..

# Build API Gateway
cd backend/api-gateway
mvn clean package -DskipTests
docker build -t commsec-api-gateway:latest .
docker tag commsec-api-gateway:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-api-gateway:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/commsec-api-gateway:latest
cd ../..
```

### 5.3 Verify Images in ECR
```bash
# List all images
for repo in frontend trading-service portfolio-service market-data-service api-gateway; do
    echo "=== commsec-$repo ==="
    aws ecr list-images --repository-name commsec-$repo --region $AWS_REGION --query 'imageIds[*].imageTag' --output table
done
```

---

## 6. EKS Configuration

### 6.1 Configure kubectl
```bash
# Update kubeconfig for EKS cluster
aws eks update-kubeconfig \
    --name $EKS_CLUSTER_NAME \
    --region $AWS_REGION

# Verify connection
kubectl cluster-info
kubectl get nodes
```

### 6.2 Install AWS Load Balancer Controller
```bash
# Create IAM service account
eksctl create iamserviceaccount \
    --cluster=$EKS_CLUSTER_NAME \
    --namespace=kube-system \
    --name=aws-load-balancer-controller \
    --attach-policy-arn=arn:aws:iam::$AWS_ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
    --override-existing-serviceaccounts \
    --region $AWS_REGION \
    --approve

# Install using Helm
helm repo add eks https://aws.github.io/eks-charts
helm repo update
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=$EKS_CLUSTER_NAME \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller

# Verify controller is running
kubectl get deployment -n kube-system aws-load-balancer-controller
```

### 6.3 Create Kubernetes Secrets
```bash
# Retrieve DB password from SSM
DB_PASSWORD=$(aws ssm get-parameter \
    --name "/commsec/production/db-password" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text)

# Create namespace
kubectl apply -f kubernetes/namespace.yaml

# Create database secret
kubectl create secret generic commsec-db-secret \
    --namespace=commsec \
    --from-literal=username=commsec_admin \
    --from-literal=password=$DB_PASSWORD \
    --from-literal=host=$RDS_ENDPOINT \
    --from-literal=database=commsec

# Verify secret
kubectl get secrets -n commsec
```

---

## 7. Application Deployment

### 7.1 Update Kubernetes Manifests
```bash
# Update image references in manifests
cd kubernetes

# Replace AWS_ACCOUNT_ID placeholder with actual value
find . -name "*.yaml" -exec sed -i "s/\${AWS_ACCOUNT_ID}/$AWS_ACCOUNT_ID/g" {} \;
find . -name "*.yaml" -exec sed -i "s/\${AWS_REGION}/$AWS_REGION/g" {} \;

cd ..
```

### 7.2 Deploy Configuration
```bash
# Deploy ConfigMaps and Secrets
kubectl apply -f kubernetes/config/

# Verify
kubectl get configmaps -n commsec
```

### 7.3 Deploy Backend Services
```bash
# Deploy in order: database-dependent services first
kubectl apply -f kubernetes/backend/

# Watch deployment progress
kubectl rollout status deployment/trading-service -n commsec
kubectl rollout status deployment/portfolio-service -n commsec
kubectl rollout status deployment/market-data-service -n commsec
kubectl rollout status deployment/api-gateway -n commsec

# Verify pods are running
kubectl get pods -n commsec -l tier=backend
```

### 7.4 Deploy Frontend
```bash
# Deploy frontend
kubectl apply -f kubernetes/frontend/

# Watch deployment progress
kubectl rollout status deployment/frontend -n commsec

# Verify pods are running
kubectl get pods -n commsec -l tier=frontend
```

### 7.5 Deploy Ingress
```bash
# Deploy ingress resources
kubectl apply -f kubernetes/ingress.yaml

# Wait for ALB to be created (takes 2-3 minutes)
kubectl get ingress -n commsec -w

# Get ALB DNS name
export ALB_DNS=$(kubectl get ingress -n commsec -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}')
echo "Application URL: http://$ALB_DNS"
```

---

## 8. Verification

### 8.1 Check All Resources
```bash
# Check all pods
kubectl get pods -n commsec -o wide

# Check services
kubectl get svc -n commsec

# Check ingress
kubectl get ingress -n commsec

# Check HPA status
kubectl get hpa -n commsec
```

### 8.2 Test Endpoints
```bash
# Test health endpoints
echo "Testing health endpoints..."

# API Gateway health
curl -s http://$ALB_DNS/api/health | jq .

# Trading service health
curl -s http://$ALB_DNS/api/trading/health | jq .

# Portfolio service health
curl -s http://$ALB_DNS/api/portfolio/health | jq .

# Market data service health
curl -s http://$ALB_DNS/api/market-data/health | jq .
```

### 8.3 Test Frontend
```bash
# Access frontend
echo "Frontend URL: http://$ALB_DNS"
echo "CloudFront URL: https://$CLOUDFRONT_DOMAIN"

# Open in browser or use curl
curl -s http://$ALB_DNS | head -20
```

### 8.4 Test Database Connectivity
```bash
# Check if services can connect to RDS
kubectl logs -n commsec -l app=trading-service --tail=50 | grep -i database

# Should see successful connection messages
```

### 8.5 Test WebSocket (Market Data)
```bash
# Test WebSocket connection
wscat -c ws://$ALB_DNS/api/market-data/ws/quotes

# You should receive real-time quote updates
```

---

## 9. Cleanup

### 9.1 Delete Kubernetes Resources
```bash
# Delete all Kubernetes resources
kubectl delete -f kubernetes/ --recursive

# Verify deletion
kubectl get all -n commsec
```

### 9.2 Delete ECR Images
```bash
# Delete images from ECR (optional - saves storage costs)
for repo in frontend trading-service portfolio-service market-data-service api-gateway; do
    aws ecr batch-delete-image \
        --repository-name commsec-$repo \
        --image-ids imageTag=latest \
        --region $AWS_REGION
done
```

### 9.3 Destroy Infrastructure
```bash
cd infrastructure/terraform

# Destroy all resources (takes 15-20 minutes)
terraform destroy -auto-approve

# Verify destruction
terraform show
```

### 9.4 Clean Up Additional Resources
```bash
# Delete SSM parameter
aws ssm delete-parameter --name "/commsec/production/db-password" --region $AWS_REGION

# Delete any remaining CloudWatch log groups
aws logs describe-log-groups --log-group-name-prefix /aws/eks/commsec \
    --query 'logGroups[*].logGroupName' --output text | \
    xargs -I {} aws logs delete-log-group --log-group-name {}
```

---

## Troubleshooting

### Common Issues

#### 1. Pods in ImagePullBackOff
```bash
# Check if ECR login is valid
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Check pod events
kubectl describe pod <pod-name> -n commsec
```

#### 2. Database Connection Failed
```bash
# Verify RDS security group allows EKS
# Check security group rules in AWS Console

# Verify secret is correct
kubectl get secret commsec-db-secret -n commsec -o jsonpath='{.data.host}' | base64 -d
```

#### 3. ALB Not Creating
```bash
# Check AWS Load Balancer Controller logs
kubectl logs -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller

# Verify IAM permissions
kubectl describe sa aws-load-balancer-controller -n kube-system
```

#### 4. Node Not Ready
```bash
# Check node status
kubectl describe node <node-name>

# Check AWS Console for EC2 instance status
```

---

## Next Steps

1. **Access the Platform**: Navigate to the ALB DNS or CloudFront URL
2. **Monitor**: Set up CloudWatch dashboards using the Runbook
3. **Test Resilience**: Follow the zonal shift simulation in the Runbook
4. **Review**: Check the [RUNBOOK.md](./RUNBOOK.md) for operational procedures
