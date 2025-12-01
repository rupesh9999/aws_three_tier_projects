# **Perfect OTA Travel Application - Production-Ready Prompt**

Design and generate a fully modern, secure, multi-tier **Online Travel Agency Web Application** using the **latest stable versions** of all technologies. The architecture must follow a clean **three-tier model** (Presentation → Application/Business Logic → Data) with complete **integration, messaging, DevOps, CI/CD, IaC, observability, and cloud infrastructure layers**.

**CRITICAL: Ensure all configurations are production-ready, tested, and deployment-validated. No deprecated APIs, no missing IAM permissions, no secret path mismatches, no environment variable ordering issues.**

---

## **🎯 Core Principles**

1. **Version Alignment**: All Kubernetes API versions, Docker base images, Helm charts, and dependencies must use latest stable releases
2. **Zero Configuration Drift**: Terraform-created resources must match exactly with Kubernetes manifest references (secret paths, endpoints, ports)
3. **IAM Completeness**: All IAM roles must include comprehensive policies from official AWS documentation, not partial permissions
4. **Secret Management Consistency**: Single source of truth for secrets - either AWS Secrets Manager OR manual creation, with clear documentation
5. **Database Connection Validation**: Environment variables must be ordered correctly for shell expansion in Kubernetes
6. **Health Check Accuracy**: All health check paths must match actual application endpoints
7. **Port Consistency**: Container ports, service ports, and application.properties ports must align perfectly
8. **No Deprecated Components**: Use current External Secrets Operator (v1), Ingress API (networking.k8s.io/v1), latest Spring Boot, React, etc.

---

## **✅ 1. Presentation Tier (Frontend)**

### **Technology Stack**
- **React 18+** with TypeScript
- **Vite** for build tooling (not Create React App)
- **TailwindCSS** or **MUI** for styling
- **React Router v6+** for routing
- **Axios** or **React Query** for API calls
- **Nginx 1.27-alpine** for serving

### **Features**
- User authentication (login, register, JWT storage)
- Travel search (flights, hotels, trains, buses)
- Cart/itinerary management
- Traveler details & add-ons (seats, meals, baggage)
- Checkout & payment integration
- Bookings dashboard (upcoming, past, cancellations)
- Help & support pages

### **Critical Frontend Requirements**

1. **Nginx Configuration**
   - Port 8080 for non-root user (not port 80)
   - Proper permissions for nginx.pid
   - Remove `user nginx;` directive for security
   - Health check endpoint at `/health` or root `/`

2. **Dockerfile Best Practices**
   ```dockerfile
   # Multi-stage build
   # Stage 1: Build with node:20-alpine
   # Stage 2: Serve with nginx:1.27-alpine
   # Create non-root user (appuser)
   # Fix nginx.pid permissions: touch, chown, sed commands
   # EXPOSE 8080 (not 80)
   ```

3. **Kubernetes Manifest Accuracy**
   - containerPort: 8080
   - Service targetPort: 8080
   - Service port: 80 (external facing)
   - livenessProbe and readinessProbe path: `/`
   - No host-based routing unless domain is configured

### **Deliverables**
- `frontend/` folder with src/, components/, services/, pages/
- `frontend/Dockerfile` (multi-stage, optimized)
- `frontend/nginx.conf` (port 8080, proper config)
- `infrastructure/kubernetes/deployments/frontend.yaml`
- Environment variable handling for API endpoints

---

## **✅ 2. Integration & Messaging Tier**

### **AWS API Gateway**
- REST API (not HTTP API for this use case)
- CORS configuration
- Request validation
- Throttling: 1000 requests/sec burst, 500 steady
- Stage variables for environment management

### **AWS SQS**
- Standard queues for async processing
- Dead Letter Queues (DLQ) for failed messages
- Queue policies for service access
- Message retention: 4 days default

### **Critical Integration Requirements**

1. **API Gateway must be provisioned by Terraform with**:
   - VPC Link for private EKS integration OR
   - Public ALB integration with proper security groups
   - IAM roles for CloudWatch logging
   - Deployment stages (dev, staging, production)

2. **SQS Integration**:
   - IAM roles for services to publish/consume
   - Environment variables in services for queue URLs
   - Error handling and retry logic in code

### **Deliverables**
- `infrastructure/terraform/api-gateway-messaging.tf`
- SQS queue definitions with DLQ
- API Gateway OpenAPI/Swagger definition
- Postman collection for testing

---

## **✅ 3. Application / Business Logic Tier (Backend)**

### **Technology Stack**
- **Java 17** or **Java 21** (LTS)
- **Spring Boot 3.2+** (latest stable)
- **Spring Data JPA**
- **Spring Security** with JWT
- **Flyway** for database migrations
- **Maven** (multi-module structure)

### **Microservices Architecture**

1. **auth-service** (Port 8080)
   - User registration, login
   - JWT token generation/validation
   - Password encryption (BCrypt)
   - Database: PostgreSQL

2. **search-service** (Port 8080)
   - Search flights, hotels, trains, buses
   - Integration with external APIs or mock data
   - Database: PostgreSQL + OpenSearch for indexing

3. **cart-service** (Port 8080)
   - Add/remove items from cart
   - Session management
   - Database: **Redis only** (no PostgreSQL)
   - **CRITICAL**: Disable Spring Data JPA auto-configuration

4. **booking-service** (Port 8080)
   - Create/confirm bookings
   - Ticket generation
   - Database: PostgreSQL

5. **payment-service** (Port 8080)
   - Payment processing (use mock provider or free alternative)
   - Refund handling
   - Database: PostgreSQL

6. **common module**
   - Shared DTOs, exceptions, utilities
   - JWT utilities
   - No database dependencies

### **Critical Backend Requirements**

1. **Database Connection Configuration**
   ```yaml
   # Environment variables MUST be in this order:
   - name: DB_HOST
   - name: DB_PORT  # Or include port in DB_HOST
   - name: DB_NAME
   - name: DB_USERNAME
   - name: DB_PASSWORD
   - name: SPRING_DATASOURCE_URL
     value: "jdbc:postgresql://$(DB_HOST)/$(DB_NAME)"
   ```

2. **application.properties Structure**
   ```properties
   server.port=8080
   spring.application.name=service-name
   spring.datasource.url=${SPRING_DATASOURCE_URL}
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   spring.jpa.hibernate.ddl-auto=validate
   spring.flyway.enabled=true
   management.endpoints.web.exposure.include=health,metrics
   ```

3. **Cart Service Specific**
   ```properties
   spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
   spring.jpa.enabled=false
   spring.flyway.enabled=false
   ```

4. **Health Check Endpoints**
   - All services: `/actuator/health`
   - Frontend: `/` or `/health`

5. **Dockerfile Best Practices**
   ```dockerfile
   # Use eclipse-temurin:17-jre-alpine or 21-jre-alpine
   # Multi-stage: maven build → JRE runtime
   # Non-root user
   # EXPOSE 8080
   ```

### **Deliverables**
- `backend/pom.xml` (parent POM)
- `backend/common/` (shared module)
- `backend/{service-name}/` for each microservice
- `backend/{service-name}/src/main/resources/application.properties`
- `backend/{service-name}/src/main/resources/db/migration/` (Flyway scripts)
- `backend/{service-name}/Dockerfile`
- `infrastructure/kubernetes/deployments/{service-name}.yaml`
- Unit tests for each service

---

## **✅ 4. Data Tier**

### **Database Setup**

1. **RDS PostgreSQL Instances** (Terraform-managed)
   - `ota-production-auth` → auth-service
   - `ota-production-booking` → booking-service
   - `ota-production-payment` → payment-service
   - `ota-production-search` → search-service
   - Master username: `postgres`
   - Master password: Stored in AWS Secrets Manager
   - **CRITICAL**: RDS password must match Secrets Manager value

2. **ElastiCache Redis** (Terraform-managed)
   - Single cluster for cart-service
   - Connection string in Secrets Manager

3. **Amazon OpenSearch** (Terraform-managed)
   - For search-service indexing
   - VPC endpoint with security group access
   - Credentials in Secrets Manager

### **Critical Data Requirements**

1. **Secret Path Consistency**
   - Terraform creates: `ota/production/database`
   - Kubernetes ExternalSecret references: `ota/production/database`
   - **NO MISMATCH** between Terraform and K8s manifests

2. **Database Secret Structure**
   ```json
   {
     "auth_host": "ota-production-auth.xxx.us-east-2.rds.amazonaws.com:5432",
     "booking_host": "ota-production-booking.xxx.us-east-2.rds.amazonaws.com:5432",
     "payment_host": "ota-production-payment.xxx.us-east-2.rds.amazonaws.com:5432",
     "search_host": "ota-production-search.xxx.us-east-2.rds.amazonaws.com:5432",
     "username": "postgres",
     "password": "SecurePassword123!"
   }
   ```

3. **Service-Specific ExternalSecrets**
   - Create separate ExternalSecret for each service
   - Map to individual K8s secrets: auth-database-credentials, booking-database-credentials, etc.

4. **Flyway Migrations**
   - Version-numbered SQL files: V1__initial_schema.sql
   - Idempotent scripts
   - Run automatically on service startup

### **Deliverables**
- `infrastructure/terraform/rds.tf`
- `infrastructure/terraform/elasticache-opensearch.tf`
- `backend/{service}/src/main/resources/db/migration/V*.sql`
- `infrastructure/kubernetes/external-secrets.yaml`
- `scripts/init-db.sql` (optional manual setup script)

---

## **✅ 5. Infrastructure & DevOps Layer**

### **5.1 AWS Infrastructure (Terraform)**

**VPC & Networking**
- 3 public subnets, 3 private subnets across 3 AZs
- NAT Gateway per AZ (or single for cost savings)
- Internet Gateway
- Route tables with proper associations
- Security groups for EKS, RDS, Redis, OpenSearch, ALB

**EKS Cluster**
- Kubernetes version: 1.32 or latest stable
- Node groups: t3.medium or t3.large, 2-5 nodes
- OIDC provider enabled for IRSA
- EBS CSI driver add-on enabled
- VPC CNI, CoreDNS, kube-proxy add-ons
- Cluster endpoint: public or private based on requirements

**IAM Roles (CRITICAL - Complete Policies)**

1. **AWS Load Balancer Controller Role**
   ```hcl
   # Use official policy from:
   # https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.0/docs/install/iam_policy.json
   # Must include: EC2, ELB, ACM, WAF, Shield, Cognito permissions
   ```

2. **External Secrets Role**
   ```hcl
   # Permissions: secretsmanager:GetSecretValue, secretsmanager:DescribeSecret
   # Trust policy: OIDC provider with proper conditions
   ```

3. **EBS CSI Driver Role**
   ```hcl
   # Use AWS managed policy: AmazonEBSCSIDriverPolicy
   ```

**RDS Configuration**
- Multi-AZ for production
- Automated backups enabled
- Deletion protection: true
- Storage encrypted
- Parameter group optimizations

**S3 + CloudFront**
- S3 bucket for frontend static assets
- CloudFront distribution with OAI
- HTTPS enabled (ACM certificate or CloudFront default)
- Cache behaviors optimized

**API Gateway + SQS**
- As described in Section 2

**ECR Repositories**
- One per service: frontend, auth-service, booking-service, cart-service, payment-service, search-service
- Image scanning enabled
- Lifecycle policies to remove old images

### **5.2 Kubernetes Manifests**

**Namespace**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: production
```

**ConfigMaps**
- Global config: `app-config`
- Service-specific: `{service}-config`

**Secrets (via External Secrets Operator)**
- API version: `external-secrets.io/v1`
- SecretStore with IRSA service account
- ExternalSecrets for each credential type

**Deployments**
- Replicas: 1 for testing, 3+ for production
- Resource requests/limits defined
- liveness and readiness probes
- Environment variables properly ordered
- Image pull policy: Always or IfNotPresent

**Services**
- Type: ClusterIP for internal services
- Type: LoadBalancer for testing (if ALB doesn't work)
- Proper port mappings

**Ingress** (networking.k8s.io/v1)
- Ingress class: alb
- Scheme: internet-facing
- Target type: ip
- Listen ports: HTTP:80 (no HTTPS for testing unless ACM cert exists)
- Health check path: `/` for frontend, `/actuator/health` for backend
- Path-based routing: `/api/v1/{service}` → service

**HorizontalPodAutoscaler** (Optional)
- Target CPU: 70%
- Min replicas: 2, Max replicas: 10
- **NOTE**: Disable if cluster resources are limited

### **5.3 Cluster Add-ons Installation**

**AWS Load Balancer Controller**
```bash
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=${EKS_CLUSTER_NAME} \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller
```

**External Secrets Operator**
```bash
helm install external-secrets external-secrets/external-secrets \
    -n external-secrets --create-namespace
```

**Metrics Server**
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### **5.4 CI/CD Pipeline**

**Jenkins Pipeline**
- Build stage: Maven/npm build
- Test stage: Unit + integration tests
- Docker stage: Build multi-arch images
- Push stage: Tag and push to ECR
- Trigger: ArgoCD sync

**ArgoCD Applications**
- GitOps: Monitor `infrastructure/kubernetes/` folder
- Auto-sync enabled for dev, manual for production
- Health checks for all resources

### **5.5 Security Best Practices**

1. No secrets in Git (use AWS Secrets Manager)
2. IAM roles with least privilege
3. Network policies for pod-to-pod communication
4. Security groups: restrict to necessary ports only
5. Encrypted data at rest (RDS, S3, EBS)
6. HTTPS/TLS for all external communication

### **Deliverables**
- `infrastructure/terraform/*.tf` (15+ files)
- `infrastructure/kubernetes/*.yaml` (namespaces, configmaps, external-secrets, ingress)
- `infrastructure/kubernetes/deployments/*.yaml` (6 deployment files)
- `cicd/jenkins/Jenkinsfile`
- `cicd/argocd/applications.yaml`

---

## **✅ 6. Monitoring & Observability**

### **Prometheus + Grafana Stack**
```bash
helm install prometheus prometheus-community/kube-prometheus-stack \
    -n monitoring --create-namespace \
    -f infrastructure/helm/monitoring-values.yaml
```

**Features**:
- ServiceMonitor for each microservice
- Grafana dashboards for K8s, JVM, Nginx
- Alerting rules for pod failures, high CPU, memory issues

### **Logging Stack (Choose One)**

**Option A: Grafana Loki**
- FluentBit → Loki → Grafana
- Lightweight, cost-effective

**Option B: ELK Stack**
- FluentBit → Elasticsearch → Kibana
- More features, higher resource usage

**Option C: AWS-Native**
- FluentBit → S3 → Athena
- Cheapest, query-based analysis

### **Deliverables**
- `infrastructure/helm/monitoring-values.yaml`
- `infrastructure/monitoring/prometheus.yml`
- `infrastructure/monitoring/grafana/dashboards/*.json`
- `infrastructure/kubernetes/logging/` (FluentBit DaemonSet)

---

## **✅ 7. Documentation (Critical)**

### **7.1 README.md**
- Project overview with architecture diagram
- Technology stack with versions
- Quick start guide
- Local development setup
- Docker Compose instructions
- Kubernetes deployment overview

### **7.2 PREREQUISITES.md**
- Required tools: AWS CLI, kubectl, Terraform, Helm, Docker, Maven, Node.js
- AWS account setup
- IAM permissions needed
- Domain setup (if applicable)
- Cost estimates

### **7.3 EXECUTION-GUIDE.md**

**Must include step-by-step instructions with validation commands**:

1. **Infrastructure Deployment**
   ```bash
   # Set environment variables
   export AWS_REGION=us-east-2
   export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
   
   # Initialize Terraform
   cd infrastructure/terraform
   terraform init
   
   # Create terraform.tfvars with all required variables
   
   # Apply infrastructure
   terraform apply
   
   # Verify outputs
   terraform output
   ```

2. **EKS Configuration**
   ```bash
   # Update kubeconfig
   aws eks update-kubeconfig --name ${CLUSTER_NAME} --region ${AWS_REGION}
   
   # Verify cluster access
   kubectl get nodes
   
   # Check EBS CSI driver
   kubectl get pods -n kube-system | grep ebs
   ```

3. **Install Cluster Add-ons**
   - ALB Controller (with complete IAM policy)
   - External Secrets Operator
   - Metrics Server

4. **Secret Management**
   - Verify Terraform created secrets in AWS Secrets Manager
   - Validate secret paths match K8s ExternalSecrets
   - Check secret synchronization: `kubectl get externalsecrets -n production`

5. **Build and Push Images**
   ```bash
   # Login to ECR
   aws ecr get-login-password --region ${AWS_REGION} | docker login...
   
   # Build each service
   # Push to ECR with tags
   ```

6. **Deploy Applications**
   ```bash
   # Apply in order:
   kubectl apply -f infrastructure/kubernetes/namespaces.yaml
   kubectl apply -f infrastructure/kubernetes/configmaps.yaml
   kubectl apply -f infrastructure/kubernetes/external-secrets.yaml
   kubectl apply -f infrastructure/kubernetes/deployments/
   kubectl apply -f infrastructure/kubernetes/ingress.yaml
   ```

7. **Validation**
   ```bash
   # Check pod status
   kubectl get pods -n production
   
   # Check logs
   kubectl logs -f deployment/auth-service -n production
   
   # Check ingress
   kubectl get ingress -n production
   
   # Get ALB DNS
   ALB_DNS=$(kubectl get ingress ... -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
   
   # Test endpoints
   curl http://${ALB_DNS}/api/v1/auth/actuator/health
   ```

### **7.4 TROUBLESHOOTING.md**

**Common Issues with Solutions**:

1. **EBS CSI Driver CrashLoopBackOff**
   - Symptom: Controller pods failing
   - Cause: Missing IAM permissions
   - Fix: Attach AmazonEBSCSIDriverPolicy to node role

2. **External Secrets Not Syncing**
   - Symptom: ExternalSecret status not Ready
   - Cause: IAM role missing permissions or wrong secret path
   - Fix: Verify IRSA trust policy, check secret path matches Terraform output

3. **Database Connection Failures**
   - Symptom: Service logs show "Could not connect to database"
   - Cause: Environment variable ordering, wrong credentials, security group
   - Fix: Check env var order, verify RDS password matches secret, check SG rules

4. **Frontend 502/504 Errors**
   - Symptom: ALB returns bad gateway
   - Cause: Container port mismatch, nginx not running
   - Fix: Verify containerPort: 8080, check nginx.pid permissions

5. **ALB Not Created**
   - Symptom: Ingress has no ADDRESS
   - Cause: IAM permissions missing, AWS account restriction
   - Fix: Apply complete ALB controller IAM policy, check AWS service quotas

6. **Cart Service Database Errors**
   - Symptom: Cart service trying to connect to PostgreSQL
   - Cause: Spring Boot auto-configuration not disabled
   - Fix: Add exclusions to application.properties

### **7.5 RUNBOOK.md**
- Service restart procedures
- Scaling operations
- Database backup/restore
- Incident response checklist
- Rollback procedures
- Log analysis commands

---

## **✅ 8. Testing & Validation**

### **Local Testing**
```bash
# Run with Docker Compose
docker-compose up

# Test APIs with Postman collection
```

### **Kubernetes Testing**
```bash
# Port-forward for local access
kubectl port-forward svc/frontend -n production 8080:80

# Run integration tests
mvn verify -Pintegration-test
```

### **Load Testing**
- Use k6 or Apache JMeter
- Test scenarios: user registration, search, booking flow
- Target: 100 RPS for 5 minutes

---

## **✅ 9. Project Structure**

```
ota-travel-application/
├── backend/
│   ├── pom.xml
│   ├── common/
│   ├── auth-service/
│   ├── booking-service/
│   ├── cart-service/
│   ├── payment-service/
│   └── search-service/
├── frontend/
│   ├── src/
│   ├── public/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── vite.config.ts
├── infrastructure/
│   ├── terraform/
│   │   ├── main.tf
│   │   ├── vpc.tf
│   │   ├── eks.tf
│   │   ├── rds.tf
│   │   ├── elasticache-opensearch.tf
│   │   ├── s3-cloudfront.tf
│   │   ├── api-gateway-messaging.tf
│   │   ├── ecr.tf
│   │   ├── iam.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   └── terraform.tfvars
│   ├── kubernetes/
│   │   ├── namespaces.yaml
│   │   ├── configmaps.yaml
│   │   ├── external-secrets.yaml
│   │   ├── ingress.yaml
│   │   └── deployments/
│   ├── helm/
│   │   └── monitoring-values.yaml
│   └── monitoring/
├── cicd/
│   ├── jenkins/
│   │   └── Jenkinsfile
│   └── argocd/
│       └── applications.yaml
├── docs/
│   ├── PREREQUISITES.md
│   ├── EXECUTION-GUIDE.md
│   ├── TROUBLESHOOTING.md
│   └── RUNBOOK.md
├── scripts/
│   └── init-db.sql
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

## **✅ 10. .gitignore**

Auto-generate based on file types:
```
# Maven
target/
*.class

# Node
node_modules/
dist/
build/

# Terraform
.terraform/
*.tfstate
*.tfstate.backup
.terraform.lock.hcl

# IDE
.idea/
.vscode/
*.iml

# Secrets
*.pem
*.key
.env

# OS
.DS_Store
Thumbs.db
```

---

## **✅ 11. Critical Pre-Execution Checklist**

Before running the project, validate:

- [ ] All Docker base images use latest stable versions (alpine where possible)
- [ ] Kubernetes API versions are current (networking.k8s.io/v1, external-secrets.io/v1)
- [ ] Terraform secret paths match exactly with ExternalSecret references
- [ ] IAM policies are complete (use official AWS/K8s documentation)
- [ ] Database environment variables are properly ordered in deployment YAMLs
- [ ] Container ports, service ports, and application ports all align
- [ ] Health check paths match actual application endpoints
- [ ] Cart service explicitly disables database auto-configuration
- [ ] Frontend nginx runs on port 8080 with proper permissions
- [ ] All services use non-root users in containers
- [ ] RDS passwords in Secrets Manager match actual RDS instance passwords
- [ ] Security groups allow necessary traffic between components
- [ ] All Terraform outputs are validated before K8s deployment
- [ ] EBS CSI driver is running before deploying stateful workloads

---

## **✅ 12. Payment Service Alternative**

Instead of Stripe, use:
- **Mock payment service** with predefined responses
- **Open-source alternative**: Razorpay test mode (free), PayPal sandbox, or custom mock API
- Store payment status in PostgreSQL
- Return success/failure based on test card numbers

---

## **✅ 13. Logging Stack Recommendation**

**For testing/learning**: Grafana Loki (lightweight, easy setup)
**For production**: ELK Stack (comprehensive) or AWS-native (cost-effective)

---

## **🎯 Success Criteria**

The project is complete when:

1. All services are running with 1/1 Ready status in Kubernetes
2. ALB is provisioned with a valid DNS name (or port-forward works)
3. Frontend loads successfully
4. User can register, login, search, add to cart, and complete booking flow
5. All health checks return 200 OK
6. Database connections are stable with no authentication errors
7. Secrets are syncing from AWS Secrets Manager to K8s
8. Monitoring dashboards show metrics for all services
9. Jenkins pipeline can build and deploy changes
10. ArgoCD shows all applications as Healthy and Synced
11. Complete documentation allows independent redeployment from scratch

---

**This prompt ensures a production-grade, deployment-validated OTA application with zero configuration drift, complete IAM policies, proper secret management, and comprehensive troubleshooting guidance.**