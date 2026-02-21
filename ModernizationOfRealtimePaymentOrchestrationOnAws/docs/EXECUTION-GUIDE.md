# Execution Guide — Real-Time Payment Orchestration on AWS

## Table of Contents
1. [Phase 1: Environment Setup](#phase-1-environment-setup)
2. [Phase 2: Build Applications](#phase-2-build-applications)
3. [Phase 3: Local Testing with Docker Compose](#phase-3-local-testing)
4. [Phase 4: Deploy AWS Infrastructure](#phase-4-deploy-aws-infrastructure)
5. [Phase 5: Push Container Images to ECR](#phase-5-push-images)
6. [Phase 6: Deploy to EKS](#phase-6-deploy-to-eks)
7. [Phase 7: Validation & Smoke Tests](#phase-7-validation)
8. [Phase 8: Simulate Production Scenarios](#phase-8-simulate-scenarios)
9. [Phase 9: Cleanup](#phase-9-cleanup)

---

## Phase 1: Environment Setup

### 1.1 Clone and navigate to project
```bash
cd /home/ubuntu/aws_three_tier_projects/ModernizationOfRealtimePaymentOrchestrationOnAws
```

### 1.2 Verify all prerequisites
```bash
# Run the verification script from PREREQUISITES.md
aws --version && terraform --version && kubectl version --client && \
docker --version && java -version && mvn --version && \
node --version && helm version --short
```

### 1.3 Set environment variables
```bash
export AWS_REGION="us-east-1"
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export PROJECT_NAME="payment-orchestration"
export ENVIRONMENT="production"
echo "AWS Account: $AWS_ACCOUNT_ID | Region: $AWS_REGION"
```

---

## Phase 2: Build Applications

### 2.1 Build backend services
```bash
cd backend
mvn clean package -DskipTests
# This builds all 6 microservices + common module
# Artifacts located in each service's target/ directory
ls -la payment-initiation/target/*.jar
ls -la payment-execution/target/*.jar
ls -la payment-tracking/target/*.jar
ls -la payment-reconciliation/target/*.jar
ls -la payment-billing/target/*.jar
ls -la payment-risk/target/*.jar
cd ..
```

### 2.2 Build frontend
```bash
cd frontend
npm install
npm run build
# Production build output in dist/ directory
ls -la dist/
cd ..
```

### 2.3 Build Docker images
```bash
# Backend services
docker build -t payment-initiation:latest -f backend/payment-initiation/Dockerfile backend/payment-initiation
docker build -t payment-execution:latest -f backend/payment-execution/Dockerfile backend/payment-execution
docker build -t payment-tracking:latest -f backend/payment-tracking/Dockerfile backend/payment-tracking
docker build -t payment-reconciliation:latest -f backend/payment-reconciliation/Dockerfile backend/payment-reconciliation
docker build -t payment-billing:latest -f backend/payment-billing/Dockerfile backend/payment-billing
docker build -t payment-risk:latest -f backend/payment-risk/Dockerfile backend/payment-risk

# Frontend
docker build -t payment-frontend:latest -f frontend/Dockerfile frontend

# Verify image sizes (should be < 200MB for backend, < 30MB for frontend)
docker images | grep payment
```

---

## Phase 3: Local Testing with Docker Compose

### 3.1 Start local environment
```bash
docker compose up -d
# This starts: PostgreSQL, Kafka (KRaft mode), all 6 backend services, frontend
```

### 3.2 Verify services are healthy
```bash
# Check all containers are running
docker compose ps

# Check backend health endpoints
curl -s http://localhost:8081/actuator/health | jq .
curl -s http://localhost:8082/actuator/health | jq .
curl -s http://localhost:8083/actuator/health | jq .
curl -s http://localhost:8084/actuator/health | jq .
curl -s http://localhost:8085/actuator/health | jq .
curl -s http://localhost:8086/actuator/health | jq .

# Check frontend
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000
```

### 3.3 Test payment flow
```bash
# Create a payment
curl -X POST http://localhost:8081/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "US-EAST",
    "debtorAccount": "ACC-001",
    "creditorAccount": "ACC-002",
    "amount": 250.00,
    "currency": "USD",
    "paymentType": "REAL_TIME",
    "debtorName": "John Doe",
    "creditorName": "Jane Smith"
  }' | jq .

# Track the payment (use transaction ID from above response)
curl -s http://localhost:8083/api/v1/payments/{transactionId}/status | jq .
```

### 3.4 Stop local environment
```bash
docker compose down -v
```

---

## Phase 4: Deploy AWS Infrastructure

### 4.1 Initialize Terraform
```bash
cd infrastructure/terraform
terraform init
```

### 4.2 Review the plan
```bash
terraform plan -out=tfplan \
  -var="aws_region=$AWS_REGION" \
  -var="environment=$ENVIRONMENT" \
  -var="project_name=$PROJECT_NAME"
```

### 4.3 Apply infrastructure
```bash
terraform apply tfplan
# This creates: VPC, EKS, RDS, MSK, API Gateway, CloudFront, Lambda, ECR
# Estimated time: 25-40 minutes
```

### 4.4 Capture outputs
```bash
export EKS_CLUSTER_NAME=$(terraform output -raw eks_cluster_name)
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export MSK_BOOTSTRAP=$(terraform output -raw msk_bootstrap_brokers)
export ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "EKS Cluster: $EKS_CLUSTER_NAME"
echo "RDS Endpoint: $RDS_ENDPOINT"
echo "MSK Bootstrap: $MSK_BOOTSTRAP"
echo "ECR Registry: $ECR_REGISTRY"
```

### 4.5 Configure kubectl for EKS
```bash
aws eks update-kubeconfig --name $EKS_CLUSTER_NAME --region $AWS_REGION
kubectl get nodes
```

---

## Phase 5: Push Container Images to ECR

### 5.1 Authenticate Docker to ECR
```bash
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_REGISTRY
```

### 5.2 Tag and push all images
```bash
SERVICES=("payment-initiation" "payment-execution" "payment-tracking" \
          "payment-reconciliation" "payment-billing" "payment-risk" "payment-frontend")

for SERVICE in "${SERVICES[@]}"; do
  docker tag ${SERVICE}:latest ${ECR_REGISTRY}/${PROJECT_NAME}-${SERVICE}:latest
  docker push ${ECR_REGISTRY}/${PROJECT_NAME}-${SERVICE}:latest
  echo "✅ Pushed: ${SERVICE}"
done
```

---

## Phase 6: Deploy to EKS

### 6.1 Create namespace and secrets
```bash
cd ../../kubernetes

# Create namespace
kubectl apply -f namespace.yaml

# Create secrets (update with actual values from Terraform outputs)
kubectl create secret generic payment-db-credentials \
  --namespace=payment-system \
  --from-literal=DB_HOST=$RDS_ENDPOINT \
  --from-literal=DB_PORT=5432 \
  --from-literal=DB_NAME=paymentdb \
  --from-literal=DB_USERNAME=paymentadmin \
  --from-literal=DB_PASSWORD=$(terraform -chdir=../infrastructure/terraform output -raw rds_password)

kubectl create secret generic payment-kafka-config \
  --namespace=payment-system \
  --from-literal=KAFKA_BOOTSTRAP_SERVERS=$MSK_BOOTSTRAP
```

### 6.2 Apply ConfigMaps
```bash
kubectl apply -f configmap.yaml
```

### 6.3 Deploy microservices
```bash
# Update image references in deployment files with ECR registry
export ECR_PREFIX="${ECR_REGISTRY}/${PROJECT_NAME}"

# Apply deployments
kubectl apply -f payment-initiation-deployment.yaml
kubectl apply -f payment-execution-deployment.yaml
kubectl apply -f payment-tracking-deployment.yaml
kubectl apply -f payment-reconciliation-deployment.yaml
kubectl apply -f payment-billing-deployment.yaml
kubectl apply -f payment-risk-deployment.yaml
kubectl apply -f payment-frontend-deployment.yaml

# Apply services
kubectl apply -f services.yaml

# Apply ingress
kubectl apply -f ingress.yaml

# Apply HPA
kubectl apply -f hpa.yaml
```

### 6.4 Verify deployment
```bash
kubectl get pods -n payment-system -w
kubectl get svc -n payment-system
kubectl get ingress -n payment-system
```

---

## Phase 7: Validation & Smoke Tests

### 7.1 Get API endpoint
```bash
export API_URL=$(kubectl get ingress -n payment-system -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}')
echo "API URL: http://${API_URL}"
```

### 7.2 Run smoke tests
```bash
# Health check
curl -s http://${API_URL}/api/v1/payments/health | jq .

# Create payment
curl -X POST http://${API_URL}/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "US-EAST",
    "debtorAccount": "ACC-001",
    "creditorAccount": "ACC-002",
    "amount": 1000.00,
    "currency": "USD",
    "paymentType": "REAL_TIME",
    "debtorName": "Acme Corp",
    "creditorName": "Widget Inc"
  }' | jq .

# Verify Kafka events are flowing
kubectl exec -it -n payment-system $(kubectl get pods -n payment-system -l app=payment-tracking -o jsonpath='{.items[0].metadata.name}') \
  -- curl -s localhost:8083/actuator/health | jq .
```

### 7.3 Check CloudWatch metrics
```bash
aws cloudwatch get-metric-statistics \
  --namespace "PaymentOrchestration" \
  --metric-name "TransactionCount" \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 300 \
  --statistics Sum \
  --region $AWS_REGION
```

---

## Phase 8: Simulate Production Scenarios

### 8.1 Simulate high transaction volume
```bash
# Load test with 100 concurrent payments
for i in $(seq 1 100); do
  curl -s -X POST http://${API_URL}/api/v1/payments \
    -H "Content-Type: application/json" \
    -d "{
      \"tenantId\": \"US-EAST\",
      \"debtorAccount\": \"ACC-$(printf '%03d' $i)\",
      \"creditorAccount\": \"ACC-$(printf '%03d' $((i + 100)))\",
      \"amount\": $(echo "scale=2; $RANDOM/100" | bc),
      \"currency\": \"USD\",
      \"paymentType\": \"REAL_TIME\",
      \"debtorName\": \"Sender $i\",
      \"creditorName\": \"Receiver $i\"
    }" &
done
wait
echo "✅ 100 payments submitted"
```

### 8.2 Simulate Kafka consumer lag
```bash
# Scale down consumers to simulate lag
kubectl scale deployment payment-execution -n payment-system --replicas=0
# Send more payments...
# Watch consumer lag increase
kubectl scale deployment payment-execution -n payment-system --replicas=3
```

### 8.3 Simulate database failover
```bash
# Force RDS failover (Multi-AZ)
aws rds failover-db-cluster \
  --db-cluster-identifier ${PROJECT_NAME}-db-cluster \
  --region $AWS_REGION
# Monitor application behavior during failover
kubectl logs -f -n payment-system -l app=payment-initiation --tail=50
```

### 8.4 Simulate pod crashes
```bash
# Kill a pod to test self-healing
kubectl delete pod -n payment-system $(kubectl get pods -n payment-system -l app=payment-initiation -o jsonpath='{.items[0].metadata.name}')
# Watch Kubernetes restart the pod
kubectl get pods -n payment-system -w
```

---

## Phase 9: Cleanup

### 9.1 Delete Kubernetes resources
```bash
kubectl delete namespace payment-system
```

### 9.2 Destroy AWS infrastructure
```bash
cd infrastructure/terraform
terraform destroy \
  -var="aws_region=$AWS_REGION" \
  -var="environment=$ENVIRONMENT" \
  -var="project_name=$PROJECT_NAME" \
  -auto-approve
```

### 9.3 Cleanup Docker images
```bash
docker rmi $(docker images | grep payment | awk '{print $3}') 2>/dev/null
docker system prune -f
```
