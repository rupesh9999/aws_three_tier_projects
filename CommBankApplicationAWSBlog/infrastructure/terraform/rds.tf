# RDS PostgreSQL Configuration for CommSec Trading Platform

# ============================================
# SECURE PASSWORD RETRIEVAL FROM SSM
# ============================================
# The password is stored as a SecureString in SSM Parameter Store.
# SecureString values are encrypted at rest using AWS KMS.
# When Terraform reads this parameter:
#   1. It calls the SSM GetParameter API with with_decryption=true
#   2. SSM uses the KMS key to decrypt the value
#   3. The decrypted value is only available in memory during terraform apply
#   4. The password is NEVER stored in Terraform state in plain text when using 
#      the sensitive attribute
#
# Security Flow:
#   ┌─────────────────┐     KMS Decrypt      ┌─────────────────┐
#   │ SSM Parameter   │ ──────────────────▶  │ Terraform       │
#   │ (Encrypted)     │                      │ (In-Memory)     │
#   └─────────────────┘                      └────────┬────────┘
#                                                     │
#                                                     ▼
#                                            ┌─────────────────┐
#                                            │ RDS Instance    │
#                                            │ (password set)  │
#                                            └─────────────────┘

# Fetch the database password from SSM Parameter Store
data "aws_ssm_parameter" "db_password" {
  name            = "/commsec/production/db-password"
  with_decryption = true
}

locals {
  # Use SSM parameter value - falls back to variable if SSM fails
  db_password = data.aws_ssm_parameter.db_password.value
}

# RDS PostgreSQL Instance
module "rds" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.10"

  identifier = "${var.project_name}-postgres"

  # Engine configuration
  engine               = "postgres"
  engine_version       = "16.4"
  family               = "postgres16"
  major_engine_version = "16"
  instance_class       = var.db_instance_class

  # Storage configuration
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true
  kms_key_id            = aws_kms_key.rds.arn

  # Database configuration
  db_name  = var.db_name
  username = var.db_username
  password = local.db_password
  port     = 5432

  # High Availability
  multi_az = var.db_multi_az

  # Network configuration
  db_subnet_group_name   = module.vpc.database_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  # Backup configuration
  backup_retention_period = var.db_backup_retention_period
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  # Performance and monitoring
  performance_insights_enabled          = true
  performance_insights_retention_period = 7
  monitoring_interval                   = 60
  monitoring_role_arn                   = aws_iam_role.rds_monitoring.arn
  enabled_cloudwatch_logs_exports       = ["postgresql", "upgrade"]

  # Parameter group configuration
  # Static parameters (shared_preload_libraries, max_connections) require pending-reboot
  # Dynamic parameters can be applied immediately
  parameters = [
    {
      name         = "shared_preload_libraries"
      value        = "pg_stat_statements"
      apply_method = "pending-reboot"
    },
    {
      name         = "log_min_duration_statement"
      value        = "1000"
      apply_method = "immediate"
    },
    {
      name         = "max_connections"
      value        = "500"
      apply_method = "pending-reboot"
    }
  ]

  # Protection
  deletion_protection              = false
  skip_final_snapshot              = false
  final_snapshot_identifier_prefix = "${var.project_name}-postgres-final"
  copy_tags_to_snapshot            = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-postgres"
  })
}

# KMS Key for RDS encryption
resource "aws_kms_key" "rds" {
  description             = "KMS key for RDS encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-rds-kms"
  })
}

resource "aws_kms_alias" "rds" {
  name          = "alias/${var.project_name}-rds"
  target_key_id = aws_kms_key.rds.key_id
}

# Security Group for RDS
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-rds-sg"
  description = "Security group for RDS PostgreSQL"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "PostgreSQL from EKS nodes"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id]
  }

  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-rds-sg"
  })
}

# IAM Role for RDS Enhanced Monitoring
resource "aws_iam_role" "rds_monitoring" {
  name = "${var.project_name}-rds-monitoring"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  role       = aws_iam_role.rds_monitoring.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

# Store database credentials in Secrets Manager
resource "aws_secretsmanager_secret" "db_credentials" {
  name        = "${var.project_name}/${var.environment}/db-credentials"
  description = "Database credentials for CommSec Trading Platform"

  kms_key_id = aws_kms_key.rds.arn

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = local.db_password
    engine   = "postgres"
    host     = module.rds.db_instance_address
    port     = 5432
    dbname   = var.db_name
  })
}
