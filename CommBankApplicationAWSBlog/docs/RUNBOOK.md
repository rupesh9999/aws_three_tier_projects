# CommSec Trading Platform - Operational Runbook

## Overview
This runbook provides operational procedures for managing the CommSec Trading Platform in production. It covers routine operations, incident response, and disaster recovery procedures.

---

## Table of Contents
1. [Daily Operations](#1-daily-operations)
2. [Scaling Procedures](#2-scaling-procedures)
3. [ARC Zonal Shift Operations](#3-arc-zonal-shift-operations)
4. [Incident Response](#4-incident-response)
5. [Database Operations](#5-database-operations)
6. [Monitoring & Alerting](#6-monitoring--alerting)
7. [Backup & Recovery](#7-backup--recovery)
8. [Troubleshooting Guide](#8-troubleshooting-guide)

---

## 1. Daily Operations

### 1.1 Health Check Procedure
```bash
#!/bin/bash
# daily-health-check.sh

echo "=== CommSec Daily Health Check ==="
echo "Timestamp: $(date -u '+%Y-%m-%d %H:%M:%S UTC')"

# Check cluster status
echo -e "\n--- EKS Cluster Status ---"
kubectl get nodes -o wide
kubectl top nodes

# Check all pods
echo -e "\n--- Pod Status ---"
kubectl get pods -n commsec -o wide

# Check for any pods not in Running state
UNHEALTHY=$(kubectl get pods -n commsec --field-selector=status.phase!=Running -o name 2>/dev/null)
if [ -n "$UNHEALTHY" ]; then
    echo "⚠️ UNHEALTHY PODS FOUND:"
    echo "$UNHEALTHY"
else
    echo "✓ All pods are healthy"
fi

# Check service endpoints
echo -e "\n--- Service Endpoints ---"
kubectl get endpoints -n commsec

# Check recent events
echo -e "\n--- Recent Events (Last 1 hour) ---"
kubectl get events -n commsec --sort-by='.lastTimestamp' | tail -20

# Check resource usage
echo -e "\n--- Resource Usage ---"
kubectl top pods -n commsec
```

### 1.2 Pre-Market Open Checklist (9:00 AM AEST)
> **Note**: Market opens at 10:00 AM AEST. Perform these checks 1 hour before.

| Check | Command | Expected Result |
|-------|---------|-----------------|
| Pods Running | `kubectl get pods -n commsec` | All pods Running |
| Node Capacity | `kubectl top nodes` | <80% CPU/Memory |
| Database Connection | Check application logs | Connected |
| ALB Health | AWS Console > Load Balancers | All targets healthy |
| CloudFront | AWS Console > CloudFront | Origin healthy |

### 1.3 LCU Pre-Warming (9:30 AM AEST)
Traffic spikes 3x at market open. Pre-warm ALB capacity:

```bash
# Check current LCU capacity
aws elbv2 describe-load-balancer-attributes \
    --load-balancer-arn $WEB_ALB_ARN \
    --query 'Attributes[?Key==`load_balancing.cross_zone.enabled`]'

# Enable LCU reservation if not already enabled
# This is configured in Terraform but verify it's active
```

---

## 2. Scaling Procedures

### 2.1 Horizontal Pod Autoscaler (HPA) Status
```bash
# Check current HPA status
kubectl get hpa -n commsec

# View HPA details
kubectl describe hpa frontend-hpa -n commsec
kubectl describe hpa trading-service-hpa -n commsec
```

### 2.2 Manual Scaling (Emergency)
```bash
# Scale frontend for high traffic
kubectl scale deployment frontend -n commsec --replicas=10

# Scale trading service
kubectl scale deployment trading-service -n commsec --replicas=10

# Scale API gateway
kubectl scale deployment api-gateway -n commsec --replicas=10

# Verify scaling
kubectl get deployment -n commsec
```

### 2.3 Node Group Scaling
```bash
# Check current node group size
eksctl get nodegroup --cluster commsec-cluster --region us-east-1

# Scale web tier node group
eksctl scale nodegroup \
    --cluster=commsec-cluster \
    --name=web-tier-nodes \
    --nodes=6 \
    --nodes-min=3 \
    --nodes-max=10 \
    --region=us-east-1

# Scale app tier node group
eksctl scale nodegroup \
    --cluster=commsec-cluster \
    --name=app-tier-nodes \
    --nodes=6 \
    --nodes-min=3 \
    --nodes-max=10 \
    --region=us-east-1
```

---

## 3. ARC Zonal Shift Operations

### 3.1 Understanding Zonal Shift
AWS Application Recovery Controller (ARC) zonal shift allows you to:
- Shift traffic away from an impaired Availability Zone
- Minimize customer impact during AZ failures
- Avoid relying on control plane actions

### 3.2 Initiating Zonal Shift (Manual)
**Use when**: An Availability Zone is experiencing issues (gray failure, elevated latency, etc.)

```bash
#!/bin/bash
# initiate-zonal-shift.sh

# Parameters
IMPAIRED_AZ="us-east-1a"  # The AZ to shift away from
DURATION="1h"             # How long to maintain the shift

echo "=== Initiating Zonal Shift ==="
echo "Shifting traffic away from: $IMPAIRED_AZ"
echo "Duration: $DURATION"

# Get ALB ARN
WEB_ALB_ARN=$(aws elbv2 describe-load-balancers \
    --names commsec-web-alb \
    --query 'LoadBalancers[0].LoadBalancerArn' \
    --output text)

APP_ALB_ARN=$(aws elbv2 describe-load-balancers \
    --names commsec-app-alb \
    --query 'LoadBalancers[0].LoadBalancerArn' \
    --output text)

# Start zonal shift for Web ALB
echo "Starting zonal shift for Web ALB..."
aws arc-zonal-shift start-zonal-shift \
    --resource-identifier $WEB_ALB_ARN \
    --away-from $IMPAIRED_AZ \
    --expires-in $DURATION \
    --comment "Shifting away from $IMPAIRED_AZ due to detected issues"

# Start zonal shift for App ALB
echo "Starting zonal shift for App ALB..."
aws arc-zonal-shift start-zonal-shift \
    --resource-identifier $APP_ALB_ARN \
    --away-from $IMPAIRED_AZ \
    --expires-in $DURATION \
    --comment "Shifting away from $IMPAIRED_AZ due to detected issues"

echo "✓ Zonal shift initiated"
echo "Traffic is now being routed to: us-east-1b, us-east-1c"
```

### 3.3 Checking Zonal Shift Status
```bash
# List active zonal shifts
aws arc-zonal-shift list-zonal-shifts \
    --resource-identifier $WEB_ALB_ARN

# Get managed resources
aws arc-zonal-shift list-managed-resources
```

### 3.4 Canceling Zonal Shift
**Use when**: The AZ issue is resolved and you want to restore traffic

```bash
#!/bin/bash
# cancel-zonal-shift.sh

echo "=== Canceling Zonal Shift ==="

# Get active shift ID
SHIFT_ID=$(aws arc-zonal-shift list-zonal-shifts \
    --resource-identifier $WEB_ALB_ARN \
    --query 'items[0].zonalShiftId' \
    --output text)

if [ "$SHIFT_ID" != "None" ]; then
    # Cancel the shift
    aws arc-zonal-shift cancel-zonal-shift \
        --zonal-shift-id $SHIFT_ID
    echo "✓ Zonal shift canceled: $SHIFT_ID"
else
    echo "No active zonal shift found"
fi

# Repeat for App ALB
SHIFT_ID=$(aws arc-zonal-shift list-zonal-shifts \
    --resource-identifier $APP_ALB_ARN \
    --query 'items[0].zonalShiftId' \
    --output text)

if [ "$SHIFT_ID" != "None" ]; then
    aws arc-zonal-shift cancel-zonal-shift \
        --zonal-shift-id $SHIFT_ID
    echo "✓ App ALB zonal shift canceled: $SHIFT_ID"
fi
```

### 3.5 Zonal Shift Decision Tree
```
Is there elevated latency/errors from one AZ?
├── YES
│   ├── Are errors impacting >5% of requests?
│   │   ├── YES → Initiate zonal shift immediately
│   │   └── NO → Monitor for 5 minutes
│   │       ├── Improving → Continue monitoring
│   │       └── Worsening → Initiate zonal shift
│   └── Is there an AWS Health Dashboard event?
│       ├── YES → Initiate zonal shift, wait for AWS resolution
│       └── NO → Investigate application logs first
└── NO
    └── No action required
```

---

## 4. Incident Response

### 4.1 Severity Levels

| Level | Description | Response Time | Example |
|-------|-------------|---------------|---------|
| SEV-1 | Complete outage | < 5 min | Platform unreachable |
| SEV-2 | Major degradation | < 15 min | Trading not working |
| SEV-3 | Minor degradation | < 1 hour | Slow page loads |
| SEV-4 | Low impact | < 4 hours | Dashboard glitch |

### 4.2 SEV-1 Response Procedure
```bash
#!/bin/bash
# sev1-response.sh

echo "=== SEV-1 INCIDENT RESPONSE ==="
echo "Started: $(date -u)"

# Step 1: Verify the outage
echo -e "\n--- Step 1: Verify Outage ---"
curl -s -o /dev/null -w "%{http_code}" http://$ALB_DNS/api/health
kubectl get pods -n commsec

# Step 2: Check for obvious issues
echo -e "\n--- Step 2: Check Recent Changes ---"
kubectl get events -n commsec --sort-by='.lastTimestamp' | tail -10

# Step 3: Check node status
echo -e "\n--- Step 3: Node Status ---"
kubectl get nodes
kubectl describe nodes | grep -A5 "Conditions"

# Step 4: Check AZ health
echo -e "\n--- Step 4: AZ Health ---"
kubectl get pods -n commsec -o wide | awk '{print $7}' | sort | uniq -c

# Step 5: Immediate mitigation options
echo -e "\n--- Step 5: Mitigation Options ---"
echo "1. If single AZ issue: ./scripts/initiate-zonal-shift.sh"
echo "2. If pod crash: kubectl rollout restart deployment -n commsec"
echo "3. If DB issue: Check RDS in AWS Console"
echo "4. If network issue: Check VPC Flow Logs"
```

### 4.3 Post-Incident Review Template
```markdown
## Incident Report Template

### Summary
- **Incident ID**: INC-YYYY-MMDD-XXX
- **Severity**: SEV-1/2/3/4
- **Duration**: X hours Y minutes
- **Impact**: Brief description of customer impact

### Timeline
| Time (UTC) | Event |
|------------|-------|
| HH:MM | Issue detected |
| HH:MM | Incident declared |
| HH:MM | Mitigation started |
| HH:MM | Service restored |

### Root Cause
[Detailed explanation of what caused the incident]

### Mitigation
[What was done to resolve the incident]

### Action Items
| Item | Owner | Due Date |
|------|-------|----------|
| ... | ... | ... |
```

---

## 5. Database Operations

### 5.1 RDS Connection Check
```bash
# From a pod in the cluster
kubectl run pg-client --rm -it --restart=Never \
    --image=postgres:16-alpine \
    --env="PGPASSWORD=$DB_PASSWORD" \
    -n commsec \
    -- psql -h $RDS_ENDPOINT -U commsec_admin -d commsec -c "SELECT 1"
```

### 5.2 Database Performance Check
```sql
-- Check active connections
SELECT count(*) FROM pg_stat_activity;

-- Check slow queries
SELECT query, calls, mean_time, total_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;

-- Check table sizes
SELECT relname, pg_size_pretty(pg_total_relation_size(relid))
FROM pg_stat_user_tables
ORDER BY pg_total_relation_size(relid) DESC;
```

### 5.3 RDS Failover (Manual)
```bash
# Trigger RDS failover (for testing or emergency)
aws rds reboot-db-instance \
    --db-instance-identifier commsec-postgres \
    --force-failover

# Monitor failover
aws rds describe-events \
    --source-type db-instance \
    --source-identifier commsec-postgres
```

---

## 6. Monitoring & Alerting

### 6.1 CloudWatch Dashboard
Key metrics to monitor:
- **ALB**: RequestCount, TargetResponseTime, HTTPCode_Target_5XX_Count
- **EKS**: node_cpu_utilization, node_memory_utilization
- **RDS**: DatabaseConnections, ReadLatency, WriteLatency, FreeStorageSpace

### 6.2 Setting Up Alerts
```bash
# Create SNS topic for alerts
aws sns create-topic --name commsec-alerts

# Subscribe email
aws sns subscribe \
    --topic-arn arn:aws:sns:us-east-1:$AWS_ACCOUNT_ID:commsec-alerts \
    --protocol email \
    --notification-endpoint your-email@example.com

# Create CloudWatch alarm for high error rate
aws cloudwatch put-metric-alarm \
    --alarm-name "commsec-high-5xx-errors" \
    --alarm-description "High 5XX error rate on ALB" \
    --metric-name HTTPCode_Target_5XX_Count \
    --namespace AWS/ApplicationELB \
    --statistic Sum \
    --period 60 \
    --threshold 100 \
    --comparison-operator GreaterThanThreshold \
    --dimensions Name=LoadBalancer,Value=$WEB_ALB_NAME \
    --evaluation-periods 2 \
    --alarm-actions arn:aws:sns:us-east-1:$AWS_ACCOUNT_ID:commsec-alerts
```

### 6.3 Log Analysis
```bash
# View recent application logs
kubectl logs -n commsec -l app=trading-service --tail=100

# Search for errors in all pods
kubectl logs -n commsec -l tier=backend --tail=500 | grep -i error

# Stream logs in real-time
kubectl logs -n commsec -l app=api-gateway -f
```

---

## 7. Backup & Recovery

### 7.1 RDS Automated Backups
- **Retention**: 35 days
- **Window**: 03:00-04:00 UTC (off-peak)
- **Multi-AZ**: Automatic failover

### 7.2 Manual Snapshot
```bash
# Create manual snapshot
aws rds create-db-snapshot \
    --db-snapshot-identifier commsec-manual-$(date +%Y%m%d) \
    --db-instance-identifier commsec-postgres

# List snapshots
aws rds describe-db-snapshots \
    --db-instance-identifier commsec-postgres
```

### 7.3 Point-in-Time Recovery
```bash
# Restore to a point in time
aws rds restore-db-instance-to-point-in-time \
    --source-db-instance-identifier commsec-postgres \
    --target-db-instance-identifier commsec-postgres-restored \
    --restore-time "2026-01-30T12:00:00Z"
```

### 7.4 Application State Backup
```bash
# Export Kubernetes resources
kubectl get all -n commsec -o yaml > backup/k8s-resources-$(date +%Y%m%d).yaml
kubectl get configmap -n commsec -o yaml > backup/k8s-configmaps-$(date +%Y%m%d).yaml
kubectl get secret -n commsec -o yaml > backup/k8s-secrets-$(date +%Y%m%d).yaml

# Upload to S3
aws s3 cp backup/ s3://commsec-backups/kubernetes/$(date +%Y%m%d)/ --recursive
```

---

## 8. Troubleshooting Guide

### 8.1 Pod Won't Start
```bash
# Check pod events
kubectl describe pod <pod-name> -n commsec

# Common issues:
# - ImagePullBackOff: ECR authentication issue
# - CrashLoopBackOff: Application error
# - Pending: Insufficient resources

# For ImagePullBackOff:
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# For CrashLoopBackOff:
kubectl logs <pod-name> -n commsec --previous
```

### 8.2 High Latency
```bash
# Check pod resource usage
kubectl top pods -n commsec

# Check node resource usage
kubectl top nodes

# Check database connections
kubectl exec -it <trading-service-pod> -n commsec -- \
    curl localhost:8080/actuator/metrics/hikaricp.connections.active
```

### 8.3 Database Connection Issues
```bash
# Verify security group rules
aws ec2 describe-security-groups \
    --group-ids $RDS_SECURITY_GROUP_ID

# Test connectivity from a pod
kubectl run debug --rm -it --restart=Never \
    --image=busybox \
    -n commsec \
    -- nc -zv $RDS_ENDPOINT 5432
```

### 8.4 ALB Health Check Failures
```bash
# Check target group health
aws elbv2 describe-target-health \
    --target-group-arn $TARGET_GROUP_ARN

# Check pod readiness probe
kubectl get pods -n commsec -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.conditions[?(@.type=="Ready")].status}{"\n"}{end}'
```

---

## Contact Information

| Role | Contact |
|------|---------|
| On-Call Engineer | oncall@commsec.example.com |
| Platform Team | platform@commsec.example.com |
| AWS Support | Open case in AWS Console |
| Security Team | security@commsec.example.com |

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-31 | Platform Team | Initial version |
