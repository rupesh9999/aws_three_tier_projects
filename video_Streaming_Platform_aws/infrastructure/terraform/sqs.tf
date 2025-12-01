# =============================================================================
# SQS Queues for Async Processing
# =============================================================================

# Transcoding Queue
resource "aws_sqs_queue" "transcoding" {
  name                       = "${local.name_prefix}-transcoding-queue"
  delay_seconds              = 0
  max_message_size           = 262144  # 256 KB
  message_retention_seconds  = 1209600 # 14 days
  receive_wait_time_seconds  = 20      # Long polling
  visibility_timeout_seconds = 900     # 15 minutes for processing

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.transcoding_dlq.arn
    maxReceiveCount     = 3
  })

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-transcoding-queue"
    Type = "Transcoding"
  })
}

resource "aws_sqs_queue" "transcoding_dlq" {
  name                       = "${local.name_prefix}-transcoding-dlq"
  message_retention_seconds  = 1209600 # 14 days

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-transcoding-dlq"
    Type = "DeadLetterQueue"
  })
}

# Notifications Queue
resource "aws_sqs_queue" "notifications" {
  name                       = "${local.name_prefix}-notifications-queue"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 345600  # 4 days
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notifications_dlq.arn
    maxReceiveCount     = 5
  })

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-notifications-queue"
    Type = "Notifications"
  })
}

resource "aws_sqs_queue" "notifications_dlq" {
  name                       = "${local.name_prefix}-notifications-dlq"
  message_retention_seconds  = 1209600

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-notifications-dlq"
    Type = "DeadLetterQueue"
  })
}

# Email Queue
resource "aws_sqs_queue" "email" {
  name                       = "${local.name_prefix}-email-queue"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 345600
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.email_dlq.arn
    maxReceiveCount     = 3
  })

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-email-queue"
    Type = "Email"
  })
}

resource "aws_sqs_queue" "email_dlq" {
  name                       = "${local.name_prefix}-email-dlq"
  message_retention_seconds  = 1209600

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-email-dlq"
    Type = "DeadLetterQueue"
  })
}

# Analytics Queue
resource "aws_sqs_queue" "analytics" {
  name                       = "${local.name_prefix}-analytics-queue"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 86400  # 1 day
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 30

  # No DLQ for analytics - drop if processing fails

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-analytics-queue"
    Type = "Analytics"
  })
}

# SQS Queue Policies
resource "aws_sqs_queue_policy" "transcoding" {
  queue_url = aws_sqs_queue.transcoding.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowEKSAccess"
        Effect    = "Allow"
        Principal = {
          AWS = module.app_irsa.iam_role_arn
        }
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = aws_sqs_queue.transcoding.arn
      }
    ]
  })
}
