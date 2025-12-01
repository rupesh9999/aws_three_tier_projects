# API Gateway HTTP API
resource "aws_apigatewayv2_api" "main" {
  name          = "${var.project_name}-${var.environment}-api"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"]
    allow_headers = ["Content-Type", "Authorization", "X-Requested-With"]
    max_age       = 3600
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-api"
  }
}

# VPC Link for private integration
resource "aws_apigatewayv2_vpc_link" "main" {
  name               = "${var.project_name}-${var.environment}-vpc-link"
  security_group_ids = [aws_security_group.api_gateway.id]
  subnet_ids         = module.vpc.private_subnets

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc-link"
  }
}

# Security Group for API Gateway VPC Link
resource "aws_security_group" "api_gateway" {
  name        = "${var.project_name}-${var.environment}-api-gateway-sg"
  description = "Security group for API Gateway VPC Link"
  vpc_id      = module.vpc.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-api-gateway-sg"
  }
}

# API Gateway Stage
resource "aws_apigatewayv2_stage" "main" {
  api_id      = aws_apigatewayv2_api.main.id
  name        = var.environment
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gateway.arn
    format = jsonencode({
      requestId        = "$context.requestId"
      ip               = "$context.identity.sourceIp"
      requestTime      = "$context.requestTime"
      httpMethod       = "$context.httpMethod"
      routeKey         = "$context.routeKey"
      status           = "$context.status"
      protocol         = "$context.protocol"
      responseLength   = "$context.responseLength"
      integrationError = "$context.integrationErrorMessage"
    })
  }

  default_route_settings {
    throttling_burst_limit = 1000
    throttling_rate_limit  = 500
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-stage"
  }
}

# CloudWatch Log Group for API Gateway
resource "aws_cloudwatch_log_group" "api_gateway" {
  name              = "/aws/api-gateway/${var.project_name}-${var.environment}"
  retention_in_days = 30

  tags = {
    Name = "${var.project_name}-${var.environment}-api-gateway-logs"
  }
}

# SQS Queues for async processing
resource "aws_sqs_queue" "booking_notifications" {
  name                       = "${var.project_name}-${var.environment}-booking-notifications"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 345600
  receive_wait_time_seconds  = 10
  visibility_timeout_seconds = 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.booking_notifications_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-booking-notifications"
  }
}

resource "aws_sqs_queue" "booking_notifications_dlq" {
  name                      = "${var.project_name}-${var.environment}-booking-notifications-dlq"
  message_retention_seconds = 1209600

  tags = {
    Name = "${var.project_name}-${var.environment}-booking-notifications-dlq"
  }
}

resource "aws_sqs_queue" "payment_processing" {
  name                       = "${var.project_name}-${var.environment}-payment-processing"
  delay_seconds              = 0
  max_message_size           = 262144
  message_retention_seconds  = 345600
  receive_wait_time_seconds  = 10
  visibility_timeout_seconds = 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.payment_processing_dlq.arn
    maxReceiveCount     = 3
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-payment-processing"
  }
}

resource "aws_sqs_queue" "payment_processing_dlq" {
  name                      = "${var.project_name}-${var.environment}-payment-processing-dlq"
  message_retention_seconds = 1209600

  tags = {
    Name = "${var.project_name}-${var.environment}-payment-processing-dlq"
  }
}

# SNS Topics
resource "aws_sns_topic" "booking_events" {
  name = "${var.project_name}-${var.environment}-booking-events"

  tags = {
    Name = "${var.project_name}-${var.environment}-booking-events"
  }
}

resource "aws_sns_topic" "payment_events" {
  name = "${var.project_name}-${var.environment}-payment-events"

  tags = {
    Name = "${var.project_name}-${var.environment}-payment-events"
  }
}

# SNS Topic Subscriptions
resource "aws_sns_topic_subscription" "booking_to_sqs" {
  topic_arn = aws_sns_topic.booking_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.booking_notifications.arn
}

resource "aws_sns_topic_subscription" "payment_to_sqs" {
  topic_arn = aws_sns_topic.payment_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.payment_processing.arn
}

# SQS Policy for SNS
resource "aws_sqs_queue_policy" "booking_notifications" {
  queue_url = aws_sqs_queue.booking_notifications.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect    = "Allow"
        Principal = { Service = "sns.amazonaws.com" }
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.booking_notifications.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.booking_events.arn
          }
        }
      }
    ]
  })
}

resource "aws_sqs_queue_policy" "payment_processing" {
  queue_url = aws_sqs_queue.payment_processing.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect    = "Allow"
        Principal = { Service = "sns.amazonaws.com" }
        Action    = "sqs:SendMessage"
        Resource  = aws_sqs_queue.payment_processing.arn
        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_sns_topic.payment_events.arn
          }
        }
      }
    ]
  })
}
