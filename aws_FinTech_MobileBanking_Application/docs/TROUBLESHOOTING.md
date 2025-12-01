# Troubleshooting Guide

This guide covers common issues and their solutions when working with the FinTech Mobile Banking Application.

---

## Table of Contents

1. [Docker Issues](#1-docker-issues)
2. [Kubernetes Issues](#2-kubernetes-issues)
3. [API Gateway Issues](#3-api-gateway-issues)
4. [Database Issues](#4-database-issues)
5. [SQS Queue Issues](#5-sqs-queue-issues)
6. [Frontend Issues](#6-frontend-issues)
7. [Backend Service Issues](#7-backend-service-issues)
8. [Terraform Issues](#8-terraform-issues)
9. [Security & Authentication Issues](#9-security--authentication-issues)

---

## 1. Docker Issues

### 1.1 Docker Build Fails

**Symptom:** `docker build` command fails with errors.

**Common Causes & Solutions:**

```bash
# Issue: Out of disk space
docker system prune -a --volumes

# Issue: Layer caching problems
docker build --no-cache -t <image-name> .

# Issue: Network timeout during package download
# Add retry logic or use alternative mirrors
docker build --network=host -t <image-name> .
```

### 1.2 Image Pull Failures

**Symptom:** `Error: ImagePullBackOff` in Kubernetes

```bash
# Check if image exists in ECR
aws ecr describe-images --repository-name fintech-auth-service

# Verify ECR login
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Check Kubernetes secret for ECR
kubectl get secret ecr-registry-secret -n fintech-banking -o yaml

# Recreate ECR secret if needed
kubectl delete secret ecr-registry-secret -n fintech-banking
kubectl create secret docker-registry ecr-registry-secret \
  --docker-server=<account-id>.dkr.ecr.us-east-1.amazonaws.com \
  --docker-username=AWS \
  --docker-password=$(aws ecr get-login-password --region us-east-1) \
  -n fintech-banking
```

### 1.3 Container Crashes on Start

**Symptom:** Container exits immediately after starting.

```bash
# Check container logs
docker logs <container-id>

# Run container interactively
docker run -it --entrypoint /bin/sh <image-name>

# Check for missing environment variables
docker run --env-file .env <image-name>
```

---

## 2. Kubernetes Issues

### 2.1 Pods Not Starting

**Symptom:** Pods stuck in `Pending` or `CrashLoopBackOff` state.

```bash
# Describe pod for events
kubectl describe pod <pod-name> -n fintech-banking

# Check pod logs
kubectl logs <pod-name> -n fintech-banking
kubectl logs <pod-name> -n fintech-banking --previous  # Previous crashed instance

# Common issues:
# - Insufficient resources: Check node capacity
kubectl describe nodes | grep -A 5 "Allocated resources"

# - Missing ConfigMaps/Secrets
kubectl get configmaps -n fintech-banking
kubectl get secrets -n fintech-banking
```

### 2.2 Service Not Accessible

**Symptom:** Cannot reach service endpoints.

```bash
# Check service exists and has endpoints
kubectl get svc -n fintech-banking
kubectl get endpoints -n fintech-banking

# Test from within cluster
kubectl run test-pod --rm -it --image=busybox --restart=Never -- wget -qO- http://auth-service:8080/actuator/health

# Check network policies
kubectl get networkpolicies -n fintech-banking
```

### 2.3 Ingress Not Working

**Symptom:** External access to services fails.

```bash
# Check ingress controller
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx

# Check ingress resource
kubectl describe ingress fintech-ingress -n fintech-banking

# Verify ingress class
kubectl get ingressclass

# Check if ALB/NLB is created (for AWS)
aws elbv2 describe-load-balancers | grep fintech
```

### 2.4 Persistent Volume Issues

**Symptom:** Pods fail with volume mounting errors.

```bash
# Check PVC status
kubectl get pvc -n fintech-banking
kubectl describe pvc <pvc-name> -n fintech-banking

# Check storage class
kubectl get storageclass

# For EBS volumes, ensure CSI driver is installed
kubectl get pods -n kube-system | grep ebs-csi
```

---

## 3. API Gateway Issues

### 3.1 HTTP 4xx Errors

#### 400 Bad Request
```bash
# Check request format
# Ensure Content-Type header is set correctly
curl -X POST <api-url>/endpoint \
  -H "Content-Type: application/json" \
  -d '{"valid": "json"}'
```

#### 401 Unauthorized
```bash
# Verify JWT token is valid and not expired
# Check token format: Bearer <token>
curl <api-url>/endpoint \
  -H "Authorization: Bearer <your-jwt-token>"

# Decode JWT to check expiry
echo "<jwt-token>" | cut -d'.' -f2 | base64 -d | jq '.exp'
```

#### 403 Forbidden
```bash
# Check IAM permissions
# Check resource policy on API Gateway
aws apigateway get-rest-api --rest-api-id <api-id>
aws apigateway get-resource-policy --rest-api-id <api-id>
```

#### 429 Too Many Requests
```bash
# Rate limit exceeded - wait and retry
# Check throttling settings in API Gateway
aws apigateway get-usage-plans
```

### 3.2 HTTP 5xx Errors

#### 500 Internal Server Error
```bash
# Check backend service logs
kubectl logs -f deployment/auth-service -n fintech-banking

# Check CloudWatch logs for API Gateway
aws logs filter-log-events \
  --log-group-name "API-Gateway-Execution-Logs_<api-id>/prod"
```

#### 502 Bad Gateway
```bash
# Backend service not responding
# Check service health
kubectl get pods -n fintech-banking
kubectl describe pod <pod-name>

# Check VPC Link (for private integrations)
aws apigateway get-vpc-links
```

#### 503 Service Unavailable
```bash
# Service is overloaded or unavailable
# Check pod status and scaling
kubectl get hpa -n fintech-banking
kubectl scale deployment auth-service --replicas=3 -n fintech-banking
```

#### 504 Gateway Timeout
```bash
# Backend taking too long
# Check API Gateway timeout settings (max 29s)
# Optimize backend queries/processing
# Check database connection pool exhaustion
```

---

## 4. Database Issues

### 4.1 Connection Failures

```bash
# Test connectivity from pod
kubectl run psql-test --rm -it --image=postgres:16 --restart=Never -- \
  psql -h <rds-endpoint> -U <username> -d fintech_banking_db -c "SELECT 1"

# Check security groups
aws ec2 describe-security-groups --group-ids <rds-sg-id>

# Check RDS status
aws rds describe-db-instances --db-instance-identifier fintech-banking-db

# Common issues:
# - VPC security group not allowing traffic from EKS
# - RDS not publicly accessible (correct for prod)
# - Wrong credentials
```

### 4.2 Connection Pool Exhaustion

**Symptom:** `Connection pool exhausted` errors.

```bash
# Check active connections
psql -h <rds-endpoint> -U <username> -d fintech_banking_db -c \
  "SELECT count(*) FROM pg_stat_activity WHERE state = 'active';"

# Increase pool size in application.yml
# spring.datasource.hikari.maximum-pool-size: 20

# Kill idle connections (last resort)
psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE state = 'idle' AND query_start < now() - interval '10 minutes';"
```

### 4.3 Migration Failures

```bash
# Check Flyway migration status
./gradlew :account-service:flywayInfo

# Repair failed migrations
./gradlew :account-service:flywayRepair

# Check migration table
psql -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;"
```

### 4.4 Performance Issues

```bash
# Check slow queries
psql -c "SELECT query, calls, mean_time FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 10;"

# Check table bloat
psql -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname || '.' || tablename)) FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname || '.' || tablename) DESC;"

# Run VACUUM ANALYZE
psql -c "VACUUM ANALYZE;"
```

---

## 5. SQS Queue Issues

### 5.1 Messages Not Being Processed

```bash
# Check queue depth
aws sqs get-queue-attributes \
  --queue-url <queue-url> \
  --attribute-names ApproximateNumberOfMessages

# Check DLQ for failed messages
aws sqs receive-message \
  --queue-url <dlq-url> \
  --max-number-of-messages 10

# Verify consumer is running
kubectl logs -f deployment/notification-service -n fintech-banking | grep SQS
```

### 5.2 Messages Going to DLQ

```bash
# Read DLQ messages to understand failures
aws sqs receive-message \
  --queue-url <dlq-url> \
  --attribute-names All \
  --message-attribute-names All

# Check consumer error logs
kubectl logs deployment/transaction-service -n fintech-banking | grep -i error

# Redrive messages from DLQ (after fixing issue)
# Use AWS Console or write a redrive script
```

### 5.3 High Latency

```bash
# Check queue metrics in CloudWatch
aws cloudwatch get-metric-statistics \
  --namespace AWS/SQS \
  --metric-name ApproximateAgeOfOldestMessage \
  --dimensions Name=QueueName,Value=<queue-name> \
  --start-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%SZ) \
  --period 300 \
  --statistics Average

# Scale up consumers
kubectl scale deployment notification-service --replicas=3 -n fintech-banking
```

---

## 6. Frontend Issues

### 6.1 Build Failures

```bash
# Clear npm cache
npm cache clean --force
rm -rf node_modules
rm package-lock.json
npm install

# Check for dependency conflicts
npm ls

# Build with verbose logging
npm run build -- --verbose
```

### 6.2 CORS Errors

**Symptom:** `Access-Control-Allow-Origin` errors in browser console.

```bash
# Verify CORS configuration in backend
# Check application.yml or @CrossOrigin annotations

# API Gateway CORS settings
aws apigateway get-method \
  --rest-api-id <api-id> \
  --resource-id <resource-id> \
  --http-method OPTIONS

# Ensure CloudFront is forwarding necessary headers
```

### 6.3 API Connection Failures

```bash
# Check API base URL configuration
# In .env: REACT_APP_API_BASE_URL=https://api.example.com

# Test API directly
curl -v <api-url>/health

# Check browser developer tools Network tab for actual requests
```

---

## 7. Backend Service Issues

### 7.1 OutOfMemoryError

```bash
# Increase JVM heap
# In Dockerfile or K8s manifest:
# JAVA_OPTS=-Xmx512m -Xms256m

# Check memory usage
kubectl top pods -n fintech-banking

# Update resource limits in K8s
kubectl edit deployment auth-service -n fintech-banking
# resources:
#   requests:
#     memory: "512Mi"
#   limits:
#     memory: "1Gi"
```

### 7.2 Slow Startup

```bash
# Enable lazy initialization
# spring.main.lazy-initialization=true

# Check startup logs for bottlenecks
kubectl logs deployment/auth-service -n fintech-banking | grep "Started"

# Use Spring Boot startup actuator
curl http://<service>/actuator/startup
```

### 7.3 Health Check Failures

```bash
# Check actuator health endpoint
curl http://<service>:8080/actuator/health

# Check specific health indicators
curl http://<service>:8080/actuator/health/db
curl http://<service>:8080/actuator/health/diskSpace

# Adjust health check settings in K8s
# livenessProbe:
#   initialDelaySeconds: 60
#   periodSeconds: 10
```

---

## 8. Terraform Issues

### 8.1 State Lock Issues

```bash
# Check if state is locked
terraform force-unlock <lock-id>

# Use with caution - ensure no other operations running
```

### 8.2 Resource Already Exists

```bash
# Import existing resource
terraform import module.rds.aws_db_instance.main <db-instance-id>

# Or remove from state and recreate
terraform state rm <resource-address>
```

### 8.3 Dependency Errors

```bash
# Visualize dependencies
terraform graph | dot -Tpng > graph.png

# Apply specific module first
terraform apply -target=module.vpc
terraform apply -target=module.eks
terraform apply
```

---

## 9. Security & Authentication Issues

### 9.1 JWT Token Issues

```bash
# Decode JWT (without validation)
echo "<token>" | cut -d'.' -f2 | base64 -d | jq .

# Check token expiry
# Verify issuer (iss) and audience (aud) claims

# Common issues:
# - Token expired
# - Wrong signing key
# - Invalid audience
```

### 9.2 Secrets Manager Access

```bash
# Test secret retrieval
aws secretsmanager get-secret-value --secret-id fintech/db/credentials

# Check IAM role permissions
aws iam get-role-policy --role-name <eks-node-role> --policy-name <policy-name>

# Verify IRSA (IAM Roles for Service Accounts)
kubectl describe sa <service-account> -n fintech-banking
```

### 9.3 WAF Blocking Requests

```bash
# Check WAF logs in CloudWatch
aws logs filter-log-events \
  --log-group-name "aws-waf-logs-fintech" \
  --filter-pattern "BLOCK"

# Review WAF rules
aws wafv2 get-web-acl --name fintech-waf --scope CLOUDFRONT --id <waf-id>
```

---

## Quick Diagnostic Commands

```bash
#!/bin/bash
# Save as scripts/diagnose.sh

echo "=== EKS Cluster Status ==="
kubectl cluster-info

echo "=== Pod Status ==="
kubectl get pods -n fintech-banking -o wide

echo "=== Recent Events ==="
kubectl get events -n fintech-banking --sort-by='.lastTimestamp' | tail -20

echo "=== Service Endpoints ==="
kubectl get endpoints -n fintech-banking

echo "=== Resource Usage ==="
kubectl top pods -n fintech-banking

echo "=== SQS Queue Depth ==="
aws sqs get-queue-attributes \
  --queue-url <queue-url> \
  --attribute-names ApproximateNumberOfMessages 2>/dev/null || echo "SQS not configured"

echo "=== RDS Status ==="
aws rds describe-db-instances \
  --db-instance-identifier fintech-banking-db \
  --query 'DBInstances[0].DBInstanceStatus' 2>/dev/null || echo "RDS not configured"
```

---

## Getting Help

1. Check CloudWatch Logs for detailed error messages
2. Review Prometheus metrics and Grafana dashboards
3. Search AWS documentation for specific error codes
4. Check GitHub issues for similar problems
5. Contact team via Slack #fintech-banking-support
