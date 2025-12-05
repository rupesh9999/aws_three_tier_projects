resource "aws_opensearch_domain" "opensearch" {
  domain_name    = "${var.project_name}-logs"
  engine_version = "OpenSearch_2.7"

  cluster_config {
    instance_type = "t3.small.search"
    instance_count = 1
  }

  ebs_options {
    ebs_enabled = true
    volume_size = 10
  }

  access_policies = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "es:*"
        Principal = {
          AWS = "*"
        }
        Effect = "Allow"
        Resource = "arn:aws:es:${var.region}:*:domain/${var.project_name}-logs/*"
        Condition = {
          StringEquals = {
            "aws:SourceAccount" = data.aws_caller_identity.current.account_id
          }
        }
      }
    ]
  })

  tags = var.tags
}
