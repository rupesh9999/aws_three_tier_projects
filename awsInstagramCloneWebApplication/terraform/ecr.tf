locals {
  services = [
    "auth-service",
    "user-service",
    "post-service",
    "feed-service",
    "notification-service",
    "ai-service",
    "api-gateway",
    "frontend"
  ]
}

resource "aws_ecr_repository" "repos" {
  for_each = toset(local.services)

  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = var.tags
}
