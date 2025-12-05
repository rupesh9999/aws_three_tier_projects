# Prerequisites

Before you begin, ensure you have the following tools installed and configured on your local machine.

## Tools

### 1. Java Development Kit (JDK)
- **Version**: 21
- **Purpose**: Backend microservices development.
- **Installation**:
  - [OpenJDK](https://openjdk.org/projects/jdk/21/)
  - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/#java21)

### 2. Node.js & npm
- **Version**: Node.js v20+
- **Purpose**: Frontend development.
- **Installation**:
  - [Node.js](https://nodejs.org/en/download/)

### 3. Docker
- **Version**: 24.0+
- **Purpose**: Containerization of services.
- **Installation**:
  - [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### 4. AWS CLI
- **Version**: 2.x
- **Purpose**: Interacting with AWS services.
- **Installation**:
  - [AWS CLI Install Guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
- **Configuration**:
  ```bash
  aws configure
  # Enter Access Key ID, Secret Access Key, Region (e.g., us-east-1), and Output format (json)
  ```

### 5. Terraform
- **Version**: 1.5+
- **Purpose**: Infrastructure as Code (IaC).
- **Installation**:
  - [Terraform Install Guide](https://developer.hashicorp.com/terraform/downloads)

### 6. kubectl
- **Version**: 1.29+ (Must match EKS version)
- **Purpose**: Interacting with Kubernetes clusters.
- **Installation**:
  - [kubectl Install Guide](https://kubernetes.io/docs/tasks/tools/)

### 7. Helm
- **Version**: 3.x
- **Purpose**: Package manager for Kubernetes.
- **Installation**:
  - [Helm Install Guide](https://helm.sh/docs/intro/install/)

### 8. Argo CD CLI (Optional)
- **Version**: Latest
- **Purpose**: Managing Argo CD applications from CLI.
- **Installation**:
  - [Argo CD CLI Install Guide](https://argo-cd.readthedocs.io/en/stable/cli_installation/)

## Access Requirements

### AWS Permissions
Ensure your AWS IAM user has the following permissions:
- `AdministratorAccess` (Recommended for initial setup)
- OR granular permissions for:
  - EKS, EC2, VPC, IAM, RDS, ElastiCache, S3, OpenSearch, ECR, Secrets Manager.

### GitHub Access
- Access to the repository: `https://github.com/rupesh9999/aws_three_tier_projects.git`
