# ============================================================================
# Terraform Outputs
# FinTech Mobile Banking Application
# ============================================================================

# ============================================================================
# VPC Outputs
# ============================================================================
output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = module.vpc.private_subnet_ids
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = module.vpc.public_subnet_ids
}

# ============================================================================
# EKS Outputs
# ============================================================================
output "eks_cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "EKS cluster endpoint"
  value       = module.eks.cluster_endpoint
  sensitive   = true
}

output "eks_cluster_arn" {
  description = "EKS cluster ARN"
  value       = module.eks.cluster_arn
}

output "eks_oidc_provider_arn" {
  description = "EKS OIDC provider ARN"
  value       = module.eks.oidc_provider_arn
}

# ============================================================================
# RDS Outputs
# ============================================================================
output "rds_endpoint" {
  description = "RDS database endpoint"
  value       = module.rds.endpoint
  sensitive   = true
}

output "rds_port" {
  description = "RDS database port"
  value       = module.rds.port
}

# ============================================================================
# ElastiCache Outputs
# ============================================================================
output "redis_endpoint" {
  description = "Redis cluster endpoint"
  value       = module.elasticache.endpoint
  sensitive   = true
}

output "redis_port" {
  description = "Redis cluster port"
  value       = module.elasticache.port
}

# ============================================================================
# ECR Outputs
# ============================================================================
output "ecr_repository_urls" {
  description = "ECR repository URLs"
  value       = module.ecr.repository_urls
}

# ============================================================================
# S3 Outputs
# ============================================================================
output "s3_bucket_names" {
  description = "S3 bucket names"
  value = {
    documents     = module.s3.documents_bucket_name
    logs          = module.s3.logs_bucket_name
    static_assets = module.s3.static_assets_bucket_name
  }
}

# ============================================================================
# CloudFront Outputs
# ============================================================================
output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  value       = module.cloudfront.distribution_id
}

output "cloudfront_domain_name" {
  description = "CloudFront distribution domain name"
  value       = module.cloudfront.domain_name
}

# ============================================================================
# SQS Outputs
# ============================================================================
output "sqs_queue_urls" {
  description = "SQS queue URLs"
  value       = module.sqs.queue_urls
}

output "sqs_queue_arns" {
  description = "SQS queue ARNs"
  value       = module.sqs.queue_arns
}

# ============================================================================
# WAF Outputs
# ============================================================================
output "waf_web_acl_arn" {
  description = "WAF Web ACL ARN"
  value       = module.waf.web_acl_arn
}

# ============================================================================
# Secrets Manager Outputs
# ============================================================================
output "secrets_manager_arns" {
  description = "Secrets Manager secret ARNs"
  value = {
    db_credentials = module.secrets_manager.db_credentials_arn
    jwt_secret     = module.secrets_manager.jwt_secret_arn
  }
  sensitive = true
}
