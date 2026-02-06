# Variables for CommSec Trading Platform Infrastructure

# ============================================
# General Configuration
# ============================================

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "commsec"
}

variable "environment" {
  description = "Environment name (e.g., production, staging, development)"
  type        = string
  default     = "production"
}

variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

# ============================================
# VPC Configuration
# ============================================

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnets"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Use a single NAT Gateway (cost saving for non-production)"
  type        = bool
  default     = false
}

# ============================================
# EKS Configuration
# ============================================

variable "eks_cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "commsec-cluster"
}

variable "eks_cluster_version" {
  description = "Kubernetes version for EKS cluster"
  type        = string
  default     = "1.34"
}

variable "eks_web_node_instance_types" {
  description = "Instance types for web tier node group"
  type        = list(string)
  default     = ["t3.medium"]
}

variable "eks_app_node_instance_types" {
  description = "Instance types for app tier node group"
  type        = list(string)
  default     = ["t3.large"]
}

variable "eks_web_node_desired_size" {
  description = "Desired number of nodes in web tier node group"
  type        = number
  default     = 3
}

variable "eks_web_node_min_size" {
  description = "Minimum number of nodes in web tier node group"
  type        = number
  default     = 3
}

variable "eks_web_node_max_size" {
  description = "Maximum number of nodes in web tier node group"
  type        = number
  default     = 10
}

variable "eks_app_node_desired_size" {
  description = "Desired number of nodes in app tier node group"
  type        = number
  default     = 3
}

variable "eks_app_node_min_size" {
  description = "Minimum number of nodes in app tier node group"
  type        = number
  default     = 3
}

variable "eks_app_node_max_size" {
  description = "Maximum number of nodes in app tier node group"
  type        = number
  default     = 10
}

# ============================================
# RDS Configuration
# ============================================

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.r6g.large"
}

variable "db_name" {
  description = "Name of the database"
  type        = string
  default     = "commsec"
}

variable "db_username" {
  description = "Master username for the database"
  type        = string
  default     = "commsec_admin"
  sensitive   = true
}

variable "db_password" {
  description = "Master password for the database"
  type        = string
  sensitive   = true
}

variable "db_allocated_storage" {
  description = "Allocated storage in GB"
  type        = number
  default     = 100
}

variable "db_max_allocated_storage" {
  description = "Maximum allocated storage in GB (for autoscaling)"
  type        = number
  default     = 1000
}

variable "db_backup_retention_period" {
  description = "Number of days to retain backups"
  type        = number
  default     = 35
}

variable "db_multi_az" {
  description = "Enable Multi-AZ deployment for RDS"
  type        = bool
  default     = true
}

# ============================================
# CloudFront Configuration
# ============================================

variable "enable_cloudfront" {
  description = "Enable CloudFront distribution"
  type        = bool
  default     = true
}

variable "cloudfront_price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_100"
}

# ============================================
# S3 Configuration
# ============================================

variable "s3_bucket_prefix" {
  description = "Prefix for S3 bucket names"
  type        = string
  default     = "commsec"
}

# ============================================
# Tags
# ============================================

variable "additional_tags" {
  description = "Additional tags for all resources"
  type        = map(string)
  default     = {}
}
