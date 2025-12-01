# =============================================================================
# S3 Buckets for Content Storage
# =============================================================================

# Content bucket (original uploads)
resource "aws_s3_bucket" "content" {
  bucket        = "${local.name_prefix}-content-${data.aws_caller_identity.current.account_id}"
  force_destroy = var.s3_force_destroy

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-content"
    Type = "Original Content"
  })
}

resource "aws_s3_bucket_versioning" "content" {
  bucket = aws_s3_bucket.content.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "content" {
  bucket = aws_s3_bucket.content.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "content" {
  bucket = aws_s3_bucket.content.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "content" {
  bucket = aws_s3_bucket.content.id

  rule {
    id     = "transition-to-ia"
    status = "Enabled"

    transition {
      days          = 90
      storage_class = "STANDARD_IA"
    }

    transition {
      days          = 365
      storage_class = "GLACIER"
    }

    noncurrent_version_transition {
      noncurrent_days = 30
      storage_class   = "STANDARD_IA"
    }

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }
}

# Transcoded content bucket (for streaming)
resource "aws_s3_bucket" "transcoded" {
  bucket        = "${local.name_prefix}-transcoded-${data.aws_caller_identity.current.account_id}"
  force_destroy = var.s3_force_destroy

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-transcoded"
    Type = "Transcoded Content"
  })
}

resource "aws_s3_bucket_versioning" "transcoded" {
  bucket = aws_s3_bucket.transcoded.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "transcoded" {
  bucket = aws_s3_bucket.transcoded.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "transcoded" {
  bucket = aws_s3_bucket.transcoded.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# CORS configuration for transcoded bucket (for HLS streaming)
resource "aws_s3_bucket_cors_configuration" "transcoded" {
  bucket = aws_s3_bucket.transcoded.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["*"]  # Will be restricted by CloudFront
    expose_headers  = ["ETag"]
    max_age_seconds = 3600
  }
}

# Static assets bucket (thumbnails, posters, etc.)
resource "aws_s3_bucket" "static" {
  bucket        = "${local.name_prefix}-static-${data.aws_caller_identity.current.account_id}"
  force_destroy = var.s3_force_destroy

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-static"
    Type = "Static Assets"
  })
}

resource "aws_s3_bucket_versioning" "static" {
  bucket = aws_s3_bucket.static.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "static" {
  bucket = aws_s3_bucket.static.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "static" {
  bucket = aws_s3_bucket.static.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
