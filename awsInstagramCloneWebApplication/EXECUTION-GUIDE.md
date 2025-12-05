# Execution Guide: Kubernetes & GitOps

## Prerequisites
- `kubectl` installed and configured with **Cluster Admin** permissions.
- `helm` installed.
- `argocd` CLI installed (optional, but recommended).

## 1. Install Argo CD
Since the current user does not have permission to create namespaces, an admin must run the following:

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

## 2. Access Argo CD UI
By default, the Argo CD API server is not exposed with an external IP. To access the UI, use port forwarding:

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

The UI is now available at `https://localhost:8080`.

## 3. Login to Argo CD
The initial password for the `admin` account is auto-generated and stored as clear text in the field `password` in a secret named `argocd-initial-admin-secret` in your Argo CD namespace. You can retrieve this password using `kubectl`:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
```

Username: `admin`
Password: <output from above command>

## 4. Deploy Applications
We have prepared Argo CD Application manifests in `k8s/argocd/`. Apply them to the cluster:

```bash
kubectl apply -f k8s/argocd/application.yaml
```

## 5. Verify Deployment
Check the status of the applications in the Argo CD UI or using `kubectl`:

```bash
kubectl get applications -n argocd
```

## 6. Observability Setup
We use the `kube-prometheus-stack` to set up Prometheus and Grafana.

### Install kube-prometheus-stack
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring
```

### Access Grafana
Get the admin password:
```bash
kubectl get secret -n monitoring prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
```

Port-forward to access the UI:
```bash
kubectl port-forward svc/prometheus-grafana -n monitoring 3000:80
```
Access Grafana at `http://localhost:3000` (User: `admin`, Password: <from above>).

## 7. Logging Setup
We use Fluent Bit to collect logs and send them to OpenSearch (or Elasticsearch).

### Install Fluent Bit
```bash
helm repo add fluent https://fluent.github.io/helm-charts
helm repo update
kubectl create namespace logging
helm install fluent-bit fluent/fluent-bit -n logging
```

### Configure Output
By default, Fluent Bit logs to stdout. To configure it for OpenSearch, you would update the `values.yaml` to point to your OpenSearch endpoint.
