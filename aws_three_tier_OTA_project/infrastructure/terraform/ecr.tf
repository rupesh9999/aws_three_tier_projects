# ECR Repositories
resource "aws_ecr_repository" "frontend" {
  name                 = "${var.project_name}-${var.environment}/frontend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-frontend"
  }
}

resource "aws_ecr_repository" "auth_service" {
  name                 = "${var.project_name}-${var.environment}/auth-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-auth-service"
  }
}

resource "aws_ecr_repository" "search_service" {
  name                 = "${var.project_name}-${var.environment}/search-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-search-service"
  }
}

resource "aws_ecr_repository" "booking_service" {
  name                 = "${var.project_name}-${var.environment}/booking-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-booking-service"
  }
}

resource "aws_ecr_repository" "payment_service" {
  name                 = "${var.project_name}-${var.environment}/payment-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-payment-service"
  }
}

resource "aws_ecr_repository" "cart_service" {
  name                 = "${var.project_name}-${var.environment}/cart-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-cart-service"
  }
}

# ECR Lifecycle Policy
resource "aws_ecr_lifecycle_policy" "cleanup" {
  for_each = toset([
    aws_ecr_repository.frontend.name,
    aws_ecr_repository.auth_service.name,
    aws_ecr_repository.search_service.name,
    aws_ecr_repository.booking_service.name,
    aws_ecr_repository.payment_service.name,
    aws_ecr_repository.cart_service.name,
  ])

  repository = each.value

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
