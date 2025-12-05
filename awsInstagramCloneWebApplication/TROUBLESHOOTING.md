# Troubleshooting Guide

## Table of Contents
1. [Deployment Issues](#deployment-issues)
2. [Infrastructure Issues](#infrastructure-issues)
3. [Application Issues](#application-issues)
4. [Database Issues](#database-issues)

---

## Deployment Issues

### Terraform Lock Error
**Symptom**: `Error acquiring the state lock`
**Solution**:
```bash
terraform force-unlock <LOCK_ID>
```

### EKS Cluster Creation Timeout
**Symptom**: Terraform times out waiting for EKS.
**Solution**:
Check if the VPC subnets have enough IP addresses and if the IAM role has correct permissions.
```bash
terraform apply # Re-run often fixes transient timeouts
```

---

## Infrastructure Issues

### EBS CSI Driver Issues
**Symptom**: PVCs stuck in `Pending`.
**Solution**:
Ensure the EBS CSI driver addon is installed and the IAM role is annotated correctly.
```bash
aws eks describe-addon --cluster-name instagram-clone-cluster --addon-name aws-ebs-csi-driver
```

---

## Application Issues

### Pod CrashLoopBackOff
**Diagnosis**:
```bash
kubectl logs <pod-name> --previous
```

**Common Causes**:
1.  **Database Connection**: Check `SPRING_DATASOURCE_URL`.
    *   Verify RDS endpoint is reachable.
    *   Verify Security Groups allow traffic from EKS nodes to RDS on port 5432.
2.  **Missing Secrets**:
    *   Check if `ExternalSecret` has synced.
    ```bash
    kubectl get externalsecret
    kubectl get secret db-credentials
    ```

### Service Unavailable (503)
**Diagnosis**:
Check if the service has active endpoints.
```bash
kubectl get endpoints <service-name>
```
If empty, check Readiness Probes.
```bash
kubectl describe pod <pod-name>
```

---

## Database Issues

### Connection Refused
**Solution**:
1.  Check RDS Security Group. It must allow Inbound TCP 5432 from the EKS Node Security Group.
2.  Check if the database exists.
    ```bash
    psql -h $RDS_ENDPOINT -U postgres -l
    ```

### Slow Queries
**Solution**:
Enable Performance Insights in AWS Console or check `pg_stat_statements`.
