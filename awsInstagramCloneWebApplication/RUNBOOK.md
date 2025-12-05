# Operations Runbook

## Daily Operations

### Health Check
Run this daily to verify system health.
```bash
# Check Nodes
kubectl get nodes

# Check Pods
kubectl get pods -A | grep -v Running

# Check RDS Status
aws rds describe-db-instances --db-instance-identifier instagram-clone-db --query 'DBInstances[0].DBInstanceStatus'
```

---

## Scaling Procedures

### Horizontal Scaling (Pods)
To scale the Feed Service during high traffic:
```bash
kubectl scale deployment feed-service --replicas=5
```

### Vertical Scaling (Nodes)
Update the Terraform configuration:
```hcl
# terraform/eks.tf
eks_managed_node_groups = {
  default = {
    min_size     = 2
    max_size     = 5
    desired_size = 3
    # ...
  }
}
```
Then run `terraform apply`.

---

## Backup and Recovery

### Database Backup
**Manual Snapshot**:
```bash
aws rds create-db-snapshot \
    --db-instance-identifier instagram-clone-db \
    --db-snapshot-identifier manual-backup-$(date +%Y%m%d)
```

### Restore
**Restore from Snapshot**:
```bash
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier instagram-clone-db-restored \
    --db-snapshot-identifier <snapshot-id>
```

---

## Security Operations

### Rotate Database Password
1.  Update password in AWS Secrets Manager.
2.  External Secrets Operator will sync the new password to Kubernetes `Secret`.
3.  Restart pods to pick up the new password.
    ```bash
    kubectl rollout restart deployment
    ```
