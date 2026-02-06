# EKS Cluster Configuration for CommSec Trading Platform

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.31"

  cluster_name    = var.eks_cluster_name
  cluster_version = var.eks_cluster_version

  # Networking
  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # Cluster access
  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = true

  # Enable IRSA
  enable_irsa = true

  # Cluster addons
  cluster_addons = {
    coredns = {
      most_recent = true
      configuration_values = jsonencode({
        computeType  = "Fargate"
        replicaCount = 2
      })
    }
    kube-proxy = {
      most_recent = true
    }
    vpc-cni = {
      most_recent              = true
      service_account_role_arn = module.vpc_cni_irsa_role.iam_role_arn
      configuration_values = jsonencode({
        env = {
          ENABLE_PREFIX_DELEGATION = "true"
          WARM_PREFIX_TARGET       = "1"
        }
      })
    }
    aws-ebs-csi-driver = {
      most_recent              = true
      service_account_role_arn = module.ebs_csi_irsa_role.iam_role_arn
    }
  }

  # EKS Managed Node Groups
  eks_managed_node_groups = {
    # Web Tier Node Group
    web-tier = {
      name           = "web-tier-nodes"
      instance_types = var.eks_web_node_instance_types
      capacity_type  = "ON_DEMAND"

      min_size     = var.eks_web_node_min_size
      max_size     = var.eks_web_node_max_size
      desired_size = var.eks_web_node_desired_size

      # Labels for node selection
      labels = {
        "tier"        = "web"
        "environment" = var.environment
      }

      # Taints (optional - uncomment to prevent workloads without tolerations)
      # taints = [
      #   {
      #     key    = "tier"
      #     value  = "web"
      #     effect = "NO_SCHEDULE"
      #   }
      # ]

      # Update configuration
      update_config = {
        max_unavailable_percentage = 33
      }

      tags = merge(local.common_tags, {
        "Tier" = "Web"
      })
    }

    # App Tier Node Group
    app-tier = {
      name           = "app-tier-nodes"
      instance_types = var.eks_app_node_instance_types
      capacity_type  = "ON_DEMAND"

      min_size     = var.eks_app_node_min_size
      max_size     = var.eks_app_node_max_size
      desired_size = var.eks_app_node_desired_size

      # Labels for node selection
      labels = {
        "tier"        = "app"
        "environment" = var.environment
      }

      # Update configuration
      update_config = {
        max_unavailable_percentage = 33
      }

      tags = merge(local.common_tags, {
        "Tier" = "App"
      })
    }
  }

  # Cluster security group rules
  cluster_security_group_additional_rules = {
    ingress_nodes_ephemeral_ports_tcp = {
      description                = "Nodes on ephemeral ports"
      protocol                   = "tcp"
      from_port                  = 1025
      to_port                    = 65535
      type                       = "ingress"
      source_node_security_group = true
    }
  }

  # Node security group rules
  node_security_group_additional_rules = {
    ingress_self_all = {
      description = "Node to node all ports/protocols"
      protocol    = "-1"
      from_port   = 0
      to_port     = 0
      type        = "ingress"
      self        = true
    }
    ingress_cluster_all = {
      description                   = "Cluster to node all ports/protocols"
      protocol                      = "-1"
      from_port                     = 0
      to_port                       = 0
      type                          = "ingress"
      source_cluster_security_group = true
    }
  }

  # Enable access entry for cluster creator
  enable_cluster_creator_admin_permissions = true

  tags = merge(local.common_tags, {
    Name = var.eks_cluster_name
  })
}

# IRSA for VPC CNI
module "vpc_cni_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.52"

  role_name             = "${var.project_name}-vpc-cni-irsa"
  attach_vpc_cni_policy = true
  vpc_cni_enable_ipv4   = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-node"]
    }
  }

  tags = local.common_tags
}

# IRSA for EBS CSI Driver
module "ebs_csi_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.52"

  role_name             = "${var.project_name}-ebs-csi-irsa"
  attach_ebs_csi_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }

  tags = local.common_tags
}

# IRSA for AWS Load Balancer Controller
module "aws_lb_controller_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.52"

  role_name                              = "${var.project_name}-aws-lb-controller-irsa"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }

  tags = local.common_tags
}

# Kubernetes namespace for the application
resource "kubernetes_namespace" "commsec" {
  depends_on = [module.eks]

  metadata {
    name = var.project_name

    labels = {
      name        = var.project_name
      environment = var.environment
    }
  }
}

# Install AWS Load Balancer Controller via Helm
resource "helm_release" "aws_load_balancer_controller" {
  depends_on = [module.eks, kubernetes_namespace.commsec]

  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.10.0"

  values = [
    yamlencode({
      clusterName = module.eks.cluster_name
      region      = var.aws_region
      vpcId       = module.vpc.vpc_id
      serviceAccount = {
        create = true
        name   = "aws-load-balancer-controller"
        annotations = {
          "eks.amazonaws.com/role-arn" = module.aws_lb_controller_irsa_role.iam_role_arn
        }
      }
    })
  ]
}

# Cluster Autoscaler IRSA
module "cluster_autoscaler_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.52"

  role_name                        = "${var.project_name}-cluster-autoscaler-irsa"
  attach_cluster_autoscaler_policy = true
  cluster_autoscaler_cluster_names = [module.eks.cluster_name]

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:cluster-autoscaler"]
    }
  }

  tags = local.common_tags
}
