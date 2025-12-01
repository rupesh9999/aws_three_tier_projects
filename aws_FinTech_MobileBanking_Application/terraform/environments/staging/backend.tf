# ============================================================================
# Staging Environment Backend Configuration
# ============================================================================

terraform {
  backend "s3" {
    bucket         = "fintech-banking-terraform-state-staging"
    key            = "staging/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "fintech-banking-terraform-locks-staging"
  }
}
