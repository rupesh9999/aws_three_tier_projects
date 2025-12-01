# ============================================================================
# Terraform Main Configuration
# FinTech Mobile Banking Application - AWS Infrastructure
# ============================================================================

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.24"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.12"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  backend "s3" {
    # Backend configuration will be provided via backend config file
    # terraform init -backend-config=backend.hcl
  }
}

# ============================================================================
# Provider Configuration
# ============================================================================
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.common_tags
  }
}

provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_ca_certificate)
  
  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name]
  }
}

provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_ca_certificate)
    
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name]
    }
  }
}

# ============================================================================
# Data Sources
# ============================================================================
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}
data "aws_availability_zones" "available" {
  state = "available"
}

# ============================================================================
# Local Variables
# ============================================================================
locals {
  name_prefix = "${var.project_name}-${var.environment}"
  
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
    Owner       = var.owner
    CostCenter  = var.cost_center
  }

  azs = slice(data.aws_availability_zones.available.names, 0, 3)
}

# ============================================================================
# VPC Module
# ============================================================================
module "vpc" {
  source = "./modules/vpc"

  name_prefix         = local.name_prefix
  vpc_cidr            = var.vpc_cidr
  availability_zones  = local.azs
  environment         = var.environment
  enable_nat_gateway  = true
  single_nat_gateway  = var.environment == "dev" ? true : false
  enable_vpn_gateway  = false

  tags = local.common_tags
}

# ============================================================================
# ECR Module
# ============================================================================
module "ecr" {
  source = "./modules/ecr"

  name_prefix  = local.name_prefix
  repositories = ["frontend", "backend", "migrations"]
  
  tags = local.common_tags
}

# ============================================================================
# Secrets Manager Module
# ============================================================================
module "secrets_manager" {
  source = "./modules/secrets-manager"

  name_prefix = local.name_prefix
  environment = var.environment
  
  tags = local.common_tags
}

# ============================================================================
# RDS Module (PostgreSQL)
# ============================================================================
module "rds" {
  source = "./modules/rds"

  name_prefix            = local.name_prefix
  vpc_id                 = module.vpc.vpc_id
  subnet_ids             = module.vpc.private_subnet_ids
  allowed_security_groups = [module.eks.node_security_group_id]
  
  instance_class         = var.rds_instance_class
  allocated_storage      = var.rds_allocated_storage
  max_allocated_storage  = var.rds_max_allocated_storage
  database_name          = "banking"
  multi_az               = var.environment == "prod" ? true : false
  
  backup_retention_period = var.environment == "prod" ? 30 : 7
  deletion_protection     = var.environment == "prod" ? true : false
  
  secrets_manager_arn    = module.secrets_manager.db_credentials_arn

  tags = local.common_tags
}

# ============================================================================
# ElastiCache Module (Redis)
# ============================================================================
module "elasticache" {
  source = "./modules/elasticache"

  name_prefix             = local.name_prefix
  vpc_id                  = module.vpc.vpc_id
  subnet_ids              = module.vpc.private_subnet_ids
  allowed_security_groups = [module.eks.node_security_group_id]
  
  node_type               = var.redis_node_type
  num_cache_nodes         = var.environment == "prod" ? 3 : 1
  
  tags = local.common_tags
}

# ============================================================================
# EKS Module
# ============================================================================
module "eks" {
  source = "./modules/eks"

  name_prefix        = local.name_prefix
  vpc_id             = module.vpc.vpc_id
  subnet_ids         = module.vpc.private_subnet_ids
  
  kubernetes_version = var.eks_kubernetes_version
  
  node_groups = {
    general = {
      desired_size   = var.eks_desired_nodes
      min_size       = var.eks_min_nodes
      max_size       = var.eks_max_nodes
      instance_types = var.eks_instance_types
      capacity_type  = var.environment == "prod" ? "ON_DEMAND" : "SPOT"
    }
  }

  enable_cluster_autoscaler = true
  enable_metrics_server     = true
  enable_aws_load_balancer_controller = true

  tags = local.common_tags
}

# ============================================================================
# S3 Module
# ============================================================================
module "s3" {
  source = "./modules/s3"

  name_prefix = local.name_prefix
  environment = var.environment
  
  buckets = {
    documents = {
      versioning_enabled = true
      encryption_enabled = true
    }
    logs = {
      versioning_enabled = false
      encryption_enabled = true
    }
    static_assets = {
      versioning_enabled = false
      encryption_enabled = true
      public_access      = false
    }
  }

  tags = local.common_tags
}

# ============================================================================
# CloudFront Module
# ============================================================================
module "cloudfront" {
  source = "./modules/cloudfront"

  name_prefix       = local.name_prefix
  s3_bucket_domain  = module.s3.static_assets_bucket_domain
  s3_bucket_arn     = module.s3.static_assets_bucket_arn
  acm_certificate_arn = var.acm_certificate_arn
  domain_aliases    = var.cloudfront_domain_aliases
  waf_acl_arn       = module.waf.web_acl_arn

  tags = local.common_tags
}

# ============================================================================
# SQS Module
# ============================================================================
module "sqs" {
  source = "./modules/sqs"

  name_prefix = local.name_prefix
  
  queues = {
    transactions = {
      delay_seconds          = 0
      message_retention      = 1209600  # 14 days
      visibility_timeout     = 30
      enable_dlq             = true
    }
    notifications = {
      delay_seconds          = 0
      message_retention      = 604800   # 7 days
      visibility_timeout     = 30
      enable_dlq             = true
    }
    audit = {
      delay_seconds          = 0
      message_retention      = 1209600
      visibility_timeout     = 60
      enable_dlq             = true
    }
  }

  tags = local.common_tags
}

# ============================================================================
# WAF Module
# ============================================================================
module "waf" {
  source = "./modules/waf"

  name_prefix = local.name_prefix
  
  rate_limit     = 2000
  enable_logging = true
  log_bucket_arn = module.s3.logs_bucket_arn

  tags = local.common_tags
}

# ============================================================================
# API Gateway Module (Optional - for Lambda integration)
# ============================================================================
# module "api_gateway" {
#   source = "./modules/api-gateway"
#   
#   name_prefix = local.name_prefix
#   
#   tags = local.common_tags
# }
