# IRSA for Post Service (S3 Access)
module "iam_assumable_role_post_service" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-assumable-role-with-oidc"
  version = "~> 5.0"

  create_role = true

  role_name = "${var.project_name}-post-service-role"

  tags = var.tags

  provider_url = replace(module.eks.cluster_oidc_issuer_url, "https://", "")

  role_policy_arns = [
    aws_iam_policy.s3_access.arn
  ]

  oidc_fully_qualified_subjects = ["system:serviceaccount:default:post-service-sa"]
}

resource "aws_iam_policy" "s3_access" {
  name        = "${var.project_name}-s3-access"
  description = "Policy for Post Service to access S3"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "s3:PutObject",
          "s3:GetObject",
          "s3:DeleteObject"
        ]
        Effect   = "Allow"
        Resource = "${aws_s3_bucket.media_bucket.arn}/*"
      }
    ]
  })
}

# Placeholder for EKS module reference (assuming it exists or would be created)
# For this task, we are just defining the IAM role logic assuming OIDC provider exists.
# We need to define a dummy EKS module output or variable if we want this to be valid standalone code
# without the actual EKS cluster resource in this state.
# For now, we'll comment out the provider_url dependency to avoid errors if EKS isn't here.
