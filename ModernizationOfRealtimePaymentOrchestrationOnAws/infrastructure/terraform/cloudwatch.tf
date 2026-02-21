#--------------------------------------------------------------
# CloudWatch Log Groups
#--------------------------------------------------------------
resource "aws_cloudwatch_log_group" "eks" {
  name              = "/aws/eks/${local.name_prefix}-eks/cluster"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "lambda_settlement" {
  name              = "/aws/lambda/${local.name_prefix}-settlement-processor"
  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "lambda_notification" {
  name              = "/aws/lambda/${local.name_prefix}-notification-service"
  retention_in_days = 30
}

#--------------------------------------------------------------
# CloudWatch Alarms
#--------------------------------------------------------------
resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${local.name_prefix}-rds-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 3
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS CPU utilization is above 80%"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.identifier
  }

  tags = {
    Name = "${local.name_prefix}-rds-cpu-alarm"
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_free_storage" {
  alarm_name          = "${local.name_prefix}-rds-storage-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 5368709120 # 5GB in bytes
  alarm_description   = "RDS free storage is below 5GB"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.identifier
  }

  tags = {
    Name = "${local.name_prefix}-rds-storage-alarm"
  }
}

resource "aws_cloudwatch_metric_alarm" "msk_disk" {
  alarm_name          = "${local.name_prefix}-msk-disk-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "KafkaDataLogsDiskUsed"
  namespace           = "AWS/Kafka"
  period              = 300
  statistic           = "Average"
  threshold           = 85
  alarm_description   = "MSK disk utilization is above 85%"

  dimensions = {
    "Cluster Name" = aws_msk_cluster.main.cluster_name
  }

  tags = {
    Name = "${local.name_prefix}-msk-disk-alarm"
  }
}

resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  alarm_name          = "${local.name_prefix}-lambda-settlement-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Sum"
  threshold           = 5
  alarm_description   = "Settlement Lambda error count exceeds threshold"

  dimensions = {
    FunctionName = aws_lambda_function.settlement.function_name
  }

  tags = {
    Name = "${local.name_prefix}-lambda-errors-alarm"
  }
}

#--------------------------------------------------------------
# CloudWatch Dashboard
#--------------------------------------------------------------
resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${local.name_prefix}-dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", aws_db_instance.main.identifier],
            ["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", aws_db_instance.main.identifier]
          ]
          period = 300
          stat   = "Average"
          region = var.aws_region
          title  = "RDS PostgreSQL Metrics"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/Kafka", "KafkaDataLogsDiskUsed", "Cluster Name", aws_msk_cluster.main.cluster_name],
            ["AWS/Kafka", "MessagesInPerSec", "Cluster Name", aws_msk_cluster.main.cluster_name]
          ]
          period = 300
          stat   = "Average"
          region = var.aws_region
          title  = "MSK Kafka Metrics"
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/Lambda", "Invocations", "FunctionName", aws_lambda_function.settlement.function_name],
            ["AWS/Lambda", "Errors", "FunctionName", aws_lambda_function.settlement.function_name],
            ["AWS/Lambda", "Duration", "FunctionName", aws_lambda_function.settlement.function_name]
          ]
          period = 300
          stat   = "Sum"
          region = var.aws_region
          title  = "Lambda Settlement Metrics"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/ApiGateway", "Count", "ApiName", aws_api_gateway_rest_api.main.name],
            ["AWS/ApiGateway", "5XXError", "ApiName", aws_api_gateway_rest_api.main.name],
            ["AWS/ApiGateway", "Latency", "ApiName", aws_api_gateway_rest_api.main.name]
          ]
          period = 300
          stat   = "Sum"
          region = var.aws_region
          title  = "API Gateway Metrics"
        }
      }
    ]
  })
}
