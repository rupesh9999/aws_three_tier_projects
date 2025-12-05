# Instagram Clone - Production Deployment Guide v1.0

This comprehensive guide documents the complete deployment process for the Instagram Clone application on Amazon Web Services (AWS), including infrastructure provisioning, application build, and Kubernetes deployment.

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Infrastructure Deployment](#2-infrastructure-deployment)
3. [Application Build](#3-application-build)
4. [Kubernetes Deployment](#4-kubernetes-deployment)
5. [Critical Configuration Fixes](#5-critical-configuration-fixes)
6. [Monitoring Setup](#6-monitoring-setup)
7. [CI/CD Pipeline](#7-cicd-pipeline)
8. [Backup & Disaster Recovery](#8-backup--disaster-recovery)
9. [Verification](#9-verification)
10. [Troubleshooting](#10-troubleshooting)
11. [Access Information](#11-access-information)

---

## 1. Prerequisites

### Required Tools
```bash
# Verify all tools are installed
aws --version           # AWS CLI v2
kubectl version --client # Kubernetes CLI
docker --version        # Container runtime
terraform --version     # Infrastructure as Code (1.5+)
helm version            # Kubernetes package manager
java -version          # Java 21 LTS
mvn -version           # Maven 3.9+
node --version         # Node.js 20+
```

### AWS Setup
```bash
# Configure AWS CLI
aws configure
# Enter Access Key ID, Secret Access Key, Region (e.g., us-east-1), and Output format (json)

# Verify identity
aws sts get-caller-identity
```

---

## 2. Infrastructure Deployment

### 2.1 Initialize Terraform

```bash
cd terraform

# Initialize Terraform
terraform init
```

### 2.2 Configure Production Variables

Create `terraform.tfvars`:
```hcl
region       = "us-east-1"
project_name = "instagram-clone"
vpc_cidr     = "10.0.0.0/16"
db_password  = "YourSecurePassword123!" # MARK AS SENSITIVE
```

### 2.3 Apply Infrastructure

```bash
# Review changes
terraform plan

# Apply infrastructure
terraform apply -auto-approve

# Store outputs for later use
export EKS_CLUSTER_NAME=$(terraform output -raw cluster_name)
export RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
export REDIS_ENDPOINT=$(terraform output -raw redis_endpoint)
export S3_BUCKET=$(terraform output -raw s3_bucket_name)
export OIDC_ISSUER=$(terraform output -raw cluster_oidc_issuer_url)
```

### 2.4 Configure kubectl

```bash
aws eks update-kubeconfig --name $EKS_CLUSTER_NAME --region us-east-1

# Verify cluster access
kubectl get nodes
```

---

## 3. Application Build

### 3.1 Build Backend Services

```bash
cd ../backend

# Build all services
SERVICES=("auth-service" "user-service" "post-service" "feed-service" "notification-service" "ai-service" "api-gateway")

for service in "${SERVICES[@]}"; do
  echo "Building $service..."
  cd $service
  mvn clean package -DskipTests
  cd ..
done
```

### 3.2 Login to ECR

```bash
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="us-east-1"

aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
```

### 3.3 Build and Push Docker Images

```bash
# Backend services
for service in "${SERVICES[@]}"; do
  REPO_URI="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$service"
  
  # Create repo if not exists (Terraform should have done this, but safe check)
  aws ecr describe-repositories --repository-names $service >/dev/null 2>&1 || aws ecr create-repository --repository-name $service

  docker build -t $REPO_URI:latest -f $service/Dockerfile $service
  docker push $REPO_URI:latest
done

# Frontend
cd ../frontend
REPO_URI="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/frontend"
aws ecr describe-repositories --repository-names frontend || aws ecr create-repository --repository-name frontend

docker build -t $REPO_URI:latest .
docker push $REPO_URI:latest
```

---

## 4. Kubernetes Deployment

### 4.1 Create Namespace

```bash
kubectl create namespace instagram-clone
```

### 4.2 Create Secrets (CRITICAL)

**⚠️ IMPORTANT: JWT_SECRET must be properly base64-encoded!**

```bash
# Generate proper JWT secret (base64-encoded 512-bit key for HS384)
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
DB_PASSWORD="YourSecurePassword123!" # Must match Terraform input

# Create secret with ALL required keys
kubectl -n instagram-clone create secret generic app-secrets \
  --from-literal=SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
  --from-literal=JWT_SECRET="$JWT_SECRET" \
  --from-literal=AWS_ACCESS_KEY_ID="<YOUR_ACCESS_KEY>" \
  --from-literal=AWS_SECRET_ACCESS_KEY="<YOUR_SECRET_KEY>"
```
 

### 4.3 Install External Secrets Operator (With IRSA)
If using AWS Secrets Manager (as per design):
```bash
# Export the IAM Role ARN created by Terraform
export EXTERNAL_SECRETS_ROLE_ARN=$(terraform output -raw external_secrets_role_arn)

helm repo add external-secrets https://charts.external-secrets.io
# Uninstall if previously installed to ensure CRDs are applied cleanly
helm uninstall external-secrets -n external-secrets || true

helm install external-secrets external-secrets/external-secrets \
  -n external-secrets --create-namespace \
  --set installCRDs=true \
  --set serviceAccount.annotations."eks\.amazonaws\.com/role-arn"=$EXTERNAL_SECRETS_ROLE_ARN \
  --set serviceAccount.name=external-secrets-sa

# Wait for pods to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=external-secrets -n external-secrets --timeout=300s
```

### 4.4 Configure ClusterSecretStore (AWS Secrets Manager)

Create a `ClusterSecretStore` to tell the operator how to access AWS Secrets Manager.

**File:** `k8s/secrets/cluster-secret-store.yaml`
```yaml
apiVersion: external-secrets.io/v1
kind: ClusterSecretStore
metadata:
  name: aws-secrets-manager
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-east-1
      auth:
        jwt:
          serviceAccountRef:
            name: external-secrets-sa
            namespace: external-secrets
```

**Apply the configuration:**
```bash
kubectl apply -f k8s/secrets/cluster-secret-store.yaml
```

### 4.5 Sync Database Credentials

Create an `ExternalSecret` to sync the RDS password from AWS Secrets Manager to a Kubernetes Secret.

**File:** `k8s/secrets/db-credentials.yaml`
```yaml
apiVersion: external-secrets.io/v1
kind: ExternalSecret
metadata:
  name: db-credentials
  namespace: instagram-clone
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: ClusterSecretStore
  target:
    name: app-secrets
    creationPolicy: Merge
  data:
    - secretKey: SPRING_DATASOURCE_PASSWORD
      remoteRef:
        key: instagram-clone/prod/db-password
        property: password
```

**Apply the configuration:**
```bash
kubectl apply -f k8s/secrets/db-credentials.yaml

# Verify secret creation
kubectl get secret app-secrets -n instagram-clone
```
```

### 4.6 Deploy Application via Argo CD

```bash
# Install Argo CD
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for Argo CD
kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=300s

# Apply Application Manifests
kubectl apply -f ../k8s/argocd/application.yaml
```

---

## 5. Critical Configuration Fixes

### 5.1 Update Argo CD Image Parameters

You must update the `application.yaml` or Argo CD parameters to point to your ECR repositories.

```bash
# Example for Auth Service
argocd app set auth-service -p image.repository=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/auth-service
```

### 5.2 Database Initialization

The microservices use Flyway/Hibernate for schema initialization. Ensure the databases exist in RDS.

```bash
# Connect to RDS (via bastion or if public access enabled)
psql -h $RDS_ENDPOINT -U postgres -W

# Create databases
CREATE DATABASE users_db;
CREATE DATABASE posts_db;
CREATE DATABASE notifications_db;
CREATE DATABASE feed_db;
```

---

## 6. Monitoring Setup

### 6.1 Deploy Prometheus & Grafana

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

kubectl create namespace monitoring
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring
```

### 6.2 Access Grafana

```bash
# Get admin password
kubectl get secret -n monitoring prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo

# Port forward
kubectl port-forward svc/prometheus-grafana -n monitoring 3000:80
```

---

## 7. CI/CD Pipeline

The project includes a `Jenkinsfile` for automated build and push.
1.  **Deploy Jenkins**: Use Helm or EC2.
2.  **Configure Credentials**: Add AWS Credentials for ECR access.
3.  **Create Pipeline**: Point to the GitHub repository.

---

## 8. Backup & Disaster Recovery

### 8.1 RDS Backups
Automated backups are enabled by Terraform (`backup_retention_period`).
To trigger a manual snapshot:
```bash
aws rds create-db-snapshot \
    --db-instance-identifier instagram-clone-db \
    --db-snapshot-identifier manual-backup-$(date +%Y%m%d)
```

---

## 9. Verification

### 9.1 Test API Endpoints

```bash
# Get API Gateway URL (LoadBalancer)
API_URL=$(kubectl get svc api-gateway -n default -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

# Test Health
curl http://$API_URL/actuator/health

# Test Registration
curl -X POST http://$API_URL/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "name": "Test User"
  }'
```

---

## 10. Troubleshooting

### Pod CrashLoopBackOff
**Diagnosis**:
```bash
kubectl logs <pod-name> --previous
```
**Common Causes**:
1.  **DB Connection**: Check `SPRING_DATASOURCE_URL` and Security Groups.
2.  **Secrets**: Verify `JWT_SECRET` is present and valid.

### Service Unavailable
**Diagnosis**:
```bash
kubectl get endpoints <service-name>
```

---

## 11. Access Information

| Service | Access Method |
|---------|---------------|
| API Gateway | LoadBalancer URL (Port 80) |
| Argo CD | `https://localhost:8080` (via Port Forward) |
| Grafana | `http://localhost:3000` (via Port Forward) |

### Database Credentials
| Field | Value |
|-------|-------|
| Master Username | `postgres` |
| Master Password | (Defined in Terraform/Secrets) |
| Endpoint | (Terraform Output) |
