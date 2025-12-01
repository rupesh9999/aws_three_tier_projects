# Operational Runbook

## Table of Contents

1. [Service Restart Procedures](#1-service-restart-procedures)
2. [Scaling Procedures](#2-scaling-procedures)
3. [Incident Response](#3-incident-response)
4. [Database Operations](#4-database-operations)
5. [SQS Queue Management](#5-sqs-queue-management)
6. [Certificate Management](#6-certificate-management)
7. [Backup & Restore](#7-backup--restore)
8. [Monitoring & Alerting](#8-monitoring--alerting)

---

## 1. Service Restart Procedures

### 1.1 Rolling Restart (Zero Downtime)

```bash
# Restart a specific deployment with rolling update
kubectl rollout restart deployment/auth-service -n fintech-banking

# Monitor rollout status
kubectl rollout status deployment/auth-service -n fintech-banking

# Check pod status during rollout
watch kubectl get pods -n fintech-banking -l app=auth-service
```

### 1.2 Force Restart (Downtime Acceptable)

```bash
# Scale to zero, then back up
kubectl scale deployment auth-service --replicas=0 -n fintech-banking
sleep 10
kubectl scale deployment auth-service --replicas=3 -n fintech-banking
```

### 1.3 Restart All Services

```bash
# Rolling restart all deployments
kubectl rollout restart deployment -n fintech-banking

# Or specific services in order
for svc in auth-service account-service transaction-service payment-service notification-service; do
  echo "Restarting $svc..."
  kubectl rollout restart deployment/$svc -n fintech-banking
  kubectl rollout status deployment/$svc -n fintech-banking --timeout=120s
  sleep 30
done
```

### 1.4 Rollback Deployment

```bash
# View rollout history
kubectl rollout history deployment/auth-service -n fintech-banking

# Rollback to previous version
kubectl rollout undo deployment/auth-service -n fintech-banking

# Rollback to specific revision
kubectl rollout undo deployment/auth-service -n fintech-banking --to-revision=2
```

---

## 2. Scaling Procedures

### 2.1 Manual Horizontal Scaling

```bash
# Scale up during peak hours
kubectl scale deployment auth-service --replicas=5 -n fintech-banking
kubectl scale deployment transaction-service --replicas=5 -n fintech-banking

# Scale down during off-peak
kubectl scale deployment auth-service --replicas=2 -n fintech-banking
kubectl scale deployment transaction-service --replicas=2 -n fintech-banking
```

### 2.2 Update HPA Settings

```bash
# View current HPA
kubectl get hpa -n fintech-banking

# Edit HPA
kubectl edit hpa auth-service-hpa -n fintech-banking

# Or apply new settings
cat <<EOF | kubectl apply -f -
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: auth-service-hpa
  namespace: fintech-banking
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: auth-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
EOF
```

### 2.3 EKS Node Group Scaling

```bash
# Scale node group
aws eks update-nodegroup-config \
  --cluster-name fintech-banking-cluster \
  --nodegroup-name fintech-workers \
  --scaling-config minSize=3,maxSize=10,desiredSize=5

# Check node status
kubectl get nodes -o wide
```

### 2.4 RDS Vertical Scaling

```bash
# Modify RDS instance class (causes downtime)
aws rds modify-db-instance \
  --db-instance-identifier fintech-banking-db \
  --db-instance-class db.r6g.xlarge \
  --apply-immediately

# Monitor modification status
aws rds describe-db-instances \
  --db-instance-identifier fintech-banking-db \
  --query 'DBInstances[0].DBInstanceStatus'
```

---

## 3. Incident Response

### 3.1 High-Severity Incident Checklist

**P1 - Service Down**

1. **Acknowledge** - Update incident channel
2. **Assess** - Check monitoring dashboards
3. **Communicate** - Notify stakeholders
4. **Mitigate** - Apply immediate fixes
5. **Resolve** - Full restoration
6. **Post-mortem** - Document and learn

```bash
# Quick health check
./scripts/diagnose.sh

# Check all pods
kubectl get pods -n fintech-banking -o wide

# Check recent events
kubectl get events -n fintech-banking --sort-by='.lastTimestamp' | head -50

# Check ingress status
kubectl describe ingress fintech-ingress -n fintech-banking
```

### 3.2 Database Emergency Procedures

```bash
# If database is unresponsive, check RDS status
aws rds describe-db-instances --db-instance-identifier fintech-banking-db

# Reboot RDS (last resort, causes downtime)
aws rds reboot-db-instance --db-instance-identifier fintech-banking-db

# Failover to standby (Multi-AZ)
aws rds reboot-db-instance \
  --db-instance-identifier fintech-banking-db \
  --force-failover
```

### 3.3 API Gateway Emergency

```bash
# Check API Gateway status
aws apigateway get-rest-apis

# Flush API Gateway cache
aws apigateway flush-stage-cache \
  --rest-api-id <api-id> \
  --stage-name prod

# Enable/disable throttling
aws apigateway update-stage \
  --rest-api-id <api-id> \
  --stage-name prod \
  --patch-operations op=replace,path=/throttling/rateLimit,value=10000
```

### 3.4 Emergency Traffic Cutover

```bash
# Redirect traffic to maintenance page (CloudFront)
aws cloudfront update-distribution \
  --id <distribution-id> \
  --distribution-config file://maintenance-mode-config.json

# Or update DNS (Route 53)
aws route53 change-resource-record-sets \
  --hosted-zone-id <zone-id> \
  --change-batch file://maintenance-dns-change.json
```

---

## 4. Database Operations

### 4.1 Connection Management

```bash
# Check active connections
kubectl exec -it $(kubectl get pods -n fintech-banking -l app=auth-service -o jsonpath='{.items[0].metadata.name}') \
  -n fintech-banking -- \
  curl -s localhost:8080/actuator/metrics/hikaricp.connections.active | jq .

# View connections directly
psql -h <rds-endpoint> -U <username> -d fintech_banking_db -c \
  "SELECT pid, usename, application_name, client_addr, state, query_start 
   FROM pg_stat_activity 
   WHERE datname = 'fintech_banking_db' 
   ORDER BY query_start DESC;"

# Terminate long-running queries (use with caution)
psql -c "SELECT pg_terminate_backend(pid) 
         FROM pg_stat_activity 
         WHERE state = 'active' 
         AND query_start < now() - interval '5 minutes'
         AND pid <> pg_backend_pid();"
```

### 4.2 Table Maintenance

```bash
# Analyze tables for query optimization
psql -c "ANALYZE;"

# Vacuum specific table
psql -c "VACUUM (VERBOSE, ANALYZE) transactions;"

# Reindex (blocks writes)
psql -c "REINDEX TABLE transactions;"
```

### 4.3 Query Performance

```bash
# Find slow queries
psql -c "SELECT query, calls, mean_time, total_time 
         FROM pg_stat_statements 
         ORDER BY mean_time DESC 
         LIMIT 10;"

# Enable query logging temporarily
psql -c "ALTER SYSTEM SET log_min_duration_statement = 1000;"
psql -c "SELECT pg_reload_conf();"

# Disable after investigation
psql -c "ALTER SYSTEM RESET log_min_duration_statement;"
psql -c "SELECT pg_reload_conf();"
```

---

## 5. SQS Queue Management

### 5.1 Monitor Queue Health

```bash
# Check all queue attributes
aws sqs get-queue-attributes \
  --queue-url <queue-url> \
  --attribute-names All

# Key metrics to watch
# - ApproximateNumberOfMessages (backlog)
# - ApproximateAgeOfOldestMessage (latency)
# - ApproximateNumberOfMessagesNotVisible (in-flight)
```

### 5.2 Handle Message Backlog

```bash
# Scale up consumers
kubectl scale deployment notification-service --replicas=5 -n fintech-banking

# If backlog is too large, temporarily increase visibility timeout
aws sqs set-queue-attributes \
  --queue-url <queue-url> \
  --attributes VisibilityTimeout=300

# Monitor reduction
watch -n 10 "aws sqs get-queue-attributes --queue-url <queue-url> --attribute-names ApproximateNumberOfMessages --output text"
```

### 5.3 Purge Queue (Emergency)

```bash
# WARNING: This deletes all messages!
aws sqs purge-queue --queue-url <queue-url>

# Note: Takes up to 60 seconds to complete
```

### 5.4 Redrive DLQ Messages

```bash
# Count messages in DLQ
aws sqs get-queue-attributes \
  --queue-url <dlq-url> \
  --attribute-names ApproximateNumberOfMessages

# Redrive messages back to main queue
# Use AWS Console or custom script
cat > redrive.py << 'EOF'
import boto3
sqs = boto3.client('sqs')
dlq_url = '<dlq-url>'
main_url = '<main-queue-url>'

while True:
    response = sqs.receive_message(QueueUrl=dlq_url, MaxNumberOfMessages=10, WaitTimeSeconds=5)
    if 'Messages' not in response:
        break
    for msg in response['Messages']:
        sqs.send_message(QueueUrl=main_url, MessageBody=msg['Body'])
        sqs.delete_message(QueueUrl=dlq_url, ReceiptHandle=msg['ReceiptHandle'])
    print(f"Redrove {len(response['Messages'])} messages")
EOF

python redrive.py
```

---

## 6. Certificate Management

### 6.1 Check Certificate Expiry

```bash
# Check ACM certificates
aws acm list-certificates --query 'CertificateSummaryList[*].[DomainName,Status]'

aws acm describe-certificate \
  --certificate-arn <cert-arn> \
  --query 'Certificate.[DomainName,NotAfter,Status]'

# Set up CloudWatch alarm for expiry
aws cloudwatch put-metric-alarm \
  --alarm-name "cert-expiry-warning" \
  --comparison-operator LessThanThreshold \
  --evaluation-periods 1 \
  --metric-name DaysToExpiry \
  --namespace AWS/CertificateManager \
  --period 86400 \
  --threshold 30 \
  --statistic Minimum \
  --dimensions Name=CertificateArn,Value=<cert-arn>
```

### 6.2 Rotate Kubernetes Secrets

```bash
# Update secret
kubectl create secret generic jwt-secret \
  --from-literal=key='<new-secret-key>' \
  --dry-run=client -o yaml | kubectl apply -f -

# Restart services to pick up new secret
kubectl rollout restart deployment -n fintech-banking
```

---

## 7. Backup & Restore

### 7.1 RDS Backup Operations

```bash
# Create manual snapshot
aws rds create-db-snapshot \
  --db-instance-identifier fintech-banking-db \
  --db-snapshot-identifier fintech-backup-$(date +%Y%m%d%H%M)

# List snapshots
aws rds describe-db-snapshots \
  --db-instance-identifier fintech-banking-db \
  --query 'DBSnapshots[*].[DBSnapshotIdentifier,SnapshotCreateTime,Status]' \
  --output table

# Restore from snapshot (creates new instance)
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier fintech-banking-db-restored \
  --db-snapshot-identifier <snapshot-id> \
  --db-instance-class db.r6g.large \
  --vpc-security-group-ids <sg-id>
```

### 7.2 Point-in-Time Recovery

```bash
# Restore to specific point in time
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier fintech-banking-db \
  --target-db-instance-identifier fintech-banking-db-pit \
  --restore-time 2024-01-15T10:30:00Z \
  --db-instance-class db.r6g.large
```

### 7.3 Elasticsearch Snapshot

```bash
# Register snapshot repository
curl -X PUT "https://<es-endpoint>/_snapshot/s3_backup" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "s3",
    "settings": {
      "bucket": "fintech-es-backups",
      "region": "us-east-1"
    }
  }'

# Create snapshot
curl -X PUT "https://<es-endpoint>/_snapshot/s3_backup/snapshot_$(date +%Y%m%d)"

# Restore snapshot
curl -X POST "https://<es-endpoint>/_snapshot/s3_backup/snapshot_20240115/_restore"
```

---

## 8. Monitoring & Alerting

### 8.1 Key Metrics to Watch

| Metric | Warning Threshold | Critical Threshold |
|--------|-------------------|-------------------|
| CPU Usage | 70% | 90% |
| Memory Usage | 75% | 90% |
| API Latency (p99) | 500ms | 1000ms |
| Error Rate | 1% | 5% |
| Queue Depth | 1000 | 10000 |
| DB Connections | 80% max | 95% max |

### 8.2 Grafana Dashboard Access

```bash
# Port forward Grafana
kubectl port-forward svc/grafana 3000:3000 -n monitoring

# Access at http://localhost:3000
# Default credentials: admin / admin (change immediately)
```

### 8.3 Create Alert Rules

```yaml
# prometheus-rules.yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: fintech-alerts
  namespace: monitoring
spec:
  groups:
  - name: fintech
    rules:
    - alert: HighErrorRate
      expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) > 0.05
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: High error rate detected
        description: Error rate is {{ $value | humanizePercentage }}
    
    - alert: HighLatency
      expr: histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (le)) > 1
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: High latency detected
        description: p99 latency is {{ $value }}s
```

### 8.4 PagerDuty Integration

```bash
# Configure AlertManager for PagerDuty
kubectl edit configmap alertmanager-config -n monitoring

# Add receiver
# receivers:
# - name: 'pagerduty'
#   pagerduty_configs:
#   - service_key: '<pagerduty-integration-key>'
#     severity: '{{ .GroupLabels.severity }}'
```

---

## Emergency Contacts

| Role | Contact | Escalation |
|------|---------|------------|
| On-Call Engineer | PagerDuty rotation | Slack #fintech-oncall |
| Database Admin | dba-team@company.com | PagerDuty |
| Security Team | security@company.com | Immediate for breaches |
| AWS Support | AWS Console | Business/Enterprise support |

---

## Change Log

| Date | Change | Author |
|------|--------|--------|
| 2024-01-01 | Initial runbook | Platform Team |
| 2024-01-15 | Added SQS procedures | DevOps |
| 2024-02-01 | Updated scaling procedures | SRE Team |
