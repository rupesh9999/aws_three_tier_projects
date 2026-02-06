#!/bin/bash
# CommSec Trading Platform - Deployment Script
# This script deploys the entire application stack to AWS EKS

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="${PROJECT_NAME:-commsec}"
AWS_REGION="${AWS_REGION:-us-east-1}"
AWS_ACCOUNT_ID="${AWS_ACCOUNT_ID:-$(aws sts get-caller-identity --query Account --output text)}"
EKS_CLUSTER_NAME="${EKS_CLUSTER_NAME:-commsec-cluster}"

# Derived variables
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    command -v aws >/dev/null 2>&1 || error "AWS CLI is not installed"
    command -v kubectl >/dev/null 2>&1 || error "kubectl is not installed"
    command -v docker >/dev/null 2>&1 || error "Docker is not installed"
    
    aws sts get-caller-identity >/dev/null 2>&1 || error "AWS credentials not configured"
    
    success "All prerequisites met"
}

# Configure kubectl for EKS
configure_kubectl() {
    log "Configuring kubectl for EKS cluster: ${EKS_CLUSTER_NAME}..."
    aws eks update-kubeconfig --name "${EKS_CLUSTER_NAME}" --region "${AWS_REGION}"
    success "kubectl configured"
}

# Login to ECR
ecr_login() {
    log "Logging into ECR..."
    aws ecr get-login-password --region "${AWS_REGION}" | \
        docker login --username AWS --password-stdin "${ECR_REGISTRY}"
    success "ECR login successful"
}

# Build and push Docker images
build_and_push() {
    local service=$1
    local dockerfile_path=$2
    local image_name="${PROJECT_NAME}-${service}"
    local image_tag="${ECR_REGISTRY}/${image_name}:latest"
    
    log "Building ${service}..."
    docker build -t "${image_tag}" -f "${dockerfile_path}/Dockerfile" "${dockerfile_path}"
    
    log "Pushing ${service} to ECR..."
    docker push "${image_tag}"
    success "${service} image pushed to ECR"
}

# Build all images
build_all_images() {
    log "Building all Docker images..."
    
    build_and_push "frontend" "frontend"
    build_and_push "api-gateway" "backend/api-gateway"
    build_and_push "trading-service" "backend/trading-service"
    build_and_push "market-data-service" "backend/market-data-service"
    
    success "All images built and pushed"
}

# Create Kubernetes namespace
create_namespace() {
    log "Creating namespace ${PROJECT_NAME}..."
    kubectl create namespace "${PROJECT_NAME}" --dry-run=client -o yaml | kubectl apply -f -
    success "Namespace created/updated"
}

# Apply Kubernetes configurations
apply_k8s_configs() {
    log "Applying Kubernetes configurations..."
    
    # Apply config and secrets first
    kubectl apply -f kubernetes/config/
    
    # Apply backend services
    kubectl apply -f kubernetes/backend/
    
    # Apply frontend
    kubectl apply -f kubernetes/frontend/
    
    success "Kubernetes configurations applied"
}

# Wait for deployments to be ready
wait_for_deployments() {
    log "Waiting for deployments to be ready..."
    
    local deployments=("frontend" "api-gateway" "trading-service" "market-data-service")
    
    for deployment in "${deployments[@]}"; do
        log "Waiting for ${deployment}..."
        kubectl rollout status deployment/"${deployment}" -n "${PROJECT_NAME}" --timeout=300s
    done
    
    success "All deployments are ready"
}

# Display deployment status
show_status() {
    log "Deployment Status:"
    echo ""
    
    echo "=== Pods ==="
    kubectl get pods -n "${PROJECT_NAME}"
    echo ""
    
    echo "=== Services ==="
    kubectl get svc -n "${PROJECT_NAME}"
    echo ""
    
    echo "=== Ingress ==="
    kubectl get ingress -n "${PROJECT_NAME}"
    echo ""
    
    # Get ALB URL
    local alb_url=$(kubectl get ingress frontend-ingress -n "${PROJECT_NAME}" -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "Pending...")
    echo "Application URL: http://${alb_url}"
}

# Main deployment function
deploy() {
    log "Starting deployment of CommSec Trading Platform..."
    echo ""
    
    check_prerequisites
    configure_kubectl
    ecr_login
    build_all_images
    create_namespace
    apply_k8s_configs
    wait_for_deployments
    show_status
    
    success "Deployment completed successfully!"
}

# Cleanup function
cleanup() {
    log "Cleaning up resources..."
    kubectl delete namespace "${PROJECT_NAME}" --ignore-not-found
    success "Cleanup completed"
}

# Help function
show_help() {
    echo "CommSec Trading Platform Deployment Script"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  deploy    Deploy the entire application stack"
    echo "  build     Build and push Docker images only"
    echo "  apply     Apply Kubernetes configurations only"
    echo "  status    Show deployment status"
    echo "  cleanup   Delete all resources"
    echo "  help      Show this help message"
}

# Parse command
case "${1:-deploy}" in
    deploy)
        deploy
        ;;
    build)
        check_prerequisites
        ecr_login
        build_all_images
        ;;
    apply)
        check_prerequisites
        configure_kubectl
        create_namespace
        apply_k8s_configs
        wait_for_deployments
        ;;
    status)
        configure_kubectl
        show_status
        ;;
    cleanup)
        configure_kubectl
        cleanup
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        error "Unknown command: $1. Use 'help' for usage information."
        ;;
esac
