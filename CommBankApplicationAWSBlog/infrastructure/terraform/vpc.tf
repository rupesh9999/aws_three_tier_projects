# VPC Configuration for CommSec Trading Platform
# Multi-AZ VPC with public, private, and database subnets

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.16"

  name = "${var.project_name}-vpc"
  cidr = var.vpc_cidr

  azs = local.azs

  # Public subnets for ALB and NAT Gateways
  public_subnets = [
    cidrsubnet(var.vpc_cidr, 8, 1), # 10.0.1.0/24
    cidrsubnet(var.vpc_cidr, 8, 2), # 10.0.2.0/24
    cidrsubnet(var.vpc_cidr, 8, 3)  # 10.0.3.0/24
  ]

  # Private subnets for EKS nodes (web and app tier)
  private_subnets = [
    cidrsubnet(var.vpc_cidr, 8, 11), # 10.0.11.0/24
    cidrsubnet(var.vpc_cidr, 8, 12), # 10.0.12.0/24
    cidrsubnet(var.vpc_cidr, 8, 13)  # 10.0.13.0/24
  ]

  # Database subnets (isolated, no internet access)
  database_subnets = [
    cidrsubnet(var.vpc_cidr, 8, 21), # 10.0.21.0/24
    cidrsubnet(var.vpc_cidr, 8, 22), # 10.0.22.0/24
    cidrsubnet(var.vpc_cidr, 8, 23)  # 10.0.23.0/24
  ]

  # NAT Gateway configuration
  enable_nat_gateway     = var.enable_nat_gateway
  single_nat_gateway     = var.single_nat_gateway
  one_nat_gateway_per_az = !var.single_nat_gateway

  # DNS settings
  enable_dns_hostnames = true
  enable_dns_support   = true

  # Database subnet group
  create_database_subnet_group       = true
  create_database_subnet_route_table = true
  database_subnet_group_name         = "${var.project_name}-db-subnet-group"

  # VPC Flow Logs
  enable_flow_log                      = true
  create_flow_log_cloudwatch_log_group = true
  create_flow_log_cloudwatch_iam_role  = true
  flow_log_max_aggregation_interval    = 60

  # Tags for EKS subnet discovery
  public_subnet_tags = {
    "kubernetes.io/role/elb"                        = 1
    "kubernetes.io/cluster/${var.eks_cluster_name}" = "shared"
    "Tier"                                          = "Public"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"               = 1
    "kubernetes.io/cluster/${var.eks_cluster_name}" = "shared"
    "Tier"                                          = "Private"
  }

  database_subnet_tags = {
    "Tier" = "Database"
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-vpc"
  })
}

# VPC Endpoints for AWS Services (reduces data transfer costs and improves security)
module "vpc_endpoints" {
  source  = "terraform-aws-modules/vpc/aws//modules/vpc-endpoints"
  version = "~> 5.16"

  vpc_id = module.vpc.vpc_id

  endpoints = {
    s3 = {
      service         = "s3"
      service_type    = "Gateway"
      route_table_ids = concat(module.vpc.private_route_table_ids, module.vpc.database_route_table_ids)
      tags            = { Name = "${var.project_name}-s3-endpoint" }
    }

    ecr_api = {
      service             = "ecr.api"
      private_dns_enabled = true
      subnet_ids          = module.vpc.private_subnets
      security_group_ids  = [aws_security_group.vpc_endpoints.id]
      tags                = { Name = "${var.project_name}-ecr-api-endpoint" }
    }

    ecr_dkr = {
      service             = "ecr.dkr"
      private_dns_enabled = true
      subnet_ids          = module.vpc.private_subnets
      security_group_ids  = [aws_security_group.vpc_endpoints.id]
      tags                = { Name = "${var.project_name}-ecr-dkr-endpoint" }
    }

    logs = {
      service             = "logs"
      private_dns_enabled = true
      subnet_ids          = module.vpc.private_subnets
      security_group_ids  = [aws_security_group.vpc_endpoints.id]
      tags                = { Name = "${var.project_name}-logs-endpoint" }
    }

    sts = {
      service             = "sts"
      private_dns_enabled = true
      subnet_ids          = module.vpc.private_subnets
      security_group_ids  = [aws_security_group.vpc_endpoints.id]
      tags                = { Name = "${var.project_name}-sts-endpoint" }
    }

    ssm = {
      service             = "ssm"
      private_dns_enabled = true
      subnet_ids          = module.vpc.private_subnets
      security_group_ids  = [aws_security_group.vpc_endpoints.id]
      tags                = { Name = "${var.project_name}-ssm-endpoint" }
    }
  }

  tags = local.common_tags
}

# Security Group for VPC Endpoints
resource "aws_security_group" "vpc_endpoints" {
  name        = "${var.project_name}-vpc-endpoints-sg"
  description = "Security group for VPC endpoints"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "HTTPS from VPC"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    description = "All traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-vpc-endpoints-sg"
  })
}
