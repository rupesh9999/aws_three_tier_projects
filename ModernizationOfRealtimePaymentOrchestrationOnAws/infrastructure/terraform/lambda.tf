#--------------------------------------------------------------
# Lambda IAM Role
#--------------------------------------------------------------
resource "aws_iam_role" "lambda_execution" {
  name = "${local.name_prefix}-lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_execution.name
}

resource "aws_iam_role_policy_attachment" "lambda_vpc" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
  role       = aws_iam_role.lambda_execution.name
}

resource "aws_iam_role_policy" "lambda_msk" {
  name = "${local.name_prefix}-lambda-msk-policy"
  role = aws_iam_role.lambda_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "kafka-cluster:Connect",
          "kafka-cluster:DescribeCluster",
          "kafka-cluster:ReadData",
          "kafka-cluster:DescribeTopic",
          "kafka-cluster:DescribeGroup",
          "kafka-cluster:AlterGroup"
        ]
        Resource = [aws_msk_cluster.main.arn, "${aws_msk_cluster.main.arn}/*"]
      }
    ]
  })
}

#--------------------------------------------------------------
# Lambda: Settlement Processor
#--------------------------------------------------------------
resource "aws_lambda_function" "settlement" {
  function_name = "${local.name_prefix}-settlement-processor"
  description   = "Processes payment settlements triggered by MSK events"
  role          = aws_iam_role.lambda_execution.arn

  runtime       = "java21"
  handler       = "com.payment.lambda.SettlementHandler::handleRequest"
  memory_size   = 512
  timeout       = 60
  architectures = ["arm64"]

  filename         = "${path.module}/lambda/placeholder.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda/placeholder.zip")

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda.id]
  }

  environment {
    variables = {
      DB_HOST     = aws_db_instance.main.address
      DB_PORT     = "5432"
      DB_NAME     = var.rds_db_name
      ENVIRONMENT = var.environment
    }
  }

  tracing_config {
    mode = "Active"
  }

  tags = {
    Name = "${local.name_prefix}-settlement-lambda"
  }
}

#--------------------------------------------------------------
# Lambda: Notification Service
#--------------------------------------------------------------
resource "aws_lambda_function" "notification" {
  function_name = "${local.name_prefix}-notification-service"
  description   = "Sends payment status notifications"
  role          = aws_iam_role.lambda_execution.arn

  runtime       = "java21"
  handler       = "com.payment.lambda.NotificationHandler::handleRequest"
  memory_size   = 256
  timeout       = 30
  architectures = ["arm64"]

  filename         = "${path.module}/lambda/placeholder.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda/placeholder.zip")

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda.id]
  }

  environment {
    variables = {
      ENVIRONMENT = var.environment
    }
  }

  tracing_config {
    mode = "Active"
  }

  tags = {
    Name = "${local.name_prefix}-notification-lambda"
  }
}

#--------------------------------------------------------------
# Lambda: Compliance Checker
#--------------------------------------------------------------
resource "aws_lambda_function" "compliance" {
  function_name = "${local.name_prefix}-compliance-checker"
  description   = "Runs compliance checks on payment transactions"
  role          = aws_iam_role.lambda_execution.arn

  runtime       = "java21"
  handler       = "com.payment.lambda.ComplianceHandler::handleRequest"
  memory_size   = 256
  timeout       = 30
  architectures = ["arm64"]

  filename         = "${path.module}/lambda/placeholder.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda/placeholder.zip")

  vpc_config {
    subnet_ids         = aws_subnet.private[*].id
    security_group_ids = [aws_security_group.lambda.id]
  }

  environment {
    variables = {
      ENVIRONMENT = var.environment
    }
  }

  tracing_config {
    mode = "Active"
  }

  tags = {
    Name = "${local.name_prefix}-compliance-lambda"
  }
}
