# Production Runbook — Real-Time Payment Orchestration

## Table of Contents
1. [Service Overview](#service-overview)
2. [Incident Severity Levels](#incident-severity-levels)
3. [Common Incidents & Resolution](#common-incidents--resolution)
4. [Monitoring & Alerting](#monitoring--alerting)
5. [Scaling Procedures](#scaling-procedures)
6. [Disaster Recovery](#disaster-recovery)
7. [Maintenance Procedures](#maintenance-procedures)

---

## Service Overview

| Component | Technology | Health Check | Critical SLA |
|-----------|-----------|-------------|--------------|
| Payment Initiation | Spring Boot 3.4 / EKS | `/actuator/health` on port 8081 | < 200ms p99 |
| Payment Execution | Spring Boot 3.4 / EKS | `/actuator/health` on port 8082 | < 500ms p99 |
| Payment Tracking | Spring Boot 3.4 / EKS | `/actuator/health` on port 8083 | < 100ms p99 |
| Payment Reconciliation | Spring Boot 3.4 / EKS | `/actuator/health` on port 8084 | < 2s p99 |
| Payment Billing | Spring Boot 3.4 / EKS | `/actuator/health` on port 8085 | < 1s p99 |
| Payment Risk | Spring Boot 3.4 / EKS | `/actuator/health` on port 8086 | < 300ms p99 |
| PostgreSQL | RDS 16 Multi-AZ | RDS console / `pg_isready` | 99.999% uptime |
| Kafka | MSK 3.7 | MSK console / broker metrics | < 10ms produce latency |
| API Gateway | AWS Managed | CloudWatch metrics | 99.99% uptime |

---

## Incident Severity Levels

| Level | Description | Response Time | Example |
|-------|-------------|---------------|---------|
| **SEV-1** | Complete payment processing failure | 5 minutes | All services down, Kafka cluster unavailable |
| **SEV-2** | Degraded payment processing | 15 minutes | High latency, single service failure |
| **SEV-3** | Partial feature unavailability | 1 hour | Billing service down, tracking delayed |
| **SEV-4** | Minor issue, no user impact | 4 hours | Log aggregation failure, metric gap |

---

## Common Incidents & Resolution

### INC-001: Payment Initiation Service — JDBCConnectionException

**Symptoms**: `org.hibernate.exception.JDBCConnectionException` in logs, 503 errors on `/api/v1/payments`

**Diagnosis**:
```bash
# 1. Check pod logs
kubectl logs -n payment-system -l app=payment-initiation --tail=100

# 2. Check RDS status
aws rds describe-db-instances \
  --db-instance-identifier payment-orchestration-db \
  --query "DBInstances[0].DBInstanceStatus" --output text

# 3. Test connectivity from pod
kubectl exec -it -n payment-system $(kubectl get pods -n payment-system \
  -l app=payment-initiation -o jsonpath='{.items[0].metadata.name}') \
  -- nc -zv $RDS_ENDPOINT 5432

# 4. Check security groups
aws ec2 describe-security-groups --filters \
  "Name=tag:Name,Values=*payment*rds*" \
  --query "SecurityGroups[].IpPermissions" --output json
```

**Resolution**:
```bash
# If security group issue:
aws ec2 authorize-security-group-ingress \
  --group-id <rds-sg-id> \
  --protocol tcp --port 5432 \
  --source-group <eks-node-sg-id>

# Restart pods to apply
kubectl rollout restart deployment/payment-initiation -n payment-system
```

---

### INC-002: Kafka Consumer Lag — Messages Accumulating

**Symptoms**: Payments stuck in `INITIATED` status, consumer lag increasing

**Diagnosis**:
```bash
# 1. Check MSK consumer group lag
aws kafka describe-cluster --cluster-arn <msk-arn> \
  --query "ClusterInfo.State" --output text

# 2. Check consumer pods
kubectl get pods -n payment-system -l app=payment-execution
kubectl logs -n payment-system -l app=payment-execution --tail=50

# 3. Check Kafka topic details from a pod
kubectl exec -it -n payment-system $(kubectl get pods -n payment-system \
  -l app=payment-execution -o jsonpath='{.items[0].metadata.name}') \
  -- /bin/sh -c "echo 'Check consumer lag in actuator metrics'"
```

**Resolution**:
```bash
# Scale up consumers
kubectl scale deployment payment-execution -n payment-system --replicas=5

# If stuck, restart consumer group
kubectl rollout restart deployment/payment-execution -n payment-system

# Monitor recovery
kubectl logs -f -n payment-system -l app=payment-execution --tail=20
```

---

### INC-003: High Latency on Payment Processing

**Symptoms**: p99 latency exceeding 500ms SLA, timeout errors

**Diagnosis**:
```bash
# 1. Check HPA status
kubectl get hpa -n payment-system

# 2. Check node resource utilization
kubectl top nodes
kubectl top pods -n payment-system

# 3. Check for throttling
kubectl describe pods -n payment-system -l app=payment-initiation | grep -A5 "Limits"
```

**Resolution**:
```bash
# Increase resource limits if needed
kubectl set resources deployment/payment-initiation -n payment-system \
  --limits=cpu=1,memory=1Gi --requests=cpu=500m,memory=512Mi

# Scale horizontally
kubectl scale deployment payment-initiation -n payment-system --replicas=5

# Check CloudWatch for API Gateway throttling
aws cloudwatch get-metric-statistics \
  --namespace "AWS/ApiGateway" --metric-name "Count" \
  --start-time $(date -u -d "30 minutes ago" +%Y-%m-%dT%H:%M:%S) \
  --end-time $(date -u +%Y-%m-%dT%H:%M:%S) \
  --period 60 --statistics Sum
```

---

### INC-004: RDS Failover Event

**Symptoms**: Brief connection errors across services, automatic failover notification

**Diagnosis**:
```bash
# 1. Check RDS events
aws rds describe-events --source-type db-instance \
  --source-identifier payment-orchestration-db --duration 60

# 2. Check current endpoint
aws rds describe-db-instances \
  --db-instance-identifier payment-orchestration-db \
  --query "DBInstances[0].[Endpoint.Address,DBInstanceStatus]" --output text
```

**Resolution**:
```bash
# Services should auto-recover with connection pool retry logic
# If not, force restart all backend services
kubectl rollout restart deployment -n payment-system -l tier=backend

# Verify connections restored
for svc in payment-initiation payment-execution payment-tracking; do
  kubectl logs -n payment-system -l app=$svc --tail=10 | grep -i "connection"
done
```

---

### INC-005: Pod CrashLoopBackOff

**Symptoms**: Pods restarting repeatedly, `CrashLoopBackOff` status

**Diagnosis**:
```bash
# 1. Check pod events
kubectl describe pod -n payment-system <pod-name>

# 2. Check previous container logs
kubectl logs -n payment-system <pod-name> --previous

# 3. Common causes:
#    - OOMKilled: check memory limits
#    - Config errors: check ConfigMap/Secret mounts
#    - Dependency unavailable: check DB/Kafka connectivity
```

**Resolution**:
```bash
# If OOMKilled
kubectl set resources deployment/<service> -n payment-system \
  --limits=memory=1.5Gi --requests=memory=768Mi

# If config error
kubectl get configmap payment-config -n payment-system -o yaml
# Fix and reapply
kubectl apply -f kubernetes/configmap.yaml

# If dependency issue — check INC-001 or INC-002
```

---

## Monitoring & Alerting

### Key CloudWatch Alarms
| Alarm | Metric | Threshold | Action |
|-------|--------|-----------|--------|
| High Error Rate | 5xx errors > 1% | 1 minute | Page on-call |
| Consumer Lag | Kafka lag > 1000 | 5 minutes | Scale consumers |
| DB CPU High | RDS CPU > 80% | 5 minutes | Review queries |
| Node Memory | EKS node memory > 85% | 5 minutes | Scale nodes |
| Lambda Errors | Lambda error rate > 5% | 1 minute | Check DLQ |

### Useful kubectl commands
```bash
# Quick cluster health
kubectl get pods -n payment-system -o wide
kubectl top pods -n payment-system --sort-by=memory
kubectl get events -n payment-system --sort-by=.lastTimestamp | tail -20

# Service logs
kubectl logs -n payment-system -l app=payment-initiation -f --tail=50

# Describe resources
kubectl describe hpa -n payment-system
```

---

## Scaling Procedures

### Horizontal scaling (pods)
```bash
# Manual scale
kubectl scale deployment/<service-name> -n payment-system --replicas=<count>

# HPA adjustments
kubectl patch hpa <service-name>-hpa -n payment-system \
  -p '{"spec":{"maxReplicas": 10}}'
```

### Vertical scaling (EKS nodes)
```bash
# Update node group
aws eks update-nodegroup-config \
  --cluster-name $EKS_CLUSTER_NAME \
  --nodegroup-name payment-nodes \
  --scaling-config minSize=2,maxSize=10,desiredSize=4
```

---

## Disaster Recovery

### RDS Point-in-Time Recovery
```bash
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier payment-orchestration-db \
  --target-db-instance-identifier payment-orchestration-db-restored \
  --restore-time $(date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ) \
  --db-instance-class db.r6g.large
```

### MSK Cluster Recovery
```bash
# MSK is Multi-AZ by default. If a broker fails:
# 1. Check cluster status
aws kafka describe-cluster --cluster-arn <arn> --query "ClusterInfo.State"
# 2. MSK automatically replaces failed brokers
# 3. Monitor replication catch-up via CloudWatch
```

---

## Maintenance Procedures

### Rolling deployment (zero-downtime)
```bash
# Update image tag
kubectl set image deployment/<service-name> \
  <service-name>=${ECR_REGISTRY}/${PROJECT_NAME}-<service-name>:v2.0.0 \
  -n payment-system

# Monitor rollout
kubectl rollout status deployment/<service-name> -n payment-system

# Rollback if issues
kubectl rollout undo deployment/<service-name> -n payment-system
```

### Database migrations
```bash
# Always backup before migration
aws rds create-db-snapshot \
  --db-instance-identifier payment-orchestration-db \
  --db-snapshot-identifier pre-migration-$(date +%Y%m%d)

# Run Flyway migration (built into Spring Boot services)
# Migrations auto-apply on service startup via spring.flyway.enabled=true
```
