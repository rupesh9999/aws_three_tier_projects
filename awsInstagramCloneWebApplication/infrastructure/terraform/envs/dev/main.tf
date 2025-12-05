module "vpc" {
  source = "../../modules/vpc"

  environment          = "dev"
  vpc_cidr             = "10.0.0.0/16"
  public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnet_cidrs = ["10.0.3.0/24", "10.0.4.0/24"]
  availability_zones   = ["us-east-2a", "us-east-2b"]
  cluster_name         = "instagram-clone-dev"
}

module "eks" {
  source = "../../modules/eks"

  cluster_name        = "instagram-clone-dev"
  cluster_version     = "1.29"
  subnet_ids          = module.vpc.private_subnet_ids
  node_desired_size   = 2
  node_max_size       = 3
  node_min_size       = 1
  node_instance_types = ["t3.medium"]
}
