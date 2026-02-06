# Kubernetes Pod Troubleshooting Guide

This document covers common errors encountered during deployment and their solutions.

---

## Issue 1: ImagePullBackOff / ErrImagePull

### Symptoms
```
market-data-service-xxx   0/1   ImagePullBackOff   0   65s
market-data-service-xxx   0/1   ErrImagePull       0   65s
```

### Root Cause
The Docker image doesn't exist in ECR. The deployment is trying to pull:
```
863394984731.dkr.ecr.us-east-1.amazonaws.com/commsec-market-data-service:latest
```
But no images have been pushed to this repository.

### Solution
Build and push the image to ECR:

```bash
# 1. Login to ECR
aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin 863394984731.dkr.ecr.us-east-1.amazonaws.com

# 2. Build the service
cd backend/market-data-service
mvn clean package -DskipTests

# 3. Build Docker image
docker build -t commsec-market-data-service:latest .

# 4. Tag for ECR
docker tag commsec-market-data-service:latest \
    863394984731.dkr.ecr.us-east-1.amazonaws.com/commsec-market-data-service:latest

# 5. Push to ECR
docker push 863394984731.dkr.ecr.us-east-1.amazonaws.com/commsec-market-data-service:latest

# 6. Restart the deployment
kubectl rollout restart deployment/market-data-service -n commsec
```

### Verification
```bash
# Check ECR has the image
aws ecr describe-images --repository-name commsec-market-data-service --region us-east-1

# Check pods are running
kubectl get pods -n commsec -l app=market-data-service
```

---

## Issue 2: CrashLoopBackOff - Database Connection Failed

### Symptoms
```
trading-service-xxx   0/1   CrashLoopBackOff   3   2m
```

Pod logs show:
```
Caused by: java.net.UnknownHostException: commsec-postgres.xxxxxx.us-east-1.rds.amazonaws.com
```

### Root Cause
The `db-credentials` Kubernetes Secret contains a **placeholder hostname** (`xxxxxx`) instead of the actual RDS endpoint.

### Solution
Update the secret with the correct RDS endpoint:

```bash
# 1. Get the actual RDS endpoint from Terraform
RDS_ENDPOINT=$(cd infrastructure/terraform && terraform output -raw rds_address)
echo "RDS Endpoint: $RDS_ENDPOINT"
# Output: commsec-postgres.cg32wuc800vg.us-east-1.rds.amazonaws.com

# 2. Get the database password from SSM
DB_PASSWORD=$(aws ssm get-parameter \
    --name "/commsec/production/db-password" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text)

# 3. Update the secret
kubectl create secret generic db-credentials \
    --namespace commsec \
    --from-literal=host="$RDS_ENDPOINT" \
    --from-literal=port="5432" \
    --from-literal=dbname="commsec" \
    --from-literal=username="commsec_admin" \
    --from-literal=password="$DB_PASSWORD" \
    --dry-run=client -o yaml | kubectl apply -f -

# 4. Restart the pods to pick up new secret
kubectl rollout restart deployment/trading-service -n commsec
kubectl rollout restart deployment/portfolio-service -n commsec
```

### Verification
```bash
# Verify secret has correct values
kubectl get secret db-credentials -n commsec -o jsonpath='{.data.host}' | base64 -d

# Check pod logs
kubectl logs -n commsec -l app=trading-service --tail=20
```

---

## Issue 3: CrashLoopBackOff - Spring Cloud Gateway Route Error

### Symptoms
```
api-gateway-xxx   0/1   CrashLoopBackOff   5   5m
```

Pod logs show:
```
org.springframework.cloud.gateway.route.RouteRefreshListener...
reactor.core.publisher.BlockingSingleSubscriber.blockingGet...
```

### Root Cause
Spring Cloud Gateway is trying to resolve service routes on startup, but the downstream services (trading-service, portfolio-service, market-data-service) are not available yet.

### Solution
Add `spring.cloud.gateway.discovery.locator.enabled=false` or configure routes to be lazy-loaded:

**Option A**: Fix services first (recommended)
```bash
# Make sure all services are running before api-gateway
kubectl scale deployment api-gateway -n commsec --replicas=0

# Fix trading-service, portfolio-service, market-data-service first
# Then scale api-gateway back up
kubectl scale deployment api-gateway -n commsec --replicas=3
```

**Option B**: Update application.yaml to handle missing services gracefully
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false
      routes:
        - id: trading-service
          uri: lb://trading-service
          predicates:
            - Path=/api/v1/trade/**
          filters:
            - name: CircuitBreaker
              args:
                name: trading-service
                fallbackUri: forward:/fallback/trading
```

---

## Issue 4: Pods Stuck in Pending

### Symptoms
```
pod-xxx   0/1   Pending   0   5m
```

### Root Cause
Usually due to:
1. No nodes with matching `nodeSelector` labels
2. Insufficient cluster resources
3. PVC not bound

### Diagnosis
```bash
kubectl describe pod <pod-name> -n commsec | grep -A 10 "Events:"
```

### Solution for nodeSelector mismatch
```bash
# Check node labels
kubectl get nodes --show-labels | grep tier

# If nodes don't have tier labels, add them
kubectl label nodes <node-name> tier=app
kubectl label nodes <node-name> tier=web
```

Or remove the nodeSelector from deployments temporarily:
```yaml
spec:
  template:
    spec:
      # nodeSelector:      # Comment out or remove
      #   tier: app
```

---

## Quick Reference Commands

```bash
# View all pod statuses
kubectl get pods -n commsec -o wide

# View pod events
kubectl describe pod <pod-name> -n commsec

# View pod logs
kubectl logs <pod-name> -n commsec --tail=100

# View logs for all pods with a label
kubectl logs -n commsec -l app=trading-service --tail=50

# Restart all pods in a deployment
kubectl rollout restart deployment/<deployment-name> -n commsec

# Delete stuck pods
kubectl delete pod <pod-name> -n commsec

# Force delete stuck pod
kubectl delete pod <pod-name> -n commsec --grace-period=0 --force

# Check secrets
kubectl get secrets -n commsec
kubectl get secret <secret-name> -n commsec -o yaml

# Check ECR images
aws ecr describe-images --repository-name <repo-name> --region us-east-1
```

---

## Summary of Current Issues and Fixes

| Service | Error | Root Cause | Fix |
|---------|-------|------------|-----|
| market-data-service | ImagePullBackOff | Image not in ECR | Build and push Docker image |
| trading-service | CrashLoopBackOff | Wrong RDS hostname in secret | Update db-credentials secret |
| portfolio-service | CrashLoopBackOff | Wrong RDS hostname in secret | Update db-credentials secret |
| api-gateway | CrashLoopBackOff | Services unavailable at startup | Fix other services first |
