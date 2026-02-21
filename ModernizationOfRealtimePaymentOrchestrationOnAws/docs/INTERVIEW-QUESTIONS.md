# Real-Time Payment Orchestration — Interview Questions & Answers

> Scenario-based questions asked at FAANG, fintech (Stripe, PayPal, Square), and financial institutions (Goldman Sachs, JPMorgan) for senior engineering roles.

---

## Section 1: Architecture & System Design

### Q1: How would you design a real-time payment orchestration system handling 1,000+ TPS with sub-second latency?

**Answer**: Event-driven microservices on AWS:
1. **Decompose** into bounded contexts: Initiation, Execution, Risk, Tracking, Reconciliation, Billing
2. **Amazon MSK (Kafka)** as the event backbone — dedicated topics per stage
3. **Amazon EKS** with HPA for elastic scaling
4. **Edge-optimized API Gateway** + CloudFront for global low-latency ingress
5. **PostgreSQL (RDS Multi-AZ)** for ACID transactions with read replicas
6. **Circuit breakers** (Resilience4j) preventing cascade failures
7. **Correlation IDs** through Kafka headers for distributed tracing

Key: Replace sequential with parallel event processing — fraud checks, routing, and ledger updates run concurrently, reducing latency from seconds to ~200ms.

### Q2: Why Kafka (MSK) over SQS/SNS for payment event streaming?

**Answer**:
| Feature | MSK (Kafka) | SQS/SNS |
|---------|-------------|---------|
| Message ordering | Guaranteed per partition | FIFO limited to 300 TPS |
| Replay capability | Consumer can rewind offset | Once consumed, deleted |
| Multi-consumer | N consumer groups per topic | Requires SNS+SQS fan-out |
| Throughput | 1M+ msg/sec per cluster | Soft limits |

Payments need strict ordering (partition key = tenantId), event replay for reconciliation, and multiple independent consumers.

### Q3: How do you handle exactly-once semantics in payment processing?

**Answer**: Defense in depth:
1. **Kafka Idempotent Producer**: `enable.idempotence=true`, `acks=all`
2. **Transactional Consumer**: `isolation.level=read_committed`
3. **Application idempotency**: Check `transaction_id` exists before processing
4. **Database constraint**: `UNIQUE(idempotency_key, tenant_id)`
5. **Outbox Pattern**: Write to DB + outbox table atomically, CDC publishes to Kafka

### Q4: Explain multi-tenant isolation in this payment platform.

**Answer**: Isolation at every layer:
1. **Data**: PostgreSQL Row-Level Security (RLS) filtering by `tenant_id`
2. **Kafka**: Partition key = `tenantId` for ordering guarantee
3. **Network**: Private subnets, VPC boundaries
4. **IAM**: Tenant-specific roles via IRSA
5. **API Gateway**: Usage plans with tenant API keys and rate limits
6. **Config**: Region-specific clearing rules via ConfigMaps, not code

### Q5: How do you achieve 99.999% availability?

**Answer**: 5.26 minutes downtime/year requires:
1. **Multi-AZ**: EKS (3 AZs), RDS Multi-AZ, MSK Multi-AZ
2. **Active-active regions**: MSK Replicator for cross-region replication
3. **Zero-downtime deploys**: Rolling updates with `maxUnavailable: 0`
4. **Circuit breakers**: Fallback to queue-and-retry
5. **DLQs**: Failed Kafka messages preserved for retry
6. **Health checks**: ALB → readiness probes → Actuator → DB validation

---

## Section 2: Event-Driven Architecture

### Q6: A payment is stuck in "INITIATED" status. How do you troubleshoot?

**Answer**:
1. Check payment-execution consumer logs: `kubectl logs -l app=payment-execution`
2. Check Kafka consumer lag — is the consumer falling behind?
3. Verify message exists in `payment-initiated` topic
4. Check for deserialization errors (schema mismatch)
5. Check circuit breaker state via `/actuator/circuitbreakers`
6. Check database connectivity from execution service
7. Check resource constraints: `kubectl top pod`

Root causes by frequency: (1) Consumer rebalancing, (2) DB connection exhaustion, (3) Schema mismatch, (4) Open circuit breaker.

### Q7: How do you handle poison pill messages in Kafka?

**Answer**:
1. `ErrorHandlingDeserializer` — invalid messages logged and skipped
2. `DefaultErrorHandler` with exponential backoff (1s, 2s, 4s, max 3 retries)
3. Dead Letter Topic (`<topic>.DLT`) after max retries
4. Schema validation before business logic
5. Alert on DLT message count > 0
6. Admin endpoint to replay messages from DLT

### Q8: Choreography vs orchestration — which did you choose?

**Answer**: **Choreography** (Kafka pub/sub) for the main payment flow because:
- 6 services process independently → enables parallelism
- No single point of failure (no orchestrator)
- Independent service development/deployment
- Natural fit for event sourcing

**Exception**: Step Functions (orchestration) for settlement workflow — strict sequential process requiring saga-pattern compensating transactions.

---

## Section 3: Database & Security

### Q9: How do you prevent double payments in a distributed system?

**Answer**: Defense in depth:
1. **Client**: Generate UUID idempotency key, send in `X-Idempotency-Key` header
2. **API Gateway**: 24-hour response cache by idempotency key
3. **Database**: `UNIQUE(idempotency_key, tenant_id)` constraint
4. **SQL**: `INSERT ... ON CONFLICT DO NOTHING RETURNING *`
5. **Kafka**: `enable.idempotence=true` on producer
6. **Consumer**: Check `status != 'COMPLETED'` before processing

### Q10: How do you ensure PCI DSS compliance?

**Answer**:
1. **Network**: Payment services in private subnets only
2. **Encryption at rest**: RDS + MSK with AWS KMS
3. **Encryption in transit**: TLS 1.3 everywhere, mTLS via service mesh
4. **Tokenization**: Never store raw card numbers
5. **Access control**: IRSA, K8s RBAC, DB RLS
6. **Audit logging**: CloudTrail + immutable S3 audit logs
7. **Data masking**: PAN masked in all logs: `**** **** **** 1234`

---

## Section 4: AWS & DevOps

### Q11: Why EKS over ECS?

**Answer**:
- **Multi-cloud portability** — financial services often require vendor risk mitigation
- **Service mesh** (Istio) for mTLS between payment services
- **Rich ecosystem**: Prometheus, Grafana, Jaeger, ArgoCD
- **Namespace-based tenant isolation**
- Trade-off: Higher complexity but more flexibility

### Q12: How would you implement CI/CD for this system?

**Answer**: `Push → Build → Test → Scan → Deploy`
1. Static analysis (SpotBugs, ESLint)
2. Unit tests (JUnit 5, React Testing Library)
3. Integration tests (Testcontainers with Kafka + PostgreSQL)
4. Security scan (Trivy, OWASP dependency-check)
5. Build & push multi-arch images to ECR
6. Canary deploy to staging, smoke tests
7. Blue/green to production with approval gate
8. Auto-rollback if error rate > 0.1% within 15 minutes

### Q13: How do you manage secrets in EKS?

**Answer**: AWS Secrets Manager → External Secrets Operator → K8s Secrets → Pod env vars. IRSA for pod-level IAM. Auto-rotation every 30 days.

---

## Section 5: Performance & Troubleshooting

### Q14: p99 latency spikes during peak hours. How do you diagnose?

**Answer**:
1. **Metrics**: CPU > 80%? Scale. Memory leak? GC pause > 200ms? Tune JVM
2. **Distributed tracing**: Which span is the bottleneck?
3. **DB queries**: `pg_stat_activity` for slow queries, `EXPLAIN ANALYZE`
4. **Kafka**: Producer batching, broker disk I/O
5. **Network**: Cross-AZ traffic, DNS resolution delays

Fix priority: (1) Missing DB index, (2) Connection pool size, (3) Producer batching, (4) Scale pods.

### Q15: How do you handle 10x traffic growth (Black Friday)?

**Answer**:
1. Pre-scale EKS nodes 2 days before
2. HPA on Kafka consumer lag (not just CPU)
3. Increase Kafka partitions proportionally
4. PgBouncer sidecar for DB connection pooling
5. Read replicas for status queries
6. API Gateway throttling per tenant
7. Priority queue — high-value payments first during overload

---

## Section 6: Kafka Deep Dive

### Q16: How do you handle Kafka broker failure without losing payments?

**Answer**:
- Replication factor = 3, `min.insync.replicas=2`
- Producer `acks=all`
- MSK Multi-AZ (3 AZs)
- `unclean.leader.election.enable=false`
- MSK Replicator for multi-region
- Manual offset commit after successful processing

### Q17: How do you partition Kafka topics for optimal processing?

**Answer**:
- Partition key: `tenantId + accountId` → ordering per account
- Start with 12 partitions (divisible by 3 AZs)
- Monitor per-partition `BytesInPerSec` for hot partitions
- Sticky partitioner (default Kafka 3.x) for better batching

---

## Section 7: Scenario-Based

### Q18: Payment processed twice, customer double-charged. Root cause analysis?

**Answer**:
1. Pull all events for `transactionId` across topics
2. Was message produced twice? (producer retry without idempotence)
3. Was it consumed twice? (consumer rebalance before offset commit)
4. Check DB for duplicate rows with same idempotency key

Fix: Enable idempotent producer + manual offset commit + DB unique constraint.

### Q19: MSK cluster approaching storage limits?

**Answer**:
- **Immediate**: Reduce retention on non-critical topics
- **Short-term**: Enable MSK tiered storage (old segments → S3), enable lz4 compression
- **Long-term**: MSK Serverless auto-scales, S3 Sink for archival

### Q20: How would you migrate from monolith to microservices without downtime?

**Answer**: Strangler Fig Pattern:
1. **Parallel Run**: Deploy new services alongside monolith, mirror events for comparison
2. **Shadow Traffic**: Send real traffic to both, compare results until < 0.01% mismatch
3. **Canary**: Route 1% → 5% → 25% → 50% → 100% with feature flags
4. **Cutover**: Keep monolith read-only 30 days, then decommission

### Q21: After deploying new risk service, fraud detection silently stops. How to debug?

**Answer**:
1. Check consumer group partition assignment
2. Verify message flow: test payment in `payment-initiated` but NOT in `risk-assessed`
3. Check schema evolution — field rename causes silent null deserialization
4. Check feature flags, thread starvation
Most likely: Schema mismatch. Fix with backward-compatible evolution + defaults.

---

## Section 8: Cost & DR

### Q22: Monthly bill is $50K. How to reduce by 40%?

**Answer**: Spot instances for non-critical workloads ($8K), RDS Reserved Instances ($6K), Graviton ARM instances ($4K), MSK right-sizing ($2K) = $20K/month = 40%.

### Q23: Describe a complete DR test procedure.

**Answer**:
1. Verify MSK Replicator lag < 100ms
2. Simulate primary region failure via Route53
3. Promote RDS read replica in secondary region
4. Verify services, submit 100 test payments
5. Measure RTO (target < 5 min), RPO (target < 1 second)
6. Document lessons learned, update runbook
