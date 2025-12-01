# AWS Secrets Manager - Secrets Structure for OTA Travel Application
# This file defines the secrets structure required by the application

# Random password generation for secrets
resource "random_password" "database" {
  length           = 20
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "redis" {
  length           = 20
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "opensearch" {
  length           = 20
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "jwt_secret" {
  length  = 32
  special = false
}

# Database Credentials Secret (shared credentials for all service databases)
resource "aws_secretsmanager_secret" "database" {
  name        = "${var.project_name}/${var.environment}/database"
  description = "Database credentials for ${var.project_name} ${var.environment}"

  tags = {
    Name        = "${var.project_name}-${var.environment}-database-secret"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "database" {
  secret_id = aws_secretsmanager_secret.database.id
  secret_string = jsonencode({
    # Auth service database
    auth_host     = aws_db_instance.auth.endpoint
    auth_database = aws_db_instance.auth.db_name
    # Search service database
    search_host     = aws_db_instance.search.endpoint
    search_database = aws_db_instance.search.db_name
    # Booking service database
    booking_host     = aws_db_instance.booking.endpoint
    booking_database = aws_db_instance.booking.db_name
    # Payment service database
    payment_host     = aws_db_instance.payment.endpoint
    payment_database = aws_db_instance.payment.db_name
    # Shared credentials
    port     = 5432
    username = var.db_username
    password = random_password.database.result
    engine   = "postgres"
    ssl      = true
  })
}

# Redis Credentials Secret
resource "aws_secretsmanager_secret" "redis" {
  name        = "${var.project_name}/${var.environment}/redis"
  description = "Redis credentials for ${var.project_name} ${var.environment}"

  tags = {
    Name        = "${var.project_name}-${var.environment}-redis-secret"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "redis" {
  secret_id = aws_secretsmanager_secret.redis.id
  secret_string = jsonencode({
    host         = aws_elasticache_cluster.redis.cache_nodes[0].address
    port         = 6379
    password     = random_password.redis.result
    ssl          = true
    cluster_mode = false
  })
}

# JWT Secret
resource "aws_secretsmanager_secret" "jwt" {
  name        = "${var.project_name}/${var.environment}/jwt"
  description = "JWT signing secret for ${var.project_name} ${var.environment}"

  tags = {
    Name        = "${var.project_name}-${var.environment}-jwt-secret"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({
    secret                  = base64encode(random_password.jwt_secret.result)
    algorithm               = "HS256"
    expiration_hours        = 24
    refresh_expiration_days = 7
  })
}

# Stripe API Keys Secret
resource "aws_secretsmanager_secret" "stripe" {
  name        = "${var.project_name}/${var.environment}/stripe"
  description = "Stripe API credentials for ${var.project_name} ${var.environment}"

  tags = {
    Name        = "${var.project_name}-${var.environment}-stripe-secret"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "stripe" {
  secret_id = aws_secretsmanager_secret.stripe.id
  secret_string = jsonencode({
    secret_key     = var.stripe_secret_key
    webhook_secret = var.stripe_webhook_secret
    api_version    = "2023-10-16"
  })

  lifecycle {
    ignore_changes = [secret_string]
  }
}

# OpenSearch Credentials Secret
resource "aws_secretsmanager_secret" "opensearch" {
  name        = "${var.project_name}/${var.environment}/opensearch"
  description = "OpenSearch credentials for ${var.project_name} ${var.environment}"

  tags = {
    Name        = "${var.project_name}-${var.environment}-opensearch-secret"
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "opensearch" {
  secret_id = aws_secretsmanager_secret.opensearch.id
  secret_string = jsonencode({
    host     = "https://${aws_opensearch_domain.search.endpoint}"
    port     = 443
    username = "admin"
    password = random_password.opensearch.result
    ssl      = true
  })
}

# Outputs for reference
output "secrets_arns" {
  description = "ARNs of created secrets"
  value = {
    database   = aws_secretsmanager_secret.database.arn
    redis      = aws_secretsmanager_secret.redis.arn
    jwt        = aws_secretsmanager_secret.jwt.arn
    stripe     = aws_secretsmanager_secret.stripe.arn
    opensearch = aws_secretsmanager_secret.opensearch.arn
  }
}

output "secrets_names" {
  description = "Names of created secrets"
  value = {
    database   = aws_secretsmanager_secret.database.name
    redis      = aws_secretsmanager_secret.redis.name
    jwt        = aws_secretsmanager_secret.jwt.name
    stripe     = aws_secretsmanager_secret.stripe.name
    opensearch = aws_secretsmanager_secret.opensearch.name
  }
}
