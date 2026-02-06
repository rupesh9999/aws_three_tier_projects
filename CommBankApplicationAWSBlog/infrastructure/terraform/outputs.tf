# Terraform Outputs for CommSec Trading Platform

# ============================================
# VPC Outputs
# ============================================

output "vpc_id" {
  description = "The ID of the VPC"
  value       = module.vpc.vpc_id
}

output "vpc_cidr_block" {
  description = "The CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = module.vpc.public_subnets
}

output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = module.vpc.private_subnets
}

output "database_subnets" {
  description = "List of IDs of database subnets"
  value       = module.vpc.database_subnets
}

output "nat_gateway_ids" {
  description = "List of NAT Gateway IDs"
  value       = module.vpc.natgw_ids
}

# ============================================
# EKS Outputs
# ============================================

output "eks_cluster_name" {
  description = "The name of the EKS cluster"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "Endpoint for EKS control plane"
  value       = module.eks.cluster_endpoint
}

output "eks_cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data required to communicate with the cluster"
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

output "eks_cluster_oidc_issuer_url" {
  description = "The URL on the EKS cluster for the OpenID Connect identity provider"
  value       = module.eks.cluster_oidc_issuer_url
}

output "eks_cluster_security_group_id" {
  description = "Security group ID attached to the EKS cluster"
  value       = module.eks.cluster_security_group_id
}

output "eks_node_security_group_id" {
  description = "Security group ID attached to the EKS nodes"
  value       = module.eks.node_security_group_id
}

output "eks_update_kubeconfig_command" {
  description = "Command to update kubeconfig for the EKS cluster"
  value       = "aws eks update-kubeconfig --name ${module.eks.cluster_name} --region ${var.aws_region}"
}

# ============================================
# RDS Outputs
# ============================================

output "rds_endpoint" {
  description = "The connection endpoint for the RDS instance"
  value       = module.rds.db_instance_endpoint
}

output "rds_address" {
  description = "The hostname of the RDS instance"
  value       = module.rds.db_instance_address
}

output "rds_port" {
  description = "The database port"
  value       = module.rds.db_instance_port
}

output "rds_database_name" {
  description = "The database name"
  value       = module.rds.db_instance_name
}

output "rds_master_username" {
  description = "The master username for the database"
  value       = module.rds.db_instance_username
  sensitive   = true
}

output "db_credentials_secret_arn" {
  description = "ARN of the Secrets Manager secret containing database credentials"
  value       = aws_secretsmanager_secret.db_credentials.arn
}

# ============================================
# S3 Outputs
# ============================================

output "s3_binaries_bucket" {
  description = "Name of the S3 bucket for application binaries"
  value       = aws_s3_bucket.binaries.id
}

output "s3_binaries_bucket_arn" {
  description = "ARN of the S3 bucket for application binaries"
  value       = aws_s3_bucket.binaries.arn
}

output "s3_static_assets_bucket" {
  description = "Name of the S3 bucket for static assets"
  value       = aws_s3_bucket.static_assets.id
}

output "s3_static_assets_bucket_arn" {
  description = "ARN of the S3 bucket for static assets"
  value       = aws_s3_bucket.static_assets.arn
}

# ============================================
# CloudFront Outputs
# ============================================

output "cloudfront_distribution_id" {
  description = "The ID of the CloudFront distribution"
  value       = var.enable_cloudfront ? aws_cloudfront_distribution.main[0].id : null
}

output "cloudfront_domain_name" {
  description = "The domain name of the CloudFront distribution"
  value       = var.enable_cloudfront ? aws_cloudfront_distribution.main[0].domain_name : null
}

output "cloudfront_hosted_zone_id" {
  description = "The CloudFront Route 53 zone ID"
  value       = var.enable_cloudfront ? aws_cloudfront_distribution.main[0].hosted_zone_id : null
}

# ============================================
# ECR Outputs
# ============================================

output "ecr_frontend_repository_url" {
  description = "URL of the frontend ECR repository"
  value       = aws_ecr_repository.frontend.repository_url
}

output "ecr_trading_service_repository_url" {
  description = "URL of the trading service ECR repository"
  value       = aws_ecr_repository.trading_service.repository_url
}

output "ecr_portfolio_service_repository_url" {
  description = "URL of the portfolio service ECR repository"
  value       = aws_ecr_repository.portfolio_service.repository_url
}

output "ecr_market_data_service_repository_url" {
  description = "URL of the market data service ECR repository"
  value       = aws_ecr_repository.market_data_service.repository_url
}

output "ecr_api_gateway_repository_url" {
  description = "URL of the API gateway ECR repository"
  value       = aws_ecr_repository.api_gateway.repository_url
}

# ============================================
# IAM Outputs
# ============================================

output "aws_lb_controller_role_arn" {
  description = "ARN of the IAM role for AWS Load Balancer Controller"
  value       = module.aws_lb_controller_irsa_role.iam_role_arn
}

output "cluster_autoscaler_role_arn" {
  description = "ARN of the IAM role for Cluster Autoscaler"
  value       = module.cluster_autoscaler_irsa_role.iam_role_arn
}

# ============================================
# Summary
# ============================================

output "deployment_summary" {
  description = "Summary of all deployed resources"
  value       = <<-EOT

    ╔══════════════════════════════════════════════════════════════╗
    ║            CommSec Trading Platform - Deployment Summary      ║
    ╠══════════════════════════════════════════════════════════════╣
    ║                                                               ║
    ║  VPC ID:              ${module.vpc.vpc_id}
    ║  EKS Cluster:         ${module.eks.cluster_name}
    ║  EKS Version:         ${var.eks_cluster_version}
    ║  RDS Endpoint:        ${module.rds.db_instance_address}
    ║  CloudFront Domain:   ${var.enable_cloudfront ? aws_cloudfront_distribution.main[0].domain_name : "Not enabled"}
    ║                                                               ║
    ║  Next Steps:                                                  ║
    ║  1. Run: ${module.eks.cluster_name} --region ${var.aws_region}
    ║  2. Deploy Kubernetes manifests                               ║
    ║  3. Build and push Docker images to ECR                       ║
    ║                                                               ║
    ╚══════════════════════════════════════════════════════════════╝

  EOT
}
