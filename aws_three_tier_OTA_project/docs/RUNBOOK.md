# OTA Travel Application - Operations Runbook

## Overview
This runbook provides standard operating procedures for the OTA Travel Application production environment.

## Table of Contents
1. [Daily Operations](#daily-operations)
2. [Incident Response](#incident-response)
3. [Scaling Procedures](#scaling-procedures)
4. [Maintenance Windows](#maintenance-windows)
5. [Backup and Recovery](#backup-and-recovery)
6. [Security Operations](#security-operations)
7. [Cost Optimization](#cost-optimization)

---

## Daily Operations

### Morning Health Check (9:00 AM)

```bash
#!/bin/bash
# Daily health check script

echo "=== OTA Travel Daily Health Check ==="
echo "Date: $(date)"

# Check cluster health
echo -e "\n--- Kubernetes Cluster Status ---"
kubectl get nodes
kubectl top nodes

# Check all pods
echo -e "\n--- Pod Status (production namespace) ---"
kubectl get pods -n production -o wide

# Check resource usage
echo -e "\n--- Resource Usage ---"
kubectl top pods -n production --sort-by=memory

# Check for any alerts
echo -e "\n--- Active Alerts ---"
kubectl exec -n monitoring $(kubectl get pods -n monitoring -l app.kubernetes.io/name=alertmanager -o jsonpath='{.items[0].metadata.name}') -- \
    wget -qO- http://localhost:9093/api/v2/alerts | jq '.[] | select(.status.state=="active") | .labels.alertname'

# Check RDS status
echo -e "\n--- RDS Status ---"
aws rds describe-db-instances \
    --db-instance-identifier ota-travel-production-db \
    --query 'DBInstances[0].{Status:DBInstanceStatus,Storage:AllocatedStorage,CPU:DBInstanceClass}'

# Check recent errors in logs
echo -e "\n--- Recent Errors (last hour) ---"
kubectl logs -n production -l app=auth-service --since=1h | grep -i error | tail -20
```

### Key Metrics to Monitor

| Metric | Normal Range | Warning | Critical |
|--------|-------------|---------|----------|
| CPU Utilization | 30-60% | >70% | >85% |
| Memory Usage | 40-70% | >80% | >90% |
| Request Latency (p99) | <500ms | >1s | >2s |
| Error Rate | <0.1% | >1% | >5% |
| Pod Restarts | 0-2/day | >5/day | >10/day |
| Database Connections | 20-50 | >75% pool | >90% pool |

---

## Incident Response

### Severity Levels

| Level | Description | Response Time | Escalation |
|-------|-------------|---------------|------------|
| SEV1 | Complete outage | 5 min | Immediate page |
| SEV2 | Major feature degraded | 15 min | Page + Slack |
| SEV3 | Minor feature affected | 1 hour | Slack |
| SEV4 | Low impact | 4 hours | Ticket |

### SEV1: Complete Outage Response

#### Step 1: Acknowledge and Assess (0-5 min)
```bash
# Check overall cluster status
kubectl get nodes
kubectl get pods -n production

# Check ingress
kubectl describe ingress ota-travel-ingress -n production

# Check ALB health
aws elbv2 describe-target-health \
    --target-group-arn <target-group-arn>
```

#### Step 2: Identify Root Cause (5-15 min)
```bash
# Check recent deployments
kubectl rollout history deployment -n production

# Check recent events
kubectl get events -n production --sort-by='.lastTimestamp' | tail -30

# Check application logs
for svc in auth-service search-service booking-service payment-service cart-service; do
    echo "=== $svc ==="
    kubectl logs -n production -l app=$svc --tail=50 | grep -i "error\|exception"
done
```

#### Step 3: Mitigate (15-30 min)
```bash
# Rollback last deployment if recent change
kubectl rollout undo deployment auth-service -n production

# Scale up pods if load issue
kubectl scale deployment auth-service -n production --replicas=5

# Restart unhealthy pods
kubectl delete pod <unhealthy-pod> -n production
```

#### Step 4: Communicate
```bash
# Post to incident channel
echo "INCIDENT: [SEV1] OTA Travel - Production Outage
Status: Investigating
Impact: All users affected
Time: $(date)
Lead: <your-name>" | post-to-slack-channel
```

### SEV2: Service Degradation Response

```bash
# Identify affected service
kubectl get pods -n production | grep -v Running

# Check service-specific logs
kubectl logs -n production -l app=<affected-service> --tail=100

# Check dependencies
kubectl exec -n production <pod> -- curl -s localhost:8081/actuator/health

# Scale affected service
kubectl scale deployment <service> -n production --replicas=4
```

---

## Scaling Procedures

### Horizontal Scaling (Pods)

#### Manual Scaling
```bash
# Scale specific service
kubectl scale deployment auth-service -n production --replicas=5

# Scale all services
for deploy in auth-service search-service booking-service payment-service cart-service frontend; do
    kubectl scale deployment $deploy -n production --replicas=3
done
```

#### HPA Adjustment
```bash
# Update HPA limits
kubectl patch hpa auth-service -n production \
    --patch '{"spec":{"minReplicas":3,"maxReplicas":15}}'

# Check HPA status
kubectl get hpa -n production
```

### Vertical Scaling (Node Groups)

```bash
# Scale EKS node group
aws eks update-nodegroup-config \
    --cluster-name ota-travel-production \
    --nodegroup-name ota-travel-production-general \
    --scaling-config minSize=3,maxSize=15,desiredSize=6

# Monitor scaling
watch -n 10 'kubectl get nodes'
```

### Database Scaling

#### RDS Instance Resize
```bash
# Schedule resize during maintenance window
aws rds modify-db-instance \
    --db-instance-identifier ota-travel-production-db \
    --db-instance-class db.t3.large \
    --apply-immediately

# Monitor status
watch -n 30 'aws rds describe-db-instances \
    --db-instance-identifier ota-travel-production-db \
    --query "DBInstances[0].DBInstanceStatus"'
```

#### ElastiCache Scaling
```bash
# Scale Redis cluster
aws elasticache modify-replication-group \
    --replication-group-id ota-travel-production-redis \
    --cache-node-type cache.t3.large \
    --apply-immediately
```

### Expected Scaling Times
| Operation | Expected Duration |
|-----------|-------------------|
| Pod scale up | 30-60 seconds |
| EKS node scale up | 3-5 minutes |
| RDS resize | 10-30 minutes |
| ElastiCache resize | 5-15 minutes |

---

## Maintenance Windows

### Weekly Maintenance (Sunday 2:00-4:00 AM UTC)

```bash
#!/bin/bash
# Weekly maintenance script

echo "Starting weekly maintenance..."

# 1. Update node images (rolling)
aws eks update-nodegroup-config \
    --cluster-name ota-travel-production \
    --nodegroup-name ota-travel-production-general \
    --update-config maxUnavailable=1

# 2. Clean up old ReplicaSets
kubectl get rs -n production -o json | jq -r '.items[] | select(.spec.replicas == 0) | .metadata.name' | xargs -r kubectl delete rs -n production

# 3. Prune old Docker images from nodes
kubectl apply -f - <<EOF
apiVersion: batch/v1
kind: Job
metadata:
  name: docker-prune-$(date +%Y%m%d)
  namespace: kube-system
spec:
  template:
    spec:
      containers:
      - name: docker-prune
        image: docker:24-cli
        command: ["docker", "system", "prune", "-af"]
        volumeMounts:
        - name: docker-socket
          mountPath: /var/run/docker.sock
      volumes:
      - name: docker-socket
        hostPath:
          path: /var/run/docker.sock
      restartPolicy: Never
  backoffLimit: 1
EOF

# 4. Vacuum PostgreSQL
kubectl run pg-maintenance -n production --rm -it --image=postgres:16-alpine -- \
    psql -h $RDS_ENDPOINT -U admin -d otatravel -c "VACUUM ANALYZE;"

# 5. Clear expired Redis keys
kubectl run redis-maintenance -n production --rm -it --image=redis:7-alpine -- \
    redis-cli -h $REDIS_ENDPOINT BGSAVE

echo "Weekly maintenance completed"
```

### Monthly Maintenance (First Sunday of Month)

```bash
#!/bin/bash
# Monthly maintenance script

# 1. Rotate secrets
./scripts/rotate-secrets.sh

# 2. Update Helm releases
helm repo update
helm upgrade prometheus prometheus-community/kube-prometheus-stack \
    -n monitoring --reuse-values

# 3. Review and clean up CloudWatch logs
aws logs describe-log-groups --query 'logGroups[?starts_with(logGroupName, `/aws/eks/ota-travel`)]' | \
    jq -r '.[].logGroupName' | while read log_group; do
        aws logs delete-log-group --log-group-name "$log_group" 2>/dev/null || true
    done

# 4. Analyze cost report
aws ce get-cost-and-usage \
    --time-period Start=$(date -d '30 days ago' +%Y-%m-%d),End=$(date +%Y-%m-%d) \
    --granularity MONTHLY \
    --metrics BlendedCost \
    --group-by Type=TAG,Key=Project
```

---

## Backup and Recovery

### Automated Backups

| Component | Backup Frequency | Retention | Location |
|-----------|-----------------|-----------|----------|
| RDS | Daily snapshot | 30 days | AWS RDS |
| Redis | Hourly | 24 hours | ElastiCache |
| S3 Assets | Versioning | 90 days | S3 |
| ConfigMaps | GitOps | Unlimited | Git |

### Manual Database Backup

```bash
# Create manual snapshot
aws rds create-db-snapshot \
    --db-instance-identifier ota-travel-production-db \
    --db-snapshot-identifier manual-backup-$(date +%Y%m%d%H%M%S)

# Export to S3 for long-term storage
aws rds start-export-task \
    --export-task-identifier export-$(date +%Y%m%d) \
    --source-arn arn:aws:rds:us-east-1:${AWS_ACCOUNT_ID}:snapshot:manual-backup-xxx \
    --s3-bucket-name ota-travel-backups \
    --iam-role-arn arn:aws:iam::${AWS_ACCOUNT_ID}:role/rds-export-role \
    --kms-key-id arn:aws:kms:us-east-1:${AWS_ACCOUNT_ID}:key/xxx
```

### Database Recovery

#### Point-in-Time Recovery
```bash
# Restore to specific point in time
aws rds restore-db-instance-to-point-in-time \
    --source-db-instance-identifier ota-travel-production-db \
    --target-db-instance-identifier ota-travel-production-db-restored \
    --restore-time $(date -d '2 hours ago' -u +%Y-%m-%dT%H:%M:%SZ)

# Wait for restore
aws rds wait db-instance-available \
    --db-instance-identifier ota-travel-production-db-restored

# Update application to use restored database
kubectl set env deployment -n production --all \
    DB_HOST=<new-endpoint>
```

#### Full Recovery from Snapshot
```bash
# List available snapshots
aws rds describe-db-snapshots \
    --db-instance-identifier ota-travel-production-db \
    --query 'DBSnapshots[*].[DBSnapshotIdentifier,SnapshotCreateTime]' \
    --output table

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier ota-travel-production-db-new \
    --db-snapshot-identifier <snapshot-id> \
    --db-instance-class db.t3.medium \
    --vpc-security-group-ids <sg-id>
```

### Application State Recovery

```bash
# Restore from GitOps repository
cd ota-travel-gitops
git log --oneline -20  # Find desired state
git checkout <commit-hash>

# Force sync with ArgoCD
argocd app sync ota-travel-auth-service --force
argocd app sync ota-travel-search-service --force
# ... repeat for all services
```

---

## Security Operations

### Certificate Rotation

```bash
# Check certificate expiration
aws acm describe-certificate \
    --certificate-arn <cert-arn> \
    --query 'Certificate.NotAfter'

# For ACM-managed certs, renewal is automatic
# For custom certs, request new one
aws acm request-certificate \
    --domain-name ota-travel.example.com \
    --subject-alternative-names '*.ota-travel.example.com' \
    --validation-method DNS
```

### Secret Rotation

```bash
#!/bin/bash
# Rotate JWT secret

# 1. Generate new secret
NEW_SECRET=$(openssl rand -base64 32)

# 2. Update in Secrets Manager
aws secretsmanager update-secret \
    --secret-id ota-travel/production/jwt \
    --secret-string "{\"secret\":\"${NEW_SECRET}\"}"

# 3. Trigger External Secrets refresh
kubectl annotate externalsecret jwt-secrets -n production \
    force-sync=$(date +%s) --overwrite

# 4. Restart services to pick up new secret
kubectl rollout restart deployment -n production

# 5. Verify
kubectl logs -n production -l app=auth-service --tail=10 | grep -i "jwt\|token"
```

### Security Audit

```bash
# Run kube-bench for CIS benchmarks
kubectl apply -f https://raw.githubusercontent.com/aquasecurity/kube-bench/main/job.yaml

# Get results
kubectl logs job/kube-bench

# Scan images for vulnerabilities
aws ecr start-image-scan \
    --repository-name ota-travel-production-auth-service \
    --image-id imageTag=latest

# Get scan results
aws ecr describe-image-scan-findings \
    --repository-name ota-travel-production-auth-service \
    --image-id imageTag=latest
```

---

## Cost Optimization

### Monthly Cost Review

```bash
# Get cost breakdown by service
aws ce get-cost-and-usage \
    --time-period Start=$(date -d 'first day of last month' +%Y-%m-%d),End=$(date -d 'first day of this month' +%Y-%m-%d) \
    --granularity MONTHLY \
    --metrics BlendedCost \
    --group-by Type=SERVICE \
    --filter '{"Tags":{"Key":"Project","Values":["ota-travel"]}}'
```

### Right-sizing Recommendations

```bash
# Check for over-provisioned pods
kubectl top pods -n production --sort-by=cpu
kubectl top pods -n production --sort-by=memory

# Compare with requests
kubectl get pods -n production -o json | jq '.items[] | {name:.metadata.name, cpu:.spec.containers[0].resources.requests.cpu, memory:.spec.containers[0].resources.requests.memory}'

# Check RDS recommendations
aws compute-optimizer get-ec2-instance-recommendations \
    --instance-arns arn:aws:rds:us-east-1:${AWS_ACCOUNT_ID}:db:ota-travel-production-db
```

### Cost-Saving Actions

| Action | Potential Savings | Risk |
|--------|------------------|------|
| Reserved Instances (RDS) | 30-60% | Commitment |
| Spot Instances (EKS) | 60-80% | Interruption |
| Right-size instances | 10-30% | Performance |
| Delete unused resources | Variable | None |

---

## Contact Information

| Role | Contact | Escalation |
|------|---------|------------|
| On-Call Engineer | PagerDuty rotation | #ota-platform |
| Platform Lead | platform-lead@company.com | Direct |
| Security Team | security@company.com | #security-incidents |
| DBA | dba-team@company.com | #database-support |

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2024-01-15 | Platform Team | Initial version |
