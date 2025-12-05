module "db" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 6.0"

  identifier = "${var.project_name}-db"

  engine            = "postgres"
  engine_version    = "15.4"
  instance_class    = "db.t3.micro"
  allocated_storage = 20

  db_name  = "instagram"
  username = var.db_username
  password = var.db_password
  port     = 5432

  iam_database_authentication_enabled = true

  vpc_security_group_ids = [module.security_group_db.security_group_id]
  create_db_subnet_group = true
  subnet_ids             = module.vpc.private_subnets

  family = "postgres15"

  major_engine_version = "15"

  deletion_protection = false # For demo purposes

  tags = var.tags
}

module "security_group_db" {
  source  = "terraform-aws-modules/security-group/aws"
  version = "~> 5.0"

  name        = "${var.project_name}-db-sg"
  description = "Security group for RDS"
  vpc_id      = module.vpc.vpc_id

  ingress_with_cidr_blocks = [
    {
      from_port   = 5432
      to_port     = 5432
      protocol    = "tcp"
      description = "PostgreSQL access from within VPC"
      cidr_blocks = var.vpc_cidr
    },
  ]

  tags = var.tags
}
