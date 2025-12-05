output "vpc_id" {
  description = "The ID of the VPC"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = module.vpc.public_subnets
}

output "rds_endpoint" {
  description = "The connection endpoint for the RDS instance"
  value       = module.db.db_instance_endpoint
}

output "redis_endpoint" {
  description = "The connection endpoint for the Redis cluster"
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "s3_bucket_name" {
  description = "The name of the S3 bucket"
  value       = aws_s3_bucket.media_bucket.id
}

output "ecr_repository_urls" {
  description = "Map of ECR repository URLs"
  value = {
    for k, v in aws_ecr_repository.repos : k => v.repository_url
  }
}
