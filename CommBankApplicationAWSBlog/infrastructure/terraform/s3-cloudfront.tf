# S3 and CloudFront Configuration for CommSec Trading Platform

# S3 Bucket for Application Binaries
resource "aws_s3_bucket" "binaries" {
  bucket = "${var.s3_bucket_prefix}-binaries-${local.account_id}"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-binaries"
  })
}

resource "aws_s3_bucket_versioning" "binaries" {
  bucket = aws_s3_bucket.binaries.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "binaries" {
  bucket = aws_s3_bucket.binaries.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.s3.arn
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "binaries" {
  bucket = aws_s3_bucket.binaries.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# S3 Bucket for Static Assets (Frontend)
resource "aws_s3_bucket" "static_assets" {
  bucket = "${var.s3_bucket_prefix}-static-${local.account_id}"

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-static-assets"
  })
}

resource "aws_s3_bucket_versioning" "static_assets" {
  bucket = aws_s3_bucket.static_assets.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "static_assets" {
  bucket = aws_s3_bucket.static_assets.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "static_assets" {
  bucket = aws_s3_bucket.static_assets.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# CloudFront Origin Access Control
resource "aws_cloudfront_origin_access_control" "static_assets" {
  count = var.enable_cloudfront ? 1 : 0

  name                              = "${var.project_name}-static-oac"
  description                       = "OAC for static assets bucket"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# KMS Key for S3
resource "aws_kms_key" "s3" {
  description             = "KMS key for S3 encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-s3-kms"
  })
}

resource "aws_kms_alias" "s3" {
  name          = "alias/${var.project_name}-s3"
  target_key_id = aws_kms_key.s3.key_id
}

# CloudFront Distribution
resource "aws_cloudfront_distribution" "main" {
  count = var.enable_cloudfront ? 1 : 0

  enabled             = true
  is_ipv6_enabled     = true
  comment             = "CommSec Trading Platform CDN"
  default_root_object = "index.html"
  price_class         = var.cloudfront_price_class

  # S3 Origin for static assets
  origin {
    domain_name              = aws_s3_bucket.static_assets.bucket_regional_domain_name
    origin_access_control_id = aws_cloudfront_origin_access_control.static_assets[0].id
    origin_id                = "S3-static-assets"
  }

  # ALB Origin for API (will be updated after ALB creation)
  # origin {
  #   domain_name = aws_lb.web.dns_name
  #   origin_id   = "ALB-web"
  #
  #   custom_origin_config {
  #     http_port              = 80
  #     https_port             = 443
  #     origin_protocol_policy = "https-only"
  #     origin_ssl_protocols   = ["TLSv1.2"]
  #   }
  # }

  # Default cache behavior (static assets)
  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-static-assets"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
    compress               = true
  }

  # Cache behavior for API (no caching)
  # ordered_cache_behavior {
  #   path_pattern     = "/api/*"
  #   allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
  #   cached_methods   = ["GET", "HEAD"]
  #   target_origin_id = "ALB-web"
  #
  #   forwarded_values {
  #     query_string = true
  #     headers      = ["Authorization", "Host"]
  #     cookies {
  #       forward = "all"
  #     }
  #   }
  #
  #   viewer_protocol_policy = "https-only"
  #   min_ttl                = 0
  #   default_ttl            = 0
  #   max_ttl                = 0
  # }

  # Custom error responses for SPA
  custom_error_response {
    error_code         = 403
    response_code      = 200
    response_page_path = "/index.html"
  }

  custom_error_response {
    error_code         = 404
    response_code      = 200
    response_page_path = "/index.html"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = merge(local.common_tags, {
    Name = "${var.project_name}-cloudfront"
  })
}

# S3 bucket policy for CloudFront access
resource "aws_s3_bucket_policy" "static_assets" {
  count  = var.enable_cloudfront ? 1 : 0
  bucket = aws_s3_bucket.static_assets.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontServicePrincipal"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.static_assets.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.main[0].arn
          }
        }
      }
    ]
  })
}
