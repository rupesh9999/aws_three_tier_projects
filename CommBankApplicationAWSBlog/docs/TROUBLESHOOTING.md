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

---

## Issue 5: RDS Password Authentication Failed (ManageMasterUserPassword)

### Symptoms
```
Caused by: org.postgresql.util.PSQLException: FATAL: password authentication failed for user "commsec_admin"
```

When trying to reset the RDS password:
```
An error occurred (InvalidParameterValue) when calling the ModifyDBInstance operation: 
You can't specify MasterUserPassword for an instance with ManageMasterUserPassword enabled.
```

### Root Cause
The RDS instance was created with `manage_master_user_password = true`, which means AWS Secrets Manager manages the password automatically. The password is NOT in SSM Parameter Store.

### Solution
Use the password from AWS Secrets Manager instead of SSM:

```bash
# 1. Find the RDS managed secret
aws secretsmanager list-secrets --region us-east-1 \
    --query "SecretList[?contains(Name, 'rds')].Name" --output text
# Example output: rds!db-50c65669-5d94-4584-8eb1-01ed1e225f4f

# 2. Get the actual password from Secrets Manager
RDS_SECRET_ID="rds!db-50c65669-5d94-4584-8eb1-01ed1e225f4f"
DB_PASSWORD=$(aws secretsmanager get-secret-value \
    --secret-id "$RDS_SECRET_ID" \
    --query 'SecretString' --output text | jq -r '.password')

# 3. Get RDS endpoint
RDS_HOST=$(cd infrastructure/terraform && terraform output -raw rds_address)

# 4. Update Kubernetes secret with correct password
kubectl create secret generic db-credentials \
    --namespace commsec \
    --from-literal=host="$RDS_HOST" \
    --from-literal=port="5432" \
    --from-literal=dbname="commsec" \
    --from-literal=username="commsec_admin" \
    --from-literal=password="$DB_PASSWORD" \
    --dry-run=client -o yaml | kubectl apply -f -

# 5. Restart pods
kubectl rollout restart deployment/trading-service -n commsec
```

### Verification
```bash
# Test database connection from a pod
kubectl run -it --rm postgres-test --image=postgres:15 --restart=Never -n commsec -- \
    psql "postgresql://commsec_admin:${DB_PASSWORD}@${RDS_HOST}:5432/commsec" -c '\l'
```

---

## Issue 6: Git Push Authentication Failed

### Symptoms
```
remote: Invalid username or token. Password authentication is not supported for Git operations.
fatal: Authentication failed for 'https://github.com/username/repo.git/'
```

### Root Cause
GitHub deprecated password authentication for Git operations in August 2021. HTTPS remotes now require:
- Personal Access Token (PAT)
- SSH key authentication

### Solution

**Option A: Switch to SSH (Recommended)**
```bash
# Check current remote
git remote -v
# origin  https://github.com/username/repo.git (fetch)

# Switch to SSH
git remote set-url origin git@github.com:username/repo.git

# Verify
git remote -v
# origin  git@github.com:username/repo.git (fetch)

# Push
git push origin main
```

**Option B: Use Personal Access Token**
```bash
# Generate a token at https://github.com/settings/tokens
# Then push using the token as password
git push origin main
# Username: your-username
# Password: ghp_xxxxxxxxxxxxxxxxxxxx  (your PAT)

# Or cache credentials
git config --global credential.helper store
```

### SSH Key Setup (if needed)
```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "your_email@example.com"

# Add to SSH agent
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# Copy public key
cat ~/.ssh/id_ed25519.pub

# Add to GitHub: Settings → SSH and GPG keys → New SSH key
```

---

## Issue 7: Frontend Build Failed - Terser Not Found

### Symptoms
```
[vite]: Rollup failed to resolve import "terser" from "..."
error during build:
Error: Cannot find package 'terser'
```

### Root Cause
The `vite.config.js` specifies `minify: 'terser'` but terser is not installed as a dependency.

### Solution
Use esbuild instead (built-in to Vite, no extra install needed):

```javascript
// vite.config.js
export default defineConfig({
    build: {
        outDir: 'dist',
        sourcemap: false,
        minify: 'esbuild',  // Changed from 'terser' to 'esbuild'
        // ...
    }
});
```

Or install terser:
```bash
npm install terser --save-dev
```

---

## Issue 8: Docker Build Failed - nginx Group Already Exists

### Symptoms
```
ERROR [production 4/4] RUN addgroup -g 1001 -S nginx && ...
addgroup: group 'nginx' in use
```

### Root Cause
The `nginx:alpine` base image already includes the `nginx` user and group. The Dockerfile tries to create them again.

### Solution
Remove the `addgroup` command since nginx user/group already exists:

```dockerfile
# Before (broken)
RUN addgroup -g 1001 -S nginx && \
    chown -R nginx:nginx /usr/share/nginx/html

# After (fixed)
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid
```

---

## Issue 9: Maven Wrapper Not Found (./mvnw)

### Symptoms
```
-bash: ./mvnw: No such file or directory
```

### Root Cause
The Maven wrapper files (`mvnw`, `mvnw.cmd`, `.mvn/`) were not created with the project.

### Solution
Use the system Maven instead:

```bash
# Instead of
./mvnw clean package -DskipTests

# Use
mvn clean package -DskipTests
```

Or create the Maven wrapper:
```bash
mvn -N io.takari:maven:wrapper
```

---

## Terraform Issues

### Issue T1: CloudFront Account Verification Required

**Symptoms:**
```
Error: creating CloudFront Distribution: AccessDenied: 
Your account must be verified before you can add new CloudFront resources
```

**Solution:**
1. Contact AWS Support to verify your account
2. Or disable CloudFront temporarily:
```hcl
# terraform.tfvars
enable_cloudfront = false
```

### Issue T2: RDS Parameter Group Static Parameters

**Symptoms:**
```
Error: InvalidParameterCombination: Cannot apply immediate static parameter
```

**Solution:**
Static parameters like `shared_preload_libraries` and `max_connections` require `apply_method = "pending-reboot"`:

```hcl
parameters = [
  {
    name         = "shared_preload_libraries"
    value        = "pg_stat_statements"
    apply_method = "pending-reboot"  # NOT "immediate"
  },
  {
    name         = "max_connections"
    value        = "500"
    apply_method = "pending-reboot"  # NOT "immediate"
  }
]
```

---

## Quick Debugging Checklist

1. **Pod not starting?**
   - Check `kubectl describe pod <pod-name> -n commsec`
   - Check `kubectl logs <pod-name> -n commsec`

2. **Image not pulling?**
   - Verify image exists in ECR: `aws ecr describe-images --repository-name <repo>`
   - Check ECR login: `aws ecr get-login-password | docker login ...`

3. **Database connection failed?**
   - Check secret has correct host: `kubectl get secret db-credentials -n commsec -o yaml`
   - Check if RDS uses Secrets Manager: `aws secretsmanager list-secrets`

4. **Git push failed?**
   - Switch to SSH: `git remote set-url origin git@github.com:user/repo.git`

5. **Build failed?**
   - Check dependencies: `npm ls` or `mvn dependency:tree`
   - Check config files for missing packages

