# ============================================================================
# Secrets Manager Module
# ============================================================================

variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "tags" {
  description = "Tags for resources"
  type        = map(string)
  default     = {}
}

# ============================================================================
# KMS Key for Secrets
# ============================================================================
resource "aws_kms_key" "secrets" {
  description             = "KMS key for Secrets Manager encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = var.tags
}

resource "aws_kms_alias" "secrets" {
  name          = "alias/${var.name_prefix}-secrets"
  target_key_id = aws_kms_key.secrets.key_id
}

# ============================================================================
# Database Credentials Secret
# ============================================================================
resource "aws_secretsmanager_secret" "database" {
  name       = "${var.name_prefix}/database/${var.environment}"
  kms_key_id = aws_kms_key.secrets.arn

  tags = var.tags
}

# ============================================================================
# JWT Secret
# ============================================================================
resource "random_password" "jwt_secret" {
  length  = 64
  special = false
}

resource "aws_secretsmanager_secret" "jwt" {
  name       = "${var.name_prefix}/jwt/${var.environment}"
  kms_key_id = aws_kms_key.secrets.arn

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({
    secret     = random_password.jwt_secret.result
    expiration = "3600"
    issuer     = "${var.name_prefix}.auth.local"
  })
}

# ============================================================================
# Redis Credentials Secret
# ============================================================================
resource "aws_secretsmanager_secret" "redis" {
  name       = "${var.name_prefix}/redis/${var.environment}"
  kms_key_id = aws_kms_key.secrets.arn

  tags = var.tags
}

# ============================================================================
# API Keys Secret
# ============================================================================
resource "aws_secretsmanager_secret" "api_keys" {
  name       = "${var.name_prefix}/api-keys/${var.environment}"
  kms_key_id = aws_kms_key.secrets.arn

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "api_keys" {
  secret_id = aws_secretsmanager_secret.api_keys.id
  secret_string = jsonencode({
    stripe_api_key     = "placeholder-replace-with-actual-key"
    plaid_client_id    = "placeholder-replace-with-actual-id"
    plaid_secret       = "placeholder-replace-with-actual-secret"
    twilio_account_sid = "placeholder-replace-with-actual-sid"
    twilio_auth_token  = "placeholder-replace-with-actual-token"
  })
}

# ============================================================================
# Encryption Keys Secret
# ============================================================================
resource "random_password" "encryption_key" {
  length  = 32
  special = false
}

resource "aws_secretsmanager_secret" "encryption" {
  name       = "${var.name_prefix}/encryption/${var.environment}"
  kms_key_id = aws_kms_key.secrets.arn

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "encryption" {
  secret_id = aws_secretsmanager_secret.encryption.id
  secret_string = jsonencode({
    aes_key = base64encode(random_password.encryption_key.result)
  })
}

# ============================================================================
# Secret Rotation Configuration (Example for RDS)
# ============================================================================
# Note: Actual rotation lambda needs to be created separately
# resource "aws_secretsmanager_secret_rotation" "database" {
#   secret_id           = aws_secretsmanager_secret.database.id
#   rotation_lambda_arn = "arn:aws:lambda:region:account-id:function:rotation-function"
#
#   rotation_rules {
#     automatically_after_days = 30
#   }
# }

# ============================================================================
# IAM Policy for EKS to Access Secrets
# ============================================================================
resource "aws_iam_policy" "secrets_access" {
  name        = "${var.name_prefix}-secrets-access"
  description = "Policy for EKS pods to access Secrets Manager"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = [
          aws_secretsmanager_secret.database.arn,
          aws_secretsmanager_secret.jwt.arn,
          aws_secretsmanager_secret.redis.arn,
          aws_secretsmanager_secret.api_keys.arn,
          aws_secretsmanager_secret.encryption.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt",
          "kms:DescribeKey"
        ]
        Resource = aws_kms_key.secrets.arn
      }
    ]
  })
}

# ============================================================================
# Outputs
# ============================================================================
output "database_secret_arn" {
  description = "Database credentials secret ARN"
  value       = aws_secretsmanager_secret.database.arn
}

output "jwt_secret_arn" {
  description = "JWT secret ARN"
  value       = aws_secretsmanager_secret.jwt.arn
}

output "redis_secret_arn" {
  description = "Redis credentials secret ARN"
  value       = aws_secretsmanager_secret.redis.arn
}

output "api_keys_secret_arn" {
  description = "API keys secret ARN"
  value       = aws_secretsmanager_secret.api_keys.arn
}

output "encryption_secret_arn" {
  description = "Encryption keys secret ARN"
  value       = aws_secretsmanager_secret.encryption.arn
}

output "secrets_access_policy_arn" {
  description = "IAM policy ARN for secrets access"
  value       = aws_iam_policy.secrets_access.arn
}

output "kms_key_arn" {
  description = "KMS key ARN for secrets encryption"
  value       = aws_kms_key.secrets.arn
}
