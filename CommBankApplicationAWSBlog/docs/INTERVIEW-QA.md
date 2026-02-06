# CommSec Trading Platform - Interview Questions & Answers

## Overview
This document contains real-world interview questions and comprehensive answers based on the CommSec Trading Platform architecture. These questions are commonly asked in senior/staff-level positions at big tech companies, banks, and financial institutions.

---

## Table of Contents
1. [Architecture Design Questions](#1-architecture-design-questions)
2. [High Availability & Resilience](#2-high-availability--resilience)
3. [AWS Services Deep Dive](#3-aws-services-deep-dive)
4. [Kubernetes & Container Orchestration](#4-kubernetes--container-orchestration)
5. [Database & Data Management](#5-database--data-management)
6. [Security & Compliance](#6-security--compliance)
7. [Performance & Scalability](#7-performance--scalability)
8. [Incident Response & Troubleshooting](#8-incident-response--troubleshooting)
9. [DevOps & CI/CD](#9-devops--cicd)
10. [Scenario-Based Questions](#10-scenario-based-questions)

---

## 1. Architecture Design Questions

### Q1.1: Why did CommSec migrate from a multi-cloud active-active setup to AWS-only?

**Answer:**
CommSec initially used a multi-cloud active-active architecture for fault isolation, but this created significant operational overhead:

1. **Complexity Challenges:**
   - Maintaining two separate deployment pipelines
   - Operating model spanning two cloud platforms
   - Custom failover process requiring external witness capabilities
   - Reduced development velocity and engineering proficiency

2. **Benefits of AWS Consolidation:**
   - 25% base capacity reduction (3 AZs vs 4 stacks)
   - Standardized deployment pipelines
   - 2x faster OS patching and code deployments
   - Out-of-the-box failover with ARC zonal shift
   - Better integration with AWS-native services

3. **Resilience Equivalence:**
   - Availability Zones provide physical and logical isolation
   - ARC zonal shift enables instant traffic redirection
   - No dependency on control plane during failures

---

### Q1.2: Explain the three-tier architecture and why each tier is important.

**Answer:**
The architecture consists of:

**Web Tier (Frontend):**
- React-based single-page application
- Deployed on EKS with Nginx
- Behind public-facing ALB
- Purpose: User interface, client-side rendering, API calls

**Application Tier (Backend):**
- Spring Boot microservices (Trading, Portfolio, Market Data, API Gateway)
- Deployed on EKS in private subnets
- Behind internal ALB
- Purpose: Business logic, order processing, data aggregation

**Database Tier:**
- RDS PostgreSQL Multi-AZ
- In isolated database subnets
- Purpose: Persistent data storage, ACID transactions

**Benefits of Separation:**
- Independent scaling per tier
- Security boundaries (defense in depth)
- Fault isolation
- Technology flexibility
- Easier testing and deployment

---

### Q1.3: How would you design the network topology for this architecture?

**Answer:**
```
VPC (10.0.0.0/16)
├── Public Subnets (for ALB, NAT Gateway)
│   ├── us-east-1a: 10.0.1.0/24
│   ├── us-east-1b: 10.0.2.0/24
│   └── us-east-1c: 10.0.3.0/24
├── Private Subnets (for EKS nodes)
│   ├── us-east-1a: 10.0.11.0/24
│   ├── us-east-1b: 10.0.12.0/24
│   └── us-east-1c: 10.0.13.0/24
└── Database Subnets (isolated, no internet)
    ├── us-east-1a: 10.0.21.0/24
    ├── us-east-1b: 10.0.22.0/24
    └── us-east-1c: 10.0.23.0/24
```

**Key Design Decisions:**
- NAT Gateway per AZ for HA egress
- Database subnets have no route to internet
- Security groups as virtual firewalls between tiers
- VPC endpoints for AWS services (ECR, S3) to reduce costs and improve security

---

## 2. High Availability & Resilience

### Q2.1: What is ARC Zonal Shift and how does it help with failures?

**Answer:**
**AWS Application Recovery Controller (ARC) Zonal Shift** is a capability that allows you to shift application traffic away from an impaired Availability Zone.

**How it works:**
1. When started, it removes the ALB node's IP from DNS (stops new connections)
2. Instructs remaining ALB nodes not to route to targets in the impaired AZ
3. Cross-zone load balancing continues in healthy AZs

**Types of Failures it Addresses:**
- **Gray failures**: Partial AZ impairment not detected by health checks
- **Elevated latency**: When one AZ is slower than others
- **Application issues**: When your app in one AZ has problems

**Benefits:**
- No reliance on control plane actions (works when AWS APIs are impaired)
- Sub-minute traffic redirection
- Can be triggered manually or automated
- Low-risk testing for DR procedures

**Example Use Case:**
```
Before zonal shift:
AZ-1a: 33% traffic ← Issues detected
AZ-1b: 33% traffic
AZ-1c: 33% traffic

After zonal shift away from us-east-1a:
AZ-1a: 0% traffic ← Shifted
AZ-1b: 50% traffic
AZ-1c: 50% traffic
```

---

### Q2.2: How do you handle the 3x traffic spike at market open?

**Answer:**
CommSec traffic increases threefold between 9:59-10:02 AM at market open. We handle this with:

**1. LCU (Load Balancer Capacity Unit) Reservations:**
```bash
# Pre-warm ALB before market open
aws elbv2 modify-load-balancer-attributes \
    --load-balancer-arn $ALB_ARN \
    --attributes Key=load_balancing.cross_zone.enabled,Value=true
```
- Reserve sufficient ALB capacity before the spike
- Avoid reactive scaling delays

**2. Horizontal Pod Autoscaler (HPA) with Predictive Scaling:**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 6  # Higher baseline before market open
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 50  # Lower threshold for faster scaling
```

**3. Cluster Autoscaler Configuration:**
- Pre-scale node groups 30 minutes before market open
- Use spot instances for burst capacity
- Maintain warm pool of instances

**4. Application Bootstrap Optimization:**
- Store binaries in S3 (no external dependencies)
- Pre-pulled container images
- Fast startup times (<30 seconds)

---

### Q2.3: What's the difference between Multi-AZ, Multi-Region, and Active-Active deployments?

**Answer:**

| Aspect | Multi-AZ | Multi-Region | Active-Active |
|--------|----------|--------------|---------------|
| **Scope** | Single region | Multiple regions | Multiple regions |
| **Latency** | Sub-millisecond | 50-200ms | Optimized per region |
| **Failover** | Automatic | Manual/DNS-based | No failover needed |
| **Data Sync** | Synchronous | Asynchronous | Conflict resolution |
| **Cost** | Low | Medium | High |
| **Complexity** | Low | Medium | High |
| **Use Case** | AZ failure | Regional disaster | Global availability |

**CommSec Choice (Multi-AZ with ARC):**
- Data sovereignty requirements (must stay in Australia)
- Sub-millisecond latency requirements for trading
- Simpler operations vs multi-region
- ARC provides instant failover without multi-region complexity

---

## 3. AWS Services Deep Dive

### Q3.1: Why use EKS instead of ECS or EC2 Auto Scaling Groups?

**Answer:**

**EKS Benefits:**
1. **Portability**: Standard Kubernetes, no vendor lock-in
2. **Ecosystem**: Rich ecosystem of tools (Helm, Istio, ArgoCD)
3. **Developer familiarity**: Team already knows Kubernetes
4. **Fine-grained scaling**: Pod-level autoscaling
5. **Service mesh ready**: Easy to add Istio if needed
6. **Multi-cloud strategy**: Skills transfer if future diversification

**When ECS might be better:**
- Smaller teams without Kubernetes expertise
- Simpler deployments
- Tighter AWS integration needed
- Lower operational overhead desired

**When EC2 ASG might be better:**
- Legacy applications
- VM-based software licenses
- Specific OS requirements

**Trade-offs accepted:**
- Higher operational complexity with EKS
- More expensive control plane ($70/month per cluster)
- Steeper learning curve

---

### Q3.2: Explain the ALB configuration and why cross-zone load balancing is important.

**Answer:**

**ALB Configuration:**
```hcl
resource "aws_lb" "web_alb" {
  name               = "commsec-web-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.web_alb.id]
  subnets            = aws_subnet.public[*].id

  enable_cross_zone_load_balancing = true
  enable_deletion_protection       = true
}
```

**Cross-Zone Load Balancing:**
- Without it: Each ALB node only sends to targets in its AZ
- With it: ALB nodes distribute across all healthy targets regardless of AZ

**Why it's critical for CommSec:**
1. **Even distribution**: Prevents hot spots when AZs have unequal capacity
2. **Zonal shift compatibility**: During zonal shift, remaining AZs handle all traffic
3. **Flexible scaling**: Can scale AZs independently without traffic imbalance

**Diagram:**
```
Without Cross-Zone:          With Cross-Zone:
ALB Node A → Targets A       ALB Node A → Targets A, B, C
ALB Node B → Targets B       ALB Node B → Targets A, B, C
ALB Node C → Targets C       ALB Node C → Targets A, B, C
```

---

### Q3.3: How do you configure RDS for maximum availability?

**Answer:**

```hcl
resource "aws_db_instance" "postgres" {
  identifier     = "commsec-postgres"
  engine         = "postgres"
  engine_version = "16.4"
  instance_class = "db.r6g.xlarge"
  
  # High Availability
  multi_az = true
  
  # Storage
  storage_type          = "gp3"
  allocated_storage     = 100
  max_allocated_storage = 1000
  storage_encrypted     = true
  
  # Backup
  backup_retention_period = 35
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"
  
  # Network
  db_subnet_group_name   = aws_db_subnet_group.postgres.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  
  # Performance
  performance_insights_enabled = true
  monitoring_interval          = 60
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  
  # Protection
  deletion_protection = true
  skip_final_snapshot = false
}
```

**Multi-AZ Behavior:**
- Synchronous replication to standby
- Automatic failover (60-120 seconds)
- Standby handles backups
- No performance impact on primary

---

## 4. Kubernetes & Container Orchestration

### Q4.1: How do you ensure pods are distributed across Availability Zones?

**Answer:**

**Pod Topology Spread Constraints:**
```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      topologySpreadConstraints:
      - maxSkew: 1
        topologyKey: topology.kubernetes.io/zone
        whenUnsatisfiable: DoNotSchedule
        labelSelector:
          matchLabels:
            app: trading-service
```

**Pod Anti-Affinity:**
```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
    - labelSelector:
        matchLabels:
          app: trading-service
      topologyKey: "topology.kubernetes.io/zone"
```

**Zone-Aware Node Groups:**
```bash
# EKS node groups per AZ
eksctl create nodegroup \
  --cluster=commsec \
  --name=web-us-east-1a \
  --node-zones=us-east-1a

eksctl create nodegroup \
  --cluster=commsec \
  --name=web-us-east-1b \
  --node-zones=us-east-1b

eksctl create nodegroup \
  --cluster=commsec \
  --name=web-us-east-1c \
  --node-zones=us-east-1c
```

---

### Q4.2: How do you handle Kubernetes secrets securely?

**Answer:**

**Best Practices Implementation:**

**1. AWS Secrets Manager Integration:**
```yaml
apiVersion: secrets-store.csi.x-k8s.io/v1
kind: SecretProviderClass
metadata:
  name: db-secrets
spec:
  provider: aws
  parameters:
    objects: |
      - objectName: "commsec/production/db-credentials"
        objectType: "secretsmanager"
```

**2. IRSA (IAM Roles for Service Accounts):**
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: trading-service
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789:role/commsec-trading-role
```

**3. Never store secrets in:**
- ConfigMaps
- Environment variables in manifests
- Container images
- Git repositories

**4. Encryption:**
- EKS encrypts secrets at rest with KMS
- Use custom KMS key for additional control

---

### Q4.3: Explain the difference between liveness, readiness, and startup probes.

**Answer:**

| Probe | Purpose | Failure Action | Use Case |
|-------|---------|----------------|----------|
| **Liveness** | Is the app alive? | Restart container | Deadlock detection |
| **Readiness** | Can it serve traffic? | Remove from service | Graceful warmup |
| **Startup** | Has it started? | Block other probes | Slow-starting apps |

**CommSec Configuration:**
```yaml
spec:
  containers:
  - name: trading-service
    livenessProbe:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
      failureThreshold: 3
    
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 5
      failureThreshold: 3
    
    startupProbe:
      httpGet:
        path: /actuator/health
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 5
      failureThreshold: 30  # 5s * 30 = 150s max startup
```

**Why all three:**
- Startup probe prevents premature liveness checks during JVM warmup
- Readiness ensures traffic only goes to warmed-up instances
- Liveness catches deadlocks or stuck threads

---

## 5. Database & Data Management

### Q5.1: How do you handle database connection pooling in a Kubernetes environment?

**Answer:**

**Connection Pool Configuration (HikariCP):**
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 30000
      max-lifetime: 1800000
```

**Calculating Pool Size:**
```
Max connections = (replicas × pool_size)
= 3 pods × 20 connections = 60 connections

RDS max_connections = LEAST(DBInstanceClassMemory/9531392, 5000)
For db.r6g.xlarge (32GB): ~3,500 connections

Safety margin: 60 << 3,500 ✓
```

**Connection Pooling with PgBouncer (Optional):**
- Sits between app and database
- Reduces connection overhead
- Handles connection reuse
- Useful for serverless or high-replica scenarios

---

### Q5.2: How do you ensure data consistency in a distributed microservices architecture?

**Answer:**

**Patterns Used:**

**1. Saga Pattern for Distributed Transactions:**
```
Order Flow:
1. Trading Service: Create Order → Pending
2. Portfolio Service: Reserve Holdings → Reserved
3. Market Service: Execute Trade → Executed
4. Trading Service: Confirm Order → Confirmed

Compensation (if step 3 fails):
- Portfolio Service: Release Holdings
- Trading Service: Cancel Order
```

**2. Event Sourcing for Audit Trail:**
```java
@Entity
public class TradeEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String eventType; // ORDER_PLACED, ORDER_EXECUTED, etc.
    private String payload;
    private int version;
}
```

**3. Outbox Pattern for Reliable Events:**
```sql
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255),
    aggregate_id VARCHAR(255),
    event_type VARCHAR(255),
    payload JSONB,
    created_at TIMESTAMP,
    processed_at TIMESTAMP
);
```

---

## 6. Security & Compliance

### Q6.1: How do you implement security in a regulated financial platform?

**Answer:**

**Defense in Depth Layers:**

**1. Network Security:**
- Private subnets for workloads
- Security groups with least-privilege rules
- NACLs as additional firewall
- VPC Flow Logs for monitoring

**2. Identity & Access:**
- IRSA for pod-level IAM
- No long-lived credentials
- MFA for console access
- Role-based access control

**3. Data Protection:**
- TLS 1.3 everywhere
- RDS encryption at rest (AES-256)
- S3 bucket encryption
- Secrets in Secrets Manager (not K8s secrets)

**4. Compliance:**
- Data sovereignty (all data in Australia)
- Audit logging (CloudTrail)
- Compliance reporting
- Regular penetration testing

**5. Container Security:**
- Minimal base images (distroless)
- No root users in containers
- Pod Security Standards
- Image scanning (ECR)

---

### Q6.2: How do you handle data sovereignty requirements?

**Answer:**

**CommSec Requirements (ASIC Regulated):**
- All customer data must remain in Australia
- Audit trails for all transactions
- Encryption of PII

**Implementation:**
```hcl
# Force ap-southeast-2 for CommSec (we use us-east-1 for demo)
provider "aws" {
  region = "ap-southeast-2"  # Sydney
  
  # Prevent accidental resource creation elsewhere
  default_tags {
    tags = {
      DataSovereignty = "AU"
      Compliance      = "ASIC"
    }
  }
}

# S3 bucket with replication disabled
resource "aws_s3_bucket" "binaries" {
  bucket = "commsec-binaries"
  
  # No cross-region replication
}

# RDS without read replicas in other regions
resource "aws_db_instance" "postgres" {
  # ...
  # No cross-region read replicas
}
```

---

## 7. Performance & Scalability

### Q7.1: How do you optimize application performance for trading?

**Answer:**

**1. Latency Optimization:**
```java
// Connection pooling
@Configuration
public class DatabaseConfig {
    @Bean
    public HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(3000); // 3s timeout
        return new HikariDataSource(config);
    }
}
```

**2. Caching Strategy:**
```java
@Cacheable(value = "marketData", key = "#symbol")
public Quote getQuote(String symbol) {
    return marketDataService.getQuote(symbol);
}
```

**3. Async Processing:**
```java
@Async
public CompletableFuture<Order> processOrder(OrderRequest request) {
    // Non-blocking order processing
}
```

**4. Database Optimization:**
```sql
-- Proper indexing
CREATE INDEX CONCURRENTLY idx_orders_user_date 
ON orders(user_id, created_at DESC);

-- Partitioning by date
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ
) PARTITION BY RANGE (created_at);
```

---

### Q7.2: Explain the WebSocket implementation for real-time market data.

**Answer:**

**Server-Side (Spring Boot):**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/quotes")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

@Service
public class QuoteBroadcaster {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Scheduled(fixedRate = 100) // 10 updates per second
    public void broadcastQuotes() {
        List<Quote> quotes = marketDataService.getLatestQuotes();
        messagingTemplate.convertAndSend("/topic/quotes", quotes);
    }
}
```

**Client-Side (React):**
```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
    webSocketFactory: () => new SockJS('/ws/quotes'),
    onConnect: () => {
        client.subscribe('/topic/quotes', (message) => {
            const quotes = JSON.parse(message.body);
            updateQuotes(quotes);
        });
    },
    reconnectDelay: 5000,
});

client.activate();
```

**Scaling Considerations:**
- Sticky sessions for WebSocket connections
- Redis pub/sub for multi-pod broadcasting
- Connection limits per client

---

## 8. Incident Response & Troubleshooting

### Q8.1: Walk me through how you would diagnose a production outage.

**Answer:**

**Systematic Approach (OODA Loop):**

**1. Observe (First 2 minutes):**
```bash
# Check if platform is reachable
curl -s -o /dev/null -w "%{http_code}" https://commsec.example.com

# Check pod status
kubectl get pods -n commsec

# Check recent events
kubectl get events -n commsec --sort-by='.lastTimestamp' | tail -20
```

**2. Orient (Next 3 minutes):**
```bash
# Identify scope
# - Single service or multiple?
# - Single AZ or all?
# - Started suddenly or gradually?

# Check metrics
kubectl top pods -n commsec
kubectl top nodes

# Check logs
kubectl logs -l app=api-gateway -n commsec --tail=100 | grep -i error
```

**3. Decide (1 minute):**
```
Decision tree:
├── Single AZ issue? → Zonal shift
├── Database issue? → Check RDS, failover if needed
├── Recent deployment? → Rollback
├── Resource exhaustion? → Scale up
└── Unknown → Engage senior engineer + AWS support
```

**4. Act:**
```bash
# Example: Zonal shift for AZ issue
./scripts/initiate-zonal-shift.sh us-east-1a

# Example: Rollback
kubectl rollout undo deployment/trading-service -n commsec

# Example: Scale
kubectl scale deployment/trading-service -n commsec --replicas=10
```

---

### Q8.2: How do you differentiate between a gray failure and a complete AZ failure?

**Answer:**

**Gray Failure Characteristics:**
- Partial degradation (elevated latency, intermittent errors)
- Health checks may still pass
- Not detected by AWS Health Dashboard
- Inconsistent behavior

**Complete AZ Failure:**
- All instances in AZ unreachable
- Health checks fail consistently
- Usually appears on AWS Health Dashboard
- Consistent behavior

**Detection Methods:**

**1. Latency Percentiles:**
```bash
# If p99 latency from one AZ is significantly higher
aws cloudwatch get-metric-statistics \
    --namespace AWS/ApplicationELB \
    --metric-name TargetResponseTime \
    --dimensions Name=AvailabilityZone,Value=us-east-1a \
    --statistics Average p99
```

**2. Error Rate by AZ:**
```bash
# Compare error rates across AZs
# If one AZ has 50% more errors → gray failure
```

**3. Application Metrics:**
```java
// Instrument with AZ tag
@Timed(value = "order.processing", extraTags = {"az", "${AZ}"})
public void processOrder(Order order) { }
```

**Response Difference:**
- Gray failure: Zonal shift (proactive, quick)
- Complete failure: AWS auto-handling + zonal shift if needed

---

## 9. DevOps & CI/CD

### Q9.1: Describe your CI/CD pipeline for this platform.

**Answer:**

**Pipeline Stages:**

```yaml
# GitHub Actions workflow
name: CommSec CI/CD

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      # Frontend tests
      - name: Frontend Tests
        working-directory: ./frontend
        run: |
          npm ci
          npm run lint
          npm run test
      
      # Backend tests
      - name: Backend Tests
        working-directory: ./backend/trading-service
        run: ./mvnw verify
      
      # Security scan
      - name: Security Scan
        uses: snyk/actions/maven@master

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      # Build and push Docker images
      - name: Build & Push
        run: |
          docker build -t $ECR_REGISTRY/commsec-trading:$SHA .
          docker push $ECR_REGISTRY/commsec-trading:$SHA

  deploy-staging:
    needs: build
    environment: staging
    steps:
      - name: Deploy to Staging
        run: |
          kubectl set image deployment/trading-service \
            trading=$ECR_REGISTRY/commsec-trading:$SHA \
            -n commsec-staging

  integration-tests:
    needs: deploy-staging
    steps:
      - name: Run E2E Tests
        run: npm run test:e2e

  deploy-production:
    needs: integration-tests
    environment: production
    steps:
      - name: Deploy to Production
        run: |
          kubectl set image deployment/trading-service \
            trading=$ECR_REGISTRY/commsec-trading:$SHA \
            -n commsec
```

**Key Practices:**
- Immutable artifacts (versioned images)
- Progressive rollout (canary/blue-green)
- Automated rollback on failure
- No direct production access
- Audit trail for all deployments

---

### Q9.2: How do you handle rollbacks safely?

**Answer:**

**1. Kubernetes Native Rollback:**
```bash
# Check rollout history
kubectl rollout history deployment/trading-service -n commsec

# Rollback to previous version
kubectl rollout undo deployment/trading-service -n commsec

# Rollback to specific revision
kubectl rollout undo deployment/trading-service -n commsec --to-revision=2
```

**2. Database Rollback Strategy:**
```sql
-- Every migration has a rollback script
-- V1__create_orders_table.sql
CREATE TABLE orders (...);

-- V1__create_orders_table_rollback.sql
DROP TABLE orders;
```

**3. Feature Flags for Safe Rollout:**
```java
if (featureFlags.isEnabled("new-order-flow")) {
    return newOrderProcessor.process(order);
} else {
    return legacyOrderProcessor.process(order);
}
```

**4. Canary Deployments:**
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
spec:
  strategy:
    canary:
      steps:
      - setWeight: 10
      - pause: {duration: 5m}
      - setWeight: 50
      - pause: {duration: 10m}
      - setWeight: 100
```

---

## 10. Scenario-Based Questions

### Q10.1: It's 9:59 AM and you're about to have a 3x traffic spike at market open. The platform is already showing elevated latency. What do you do?

**Answer:**

**Immediate Actions (T-1 minute):**

```bash
# 1. Check current state
kubectl top pods -n commsec
kubectl get hpa -n commsec

# 2. Emergency scale-up if HPA hasn't caught up
kubectl scale deployment/api-gateway -n commsec --replicas=15
kubectl scale deployment/trading-service -n commsec --replicas=15
kubectl scale deployment/frontend -n commsec --replicas=10

# 3. Check for any unhealthy pods
kubectl get pods -n commsec | grep -v Running

# 4. If latency is from one AZ, consider preemptive zonal shift
aws arc-zonal-shift start-zonal-shift \
    --resource-identifier $ALB_ARN \
    --away-from us-east-1a \
    --expires-in 30m \
    --comment "Preemptive shift for market open"
```

**Communication:**
- Alert on-call team
- Notify trading desk
- Prepare rollback if new deployment was recent

**Post-Market Open:**
- Analyze why HPA didn't scale in time
- Review LCU reservation configuration
- Update runbook if needed

---

### Q10.2: A customer reports that their order was placed but they don't see it in their portfolio. How do you investigate?

**Answer:**

**Step 1: Gather Information**
```
- Order ID
- Customer ID
- Timestamp
- What they see vs expect
```

**Step 2: Trace the Request**
```bash
# Check trading service logs
kubectl logs -l app=trading-service -n commsec | grep "ORDER_ID"

# Check portfolio service logs
kubectl logs -l app=portfolio-service -n commsec | grep "CUSTOMER_ID"

# Check database
kubectl exec -it $(kubectl get pod -l app=trading-service -n commsec -o name | head -1) \
    -n commsec -- curl localhost:8080/internal/orders/ORDER_ID
```

**Step 3: Common Causes & Fixes**

| Cause | Diagnostic | Fix |
|-------|------------|-----|
| Event not published | Check outbox table | Retry event |
| Event not consumed | Check consumer lag | Restart consumer |
| DB transaction failed | Check DB logs | Investigate locks |
| Cache stale | Check cache TTL | Invalidate cache |
| Race condition | Check timestamps | Fix with locking |

**Step 4: Resolution**
```bash
# If event was lost, manual reconciliation
kubectl exec -it trading-service-pod -n commsec -- \
    java -jar cli.jar reconcile-order ORDER_ID
```

---

### Q10.3: AWS reports an issue in us-east-1a. Walk me through your response.

**Answer:**

**T+0: Alert Received**
```bash
# 1. Verify the impact
kubectl get pods -n commsec -o wide | grep us-east-1a

# 2. Check if ARC auto-shift triggered
aws arc-zonal-shift list-zonal-shifts

# 3. Check application health
curl -s http://$ALB_DNS/api/health
```

**T+2 minutes: Initiate Zonal Shift**
```bash
# If auto-shift didn't trigger
./scripts/initiate-zonal-shift.sh us-east-1a

# Verify traffic shifted
aws elbv2 describe-target-health --target-group-arn $TG_ARN
```

**T+5 minutes: Communicate**
```
- Status: Platform operational on 2/3 AZs
- Impact: Reduced capacity, may see slight latency increase
- ETA: Following AWS Health Dashboard updates
```

**T+10 minutes: Monitor**
```bash
# Watch for capacity issues
kubectl top pods -n commsec
kubectl get hpa -n commsec

# Scale if needed
kubectl scale deployment/trading-service -n commsec --replicas=12
```

**T+X: AWS Resolves Issue**
```bash
# Verify AZ health
kubectl get pods -n commsec -o wide | grep us-east-1a

# Cancel zonal shift
./scripts/cancel-zonal-shift.sh

# Monitor return to normal
```

---

## Tips for Interviewers

### What They're Looking For:
1. **Depth + Breadth**: Deep knowledge in some areas, awareness of others
2. **Trade-off Analysis**: Understanding why decisions were made
3. **Production Mindset**: Focus on reliability, not just features
4. **Communication**: Ability to explain complex topics clearly
5. **Incident Response**: Calm, systematic approach to problems

### Red Flags:
- Can't explain trade-offs
- No mention of monitoring/observability
- Ignores security considerations
- Can't handle "what if" scenarios
- Only knows theory, no practical experience

### Green Flags:
- Mentions specific services and how they configured them
- Discusses lessons learned from incidents
- Considers cost optimization
- Thinks about team and process, not just technology
- Asks clarifying questions

---

## Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-31 | Platform Team | Initial version |
