# ============================================================================
# SQS Module
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
# KMS Key for SQS Encryption
# ============================================================================
resource "aws_kms_key" "sqs" {
  description             = "KMS key for SQS queue encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = var.tags
}

resource "aws_kms_alias" "sqs" {
  name          = "alias/${var.name_prefix}-sqs"
  target_key_id = aws_kms_key.sqs.key_id
}

# ============================================================================
# Transaction Processing Queue
# ============================================================================
resource "aws_sqs_queue" "transactions_dlq" {
  name                      = "${var.name_prefix}-transactions-dlq"
  message_retention_seconds = 1209600 # 14 days
  kms_master_key_id         = aws_kms_key.sqs.id

  tags = var.tags
}

resource "aws_sqs_queue" "transactions" {
  name                       = "${var.name_prefix}-transactions"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 345600 # 4 days
  receive_wait_time_seconds  = 10
  visibility_timeout_seconds = 60
  kms_master_key_id          = aws_kms_key.sqs.id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.transactions_dlq.arn
    maxReceiveCount     = 3
  })

  tags = var.tags
}

# ============================================================================
# Notification Queue
# ============================================================================
resource "aws_sqs_queue" "notifications_dlq" {
  name                      = "${var.name_prefix}-notifications-dlq"
  message_retention_seconds = 1209600
  kms_master_key_id         = aws_kms_key.sqs.id

  tags = var.tags
}

resource "aws_sqs_queue" "notifications" {
  name                       = "${var.name_prefix}-notifications"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 86400 # 1 day
  receive_wait_time_seconds  = 10
  visibility_timeout_seconds = 30
  kms_master_key_id          = aws_kms_key.sqs.id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notifications_dlq.arn
    maxReceiveCount     = 3
  })

  tags = var.tags
}

# ============================================================================
# Audit Log Queue
# ============================================================================
resource "aws_sqs_queue" "audit_logs_dlq" {
  name                      = "${var.name_prefix}-audit-logs-dlq"
  message_retention_seconds = 1209600
  kms_master_key_id         = aws_kms_key.sqs.id

  tags = var.tags
}

resource "aws_sqs_queue" "audit_logs" {
  name                       = "${var.name_prefix}-audit-logs"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 604800 # 7 days
  receive_wait_time_seconds  = 10
  visibility_timeout_seconds = 60
  kms_master_key_id          = aws_kms_key.sqs.id

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.audit_logs_dlq.arn
    maxReceiveCount     = 5
  })

  tags = var.tags
}

# ============================================================================
# SQS Queue Policy
# ============================================================================
resource "aws_sqs_queue_policy" "transactions" {
  queue_url = aws_sqs_queue.transactions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowEKSAccess"
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.transactions.arn
      }
    ]
  })
}

# ============================================================================
# CloudWatch Alarms for DLQ Monitoring
# ============================================================================
resource "aws_cloudwatch_metric_alarm" "transactions_dlq" {
  alarm_name          = "${var.name_prefix}-transactions-dlq-alarm"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Sum"
  threshold           = 1
  alarm_description   = "Alert when messages appear in transaction DLQ"

  dimensions = {
    QueueName = aws_sqs_queue.transactions_dlq.name
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "notifications_dlq" {
  alarm_name          = "${var.name_prefix}-notifications-dlq-alarm"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Sum"
  threshold           = 5
  alarm_description   = "Alert when messages appear in notifications DLQ"

  dimensions = {
    QueueName = aws_sqs_queue.notifications_dlq.name
  }

  tags = var.tags
}

# ============================================================================
# Outputs
# ============================================================================
output "transactions_queue_url" {
  description = "Transactions queue URL"
  value       = aws_sqs_queue.transactions.url
}

output "transactions_queue_arn" {
  description = "Transactions queue ARN"
  value       = aws_sqs_queue.transactions.arn
}

output "notifications_queue_url" {
  description = "Notifications queue URL"
  value       = aws_sqs_queue.notifications.url
}

output "notifications_queue_arn" {
  description = "Notifications queue ARN"
  value       = aws_sqs_queue.notifications.arn
}

output "audit_logs_queue_url" {
  description = "Audit logs queue URL"
  value       = aws_sqs_queue.audit_logs.url
}

output "audit_logs_queue_arn" {
  description = "Audit logs queue ARN"
  value       = aws_sqs_queue.audit_logs.arn
}
