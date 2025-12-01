# =============================================================================
# CloudFront Distribution for Content Delivery
# =============================================================================

# Origin Access Control for S3
resource "aws_cloudfront_origin_access_control" "main" {
  name                              = "${local.name_prefix}-oac"
  description                       = "OAC for StreamFlix S3 buckets"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# CloudFront Distribution for video streaming
resource "aws_cloudfront_distribution" "video" {
  enabled             = true
  is_ipv6_enabled     = true
  comment             = "StreamFlix Video CDN"
  default_root_object = "index.html"
  price_class         = var.cloudfront_price_class
  
  aliases = var.environment == "prod" ? ["cdn.${var.domain_name}"] : []

  # S3 Origin for transcoded videos
  origin {
    domain_name              = aws_s3_bucket.transcoded.bucket_regional_domain_name
    origin_id                = "transcoded-content"
    origin_access_control_id = aws_cloudfront_origin_access_control.main.id
  }

  # S3 Origin for static assets
  origin {
    domain_name              = aws_s3_bucket.static.bucket_regional_domain_name
    origin_id                = "static-assets"
    origin_access_control_id = aws_cloudfront_origin_access_control.main.id
  }

  # Default cache behavior for video content
  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "transcoded-content"

    forwarded_values {
      query_string = false
      headers      = ["Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"]

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 86400      # 1 day
    max_ttl                = 31536000   # 1 year
    compress               = true

    # Signed URLs for content protection
    trusted_key_groups = var.environment == "prod" ? [aws_cloudfront_key_group.main[0].id] : []
  }

  # Cache behavior for static assets
  ordered_cache_behavior {
    path_pattern     = "/static/*"
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "static-assets"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 86400
    max_ttl                = 31536000
    compress               = true
  }

  # Cache behavior for thumbnails
  ordered_cache_behavior {
    path_pattern     = "/thumbnails/*"
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "static-assets"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 604800     # 1 week
    max_ttl                = 31536000
    compress               = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = var.environment != "prod"
    minimum_protocol_version       = "TLSv1.2_2021"
    # ssl_support_method             = var.environment == "prod" ? "sni-only" : null
    # acm_certificate_arn            = var.environment == "prod" ? aws_acm_certificate.cdn[0].arn : null
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-video-cdn"
  })
}

# S3 bucket policy for CloudFront access
resource "aws_s3_bucket_policy" "transcoded" {
  bucket = aws_s3_bucket.transcoded.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowCloudFrontServicePrincipal"
        Effect    = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.transcoded.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.video.arn
          }
        }
      }
    ]
  })
}

resource "aws_s3_bucket_policy" "static" {
  bucket = aws_s3_bucket.static.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowCloudFrontServicePrincipal"
        Effect    = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.static.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.video.arn
          }
        }
      }
    ]
  })
}

# CloudFront Key Pair for Signed URLs (production only)
resource "tls_private_key" "cloudfront" {
  count     = var.environment == "prod" ? 1 : 0
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "aws_cloudfront_public_key" "main" {
  count       = var.environment == "prod" ? 1 : 0
  name        = "${local.name_prefix}-cf-public-key"
  encoded_key = tls_private_key.cloudfront[0].public_key_pem
  comment     = "Public key for signed URLs"
}

resource "aws_cloudfront_key_group" "main" {
  count   = var.environment == "prod" ? 1 : 0
  name    = "${local.name_prefix}-cf-key-group"
  items   = [aws_cloudfront_public_key.main[0].id]
  comment = "Key group for signed URLs"
}

# Store CloudFront signing key in Secrets Manager
resource "aws_secretsmanager_secret" "cloudfront_key" {
  count       = var.environment == "prod" ? 1 : 0
  name        = "${local.name_prefix}/cloudfront-signing-key"
  description = "CloudFront signing private key for signed URLs"

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "cloudfront_key" {
  count     = var.environment == "prod" ? 1 : 0
  secret_id = aws_secretsmanager_secret.cloudfront_key[0].id
  secret_string = jsonencode({
    private_key   = tls_private_key.cloudfront[0].private_key_pem
    key_pair_id   = aws_cloudfront_public_key.main[0].id
    key_group_id  = aws_cloudfront_key_group.main[0].id
  })
}
