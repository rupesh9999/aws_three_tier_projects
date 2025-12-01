# OTA Travel Application - Troubleshooting Guide

## Overview
This guide provides solutions for common issues encountered during deployment and operation of the OTA Travel Application.

## Table of Contents
1. [Deployment Issues](#deployment-issues)
2. [Infrastructure Issues](#infrastructure-issues)
3. [Application Issues](#application-issues)
4. [Database Issues](#database-issues)
5. [Networking Issues](#networking-issues)
6. [Monitoring Issues](#monitoring-issues)
7. [CI/CD Issues](#cicd-issues)

---

## Deployment Issues

### Terraform Errors

#### Error: "Error acquiring the state lock"
**Symptoms:**
```
Error: Error acquiring the state lock
Lock Info: ...
```

**Solution:**
```bash
# Force unlock (use with caution)
terraform force-unlock <LOCK_ID>

# If DynamoDB lock is stuck
aws dynamodb delete-item \
    --table-name ota-travel-terraform-locks \
    --key '{"LockID": {"S": "ota-travel-terraform-state-<account>/production/terraform.tfstate"}}'
```

#### Error: "VPC limit exceeded"
**Solution:**
```bash
# Check current VPC count
aws ec2 describe-vpcs --query 'length(Vpcs)'

# Request limit increase or delete unused VPCs
aws ec2 delete-vpc --vpc-id vpc-xxxxxxxx
```

#### Error: "EKS cluster creation timeout"
**Symptoms:**
```
Error: waiting for EKS Cluster creation: timeout while waiting for state
```

**Solution:**
```bash
# Check EKS cluster status
aws eks describe-cluster --name ota-travel-production --query 'cluster.status'

# Check for VPC/subnet issues
aws eks describe-cluster --name ota-travel-production --query 'cluster.resourcesVpcConfig'

# Re-run terraform apply if status is CREATING
terraform apply
```

### Docker Build Errors

#### Error: "COPY failed: file not found"
**Solution:**
```bash
# Verify file exists
ls -la backend/auth-service/target/*.jar

# Rebuild with Maven first
cd backend
mvn clean package -DskipTests
```

#### Error: "no space left on device"
**Solution:**
```bash
# Clean up Docker resources
docker system prune -a -f

# Remove dangling images
docker image prune -f

# Check disk space
df -h
```

---

## Infrastructure Issues

### EKS Node Issues

#### Nodes not joining cluster
**Diagnosis:**
```bash
# Check node status
kubectl get nodes

# Check node group status
aws eks describe-nodegroup \
    --cluster-name ota-travel-production \
    --nodegroup-name ota-travel-production-general
```

**Solution:**
```bash
# Check security group rules allow communication
aws ec2 describe-security-groups --group-ids <sg-id>

# Verify IAM role
aws iam get-role --role-name ota-travel-production-eks-node-role

# Check CloudWatch logs
aws logs get-log-events \
    --log-group-name /aws/eks/ota-travel-production/cluster \
    --log-stream-name <stream-name>
```

#### Out of Memory (OOM) on nodes
**Diagnosis:**
```bash
# Check node resources
kubectl top nodes

# Check pod memory usage
kubectl top pods -n production --sort-by=memory
```

**Solution:**
```bash
# Scale up nodes
aws eks update-nodegroup-config \
    --cluster-name ota-travel-production \
    --nodegroup-name ota-travel-production-general \
    --scaling-config minSize=3,maxSize=15,desiredSize=5

# Or adjust resource requests/limits in deployments
kubectl edit deployment auth-service -n production
```

### RDS Issues

#### Connection refused
**Diagnosis:**
```bash
# Check RDS status
aws rds describe-db-instances \
    --db-instance-identifier ota-travel-production-db \
    --query 'DBInstances[0].DBInstanceStatus'

# Check security groups
aws rds describe-db-instances \
    --db-instance-identifier ota-travel-production-db \
    --query 'DBInstances[0].VpcSecurityGroups'
```

**Solution:**
```bash
# Verify security group allows inbound from EKS nodes
aws ec2 authorize-security-group-ingress \
    --group-id <rds-sg-id> \
    --protocol tcp \
    --port 5432 \
    --source-group <eks-node-sg-id>
```

#### Storage full
**Solution:**
```bash
# Increase storage (auto-scaling should handle this)
aws rds modify-db-instance \
    --db-instance-identifier ota-travel-production-db \
    --allocated-storage 200 \
    --apply-immediately
```

### ElastiCache Issues

#### Redis connection timeout
**Diagnosis:**
```bash
# Check cluster status
aws elasticache describe-replication-groups \
    --replication-group-id ota-travel-production-redis

# Test connection from a pod
kubectl run redis-test -n production --rm -it --image=redis:7-alpine -- \
    redis-cli -h <redis-endpoint> -p 6379 ping
```

**Solution:**
```bash
# Check security group
aws elasticache describe-cache-clusters \
    --cache-cluster-id ota-travel-production-redis-001 \
    --show-cache-node-info
```

---

## Application Issues

### Pod CrashLoopBackOff

**Diagnosis:**
```bash
# Check pod status
kubectl get pods -n production

# Check pod events
kubectl describe pod <pod-name> -n production

# Check container logs
kubectl logs <pod-name> -n production --previous
```

**Common Causes & Solutions:**

#### 1. Missing environment variables
```bash
# Check ConfigMap
kubectl describe configmap app-config -n production

# Check External Secrets sync
kubectl describe externalsecret database-credentials -n production
```

#### 2. Database migration failures
```bash
# Check Flyway logs
kubectl logs <auth-service-pod> -n production | grep -i flyway

# Manually check migrations
kubectl exec -it <pod> -n production -- \
    java -jar app.jar --spring.flyway.repair
```

#### 3. Health check failures
```bash
# Test health endpoint directly
kubectl exec -it <pod> -n production -- \
    curl -s localhost:8081/actuator/health
```

### Service Unavailable (503)

**Diagnosis:**
```bash
# Check pod readiness
kubectl get pods -n production -o wide

# Check service endpoints
kubectl get endpoints -n production

# Check ingress status
kubectl describe ingress ota-travel-ingress -n production
```

**Solution:**
```bash
# Scale up if needed
kubectl scale deployment auth-service -n production --replicas=3

# Check HPA status
kubectl describe hpa auth-service -n production
```

### High Latency

**Diagnosis:**
```bash
# Check application metrics
kubectl port-forward svc/prometheus-grafana -n monitoring 3000:80

# Check database connection pool
kubectl logs <pod> -n production | grep -i hikari

# Check network latency
kubectl exec -it <pod> -n production -- \
    curl -w "@/dev/stdin" -o /dev/null -s ${RDS_ENDPOINT}:5432 <<'EOF'
    time_namelookup:  %{time_namelookup}\n
    time_connect:     %{time_connect}\n
    time_appconnect:  %{time_appconnect}\n
EOF
```

**Solution:**
```bash
# Increase connection pool size in ConfigMap
kubectl edit configmap app-config -n production
# Add: SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "20"

# Restart pods
kubectl rollout restart deployment auth-service -n production
```

---

## Database Issues

### Slow Queries

**Diagnosis:**
```bash
# Enable slow query log (via RDS parameter group)
aws rds modify-db-parameter-group \
    --db-parameter-group-name ota-travel-production-pg \
    --parameters "ParameterName=log_min_duration_statement,ParameterValue=1000,ApplyMethod=immediate"

# Check Performance Insights
aws pi get-resource-metrics \
    --service-type RDS \
    --identifier db-<resource-id> \
    --metric-queries '[{"Metric":"db.load.avg"}]' \
    --start-time $(date -d '1 hour ago' -u +%Y-%m-%dT%H:%M:%SZ) \
    --end-time $(date -u +%Y-%m-%dT%H:%M:%SZ)
```

**Solution:**
```sql
-- Connect to database
psql -h <rds-endpoint> -U admin -d otatravel

-- Identify slow queries
SELECT query, calls, mean_time, total_time 
FROM pg_stat_statements 
ORDER BY total_time DESC 
LIMIT 10;

-- Add missing indexes
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_status ON bookings(status);
```

### Connection Pool Exhaustion

**Symptoms:**
```
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

**Solution:**
```yaml
# Update application.yml or ConfigMap
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 30000
      leak-detection-threshold: 60000
```

---

## Networking Issues

### ALB Target Group Unhealthy

**Diagnosis:**
```bash
# Check ALB target health
aws elbv2 describe-target-health \
    --target-group-arn <target-group-arn>

# Check ingress annotations
kubectl describe ingress ota-travel-ingress -n production
```

**Solution:**
```bash
# Verify health check path responds
kubectl exec -it <pod> -n production -- \
    curl -s localhost:8081/actuator/health

# Update ingress annotations if needed
kubectl annotate ingress ota-travel-ingress -n production \
    alb.ingress.kubernetes.io/healthcheck-path=/actuator/health \
    --overwrite
```

### DNS Resolution Failures

**Diagnosis:**
```bash
# Test DNS from pod
kubectl run dns-test -n production --rm -it --image=busybox:1.36 -- \
    nslookup auth-service.production.svc.cluster.local

# Check CoreDNS status
kubectl get pods -n kube-system -l k8s-app=kube-dns
kubectl logs -n kube-system -l k8s-app=kube-dns
```

**Solution:**
```bash
# Restart CoreDNS
kubectl rollout restart deployment coredns -n kube-system
```

### NetworkPolicy Blocking Traffic

**Diagnosis:**
```bash
# List network policies
kubectl get networkpolicy -n production

# Test connectivity
kubectl run netshoot -n production --rm -it --image=nicolaka/netshoot -- \
    curl -v auth-service.production.svc.cluster.local
```

**Solution:**
```bash
# Temporarily disable network policies for debugging
kubectl delete networkpolicy default-deny-all -n production

# Re-enable after fixing
kubectl apply -f infrastructure/kubernetes/ingress.yaml
```

---

## Monitoring Issues

### Prometheus Not Scraping Metrics

**Diagnosis:**
```bash
# Check ServiceMonitor
kubectl get servicemonitor -n monitoring

# Check Prometheus targets
kubectl port-forward svc/prometheus-kube-prometheus-prometheus -n monitoring 9090:9090
# Visit http://localhost:9090/targets
```

**Solution:**
```bash
# Ensure pods have correct annotations
kubectl annotate pod <pod-name> -n production \
    prometheus.io/scrape="true" \
    prometheus.io/port="8081" \
    prometheus.io/path="/actuator/prometheus"
```

### Grafana Dashboards Not Loading

**Solution:**
```bash
# Check Grafana logs
kubectl logs -n monitoring -l app.kubernetes.io/name=grafana

# Reset Grafana admin password
kubectl exec -it -n monitoring <grafana-pod> -- \
    grafana-cli admin reset-admin-password newpassword
```

### Missing Alerts

**Diagnosis:**
```bash
# Check PrometheusRule
kubectl get prometheusrule -n monitoring

# Check AlertManager config
kubectl get secret alertmanager-prometheus-kube-prometheus-alertmanager -n monitoring -o yaml
```

---

## CI/CD Issues

### Jenkins Pipeline Failures

#### Build stage failing
```bash
# Check Jenkins agent pod logs
kubectl logs -n jenkins <jenkins-agent-pod>

# Verify Maven cache PVC
kubectl get pvc maven-cache-pvc -n jenkins
```

#### ECR push failing
```bash
# Verify IAM role for Jenkins
aws sts assume-role \
    --role-arn arn:aws:iam::${AWS_ACCOUNT_ID}:role/jenkins-ecr-role \
    --role-session-name test

# Check ECR repository exists
aws ecr describe-repositories --repository-names ota-travel-production-auth-service
```

### ArgoCD Sync Failures

**Diagnosis:**
```bash
# Check application status
argocd app get ota-travel-auth-service

# Check sync status
argocd app sync ota-travel-auth-service --dry-run
```

**Solution:**
```bash
# Force sync
argocd app sync ota-travel-auth-service --force

# Refresh application
argocd app refresh ota-travel-auth-service

# Hard refresh
argocd app get ota-travel-auth-service --hard-refresh
```

---

## Emergency Procedures

### Rollback Deployment
```bash
# Using kubectl
kubectl rollout undo deployment auth-service -n production

# To specific revision
kubectl rollout undo deployment auth-service -n production --to-revision=2

# Using ArgoCD
argocd app rollback ota-travel-auth-service <revision>
```

### Scale Down All Services
```bash
# Emergency scale down
for deploy in $(kubectl get deploy -n production -o name); do
    kubectl scale $deploy -n production --replicas=0
done
```

### Database Backup
```bash
# Create manual RDS snapshot
aws rds create-db-snapshot \
    --db-instance-identifier ota-travel-production-db \
    --db-snapshot-identifier ota-travel-emergency-$(date +%Y%m%d%H%M%S)
```

---

## Support

For issues not covered in this guide:
1. Check application logs: `kubectl logs -f <pod> -n production`
2. Review CloudWatch Logs for infrastructure components
3. Contact platform team with relevant logs and error messages
