output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "Private subnet IDs"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "Public subnet IDs"
  value       = module.vpc.public_subnets
}

output "eks_cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "EKS cluster endpoint"
  value       = module.eks.cluster_endpoint
}

output "eks_cluster_arn" {
  description = "EKS cluster ARN"
  value       = module.eks.cluster_arn
}

output "ecr_repository_urls" {
  description = "ECR repository URLs"
  value = {
    frontend        = aws_ecr_repository.frontend.repository_url
    auth_service    = aws_ecr_repository.auth_service.repository_url
    search_service  = aws_ecr_repository.search_service.repository_url
    booking_service = aws_ecr_repository.booking_service.repository_url
    payment_service = aws_ecr_repository.payment_service.repository_url
    cart_service    = aws_ecr_repository.cart_service.repository_url
  }
}

output "rds_endpoints" {
  description = "RDS database endpoints"
  value = {
    auth    = aws_db_instance.auth.endpoint
    search  = aws_db_instance.search.endpoint
    booking = aws_db_instance.booking.endpoint
    payment = aws_db_instance.payment.endpoint
  }
}

output "redis_endpoint" {
  description = "ElastiCache Redis endpoint"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
}

output "opensearch_endpoint" {
  description = "OpenSearch endpoint"
  value       = aws_opensearch_domain.search.endpoint
}

output "s3_bucket_name" {
  description = "S3 bucket name for static assets"
  value       = aws_s3_bucket.assets.id
}

output "cloudfront_distribution_domain" {
  description = "CloudFront distribution domain (empty if CloudFront is disabled)"
  value       = var.enable_cloudfront ? aws_cloudfront_distribution.frontend[0].domain_name : ""
}

output "s3_website_endpoint" {
  description = "S3 website endpoint (when CloudFront is disabled)"
  value       = var.enable_cloudfront ? "" : aws_s3_bucket_website_configuration.assets[0].website_endpoint
}

output "api_gateway_url" {
  description = "API Gateway URL"
  value       = aws_apigatewayv2_api.main.api_endpoint
}
