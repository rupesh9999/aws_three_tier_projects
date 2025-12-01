#!/bin/bash
#
# AWS Secrets Manager Setup Script for OTA Travel Application
# This script creates the required secrets structure in AWS Secrets Manager
#
# Usage: ./setup-secrets.sh [environment]
# Example: ./setup-secrets.sh production
#          ./setup-secrets.sh staging

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="ota"
ENVIRONMENT="${1:-production}"
AWS_REGION="${AWS_REGION:-us-east-2}"

echo -e "${BLUE}=============================================${NC}"
echo -e "${BLUE}  AWS Secrets Manager Setup Script${NC}"
echo -e "${BLUE}  Project: ${PROJECT_NAME}${NC}"
echo -e "${BLUE}  Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}  Region: ${AWS_REGION}${NC}"
echo -e "${BLUE}=============================================${NC}"
echo ""

# Function to check if AWS CLI is configured
check_aws_cli() {
    echo -e "${YELLOW}Checking AWS CLI configuration...${NC}"
    if ! command -v aws &> /dev/null; then
        echo -e "${RED}Error: AWS CLI is not installed${NC}"
        exit 1
    fi
    
    if ! aws sts get-caller-identity &> /dev/null; then
        echo -e "${RED}Error: AWS CLI is not configured or credentials are invalid${NC}"
        echo "Please run 'aws configure' to set up your credentials"
        exit 1
    fi
    
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    echo -e "${GREEN}✓ AWS CLI configured for account: ${ACCOUNT_ID}${NC}"
    echo ""
}

# Function to generate a secure random password
generate_password() {
    openssl rand -base64 24 | tr -dc 'a-zA-Z0-9!@#$%^&*()' | head -c 20
}

# Function to generate JWT secret (256-bit)
generate_jwt_secret() {
    openssl rand -base64 32
}

# Function to check if secret exists
secret_exists() {
    local secret_name=$1
    aws secretsmanager describe-secret --secret-id "$secret_name" --region "$AWS_REGION" &> /dev/null
    return $?
}

# Function to create or update a secret
create_or_update_secret() {
    local secret_name=$1
    local secret_value=$2
    local description=$3
    
    if secret_exists "$secret_name"; then
        echo -e "${YELLOW}  → Secret exists, updating: ${secret_name}${NC}"
        aws secretsmanager update-secret \
            --secret-id "$secret_name" \
            --secret-string "$secret_value" \
            --region "$AWS_REGION" > /dev/null
    else
        echo -e "${GREEN}  → Creating secret: ${secret_name}${NC}"
        aws secretsmanager create-secret \
            --name "$secret_name" \
            --description "$description" \
            --secret-string "$secret_value" \
            --region "$AWS_REGION" \
            --tags "Key=Project,Value=${PROJECT_NAME}" "Key=Environment,Value=${ENVIRONMENT}" > /dev/null
    fi
}

# Function to prompt for value or use default/generated
prompt_or_generate() {
    local prompt_text=$1
    local default_value=$2
    local is_password=$3
    
    if [ "$is_password" = "true" ]; then
        read -sp "$prompt_text [Press Enter to auto-generate]: " user_value
        echo ""
    else
        read -p "$prompt_text [$default_value]: " user_value
    fi
    
    if [ -z "$user_value" ]; then
        echo "$default_value"
    else
        echo "$user_value"
    fi
}

# Main setup function
setup_secrets() {
    echo -e "${YELLOW}Setting up secrets for ${PROJECT_NAME}/${ENVIRONMENT}...${NC}"
    echo ""
    
    # ==========================================
    # Database Credentials
    # ==========================================
    echo -e "${BLUE}[1/5] Database Credentials${NC}"
    
    DB_HOST=$(prompt_or_generate "  Database Host" "${PROJECT_NAME}-${ENVIRONMENT}-db.cluster-xxx.${AWS_REGION}.rds.amazonaws.com" "false")
    DB_PORT=$(prompt_or_generate "  Database Port" "5432" "false")
    DB_NAME=$(prompt_or_generate "  Database Name" "ota_travel" "false")
    DB_USERNAME=$(prompt_or_generate "  Database Username" "ota_admin" "false")
    
    echo -n "  Database Password [Press Enter to auto-generate]: "
    read -s DB_PASSWORD
    echo ""
    if [ -z "$DB_PASSWORD" ]; then
        DB_PASSWORD=$(generate_password)
        echo -e "  ${GREEN}Auto-generated password${NC}"
    fi
    
    DATABASE_SECRET=$(cat <<EOF
{
    "host": "${DB_HOST}",
    "port": ${DB_PORT},
    "database": "${DB_NAME}",
    "username": "${DB_USERNAME}",
    "password": "${DB_PASSWORD}",
    "engine": "postgres",
    "ssl": true
}
EOF
)
    
    create_or_update_secret \
        "${PROJECT_NAME}/${ENVIRONMENT}/database" \
        "$DATABASE_SECRET" \
        "Database credentials for ${PROJECT_NAME} ${ENVIRONMENT}"
    echo ""
    
    # ==========================================
    # Redis Credentials
    # ==========================================
    echo -e "${BLUE}[2/5] Redis Credentials${NC}"
    
    REDIS_HOST=$(prompt_or_generate "  Redis Host" "${PROJECT_NAME}-${ENVIRONMENT}-redis.xxx.cache.amazonaws.com" "false")
    REDIS_PORT=$(prompt_or_generate "  Redis Port" "6379" "false")
    
    echo -n "  Redis Auth Token [Press Enter to auto-generate, or 'none' for no auth]: "
    read -s REDIS_PASSWORD
    echo ""
    if [ -z "$REDIS_PASSWORD" ]; then
        REDIS_PASSWORD=$(generate_password)
        echo -e "  ${GREEN}Auto-generated auth token${NC}"
    elif [ "$REDIS_PASSWORD" = "none" ]; then
        REDIS_PASSWORD=""
    fi
    
    REDIS_SECRET=$(cat <<EOF
{
    "host": "${REDIS_HOST}",
    "port": ${REDIS_PORT},
    "password": "${REDIS_PASSWORD}",
    "ssl": true,
    "cluster_mode": false
}
EOF
)
    
    create_or_update_secret \
        "${PROJECT_NAME}/${ENVIRONMENT}/redis" \
        "$REDIS_SECRET" \
        "Redis credentials for ${PROJECT_NAME} ${ENVIRONMENT}"
    echo ""
    
    # ==========================================
    # JWT Secret
    # ==========================================
    echo -e "${BLUE}[3/5] JWT Secret${NC}"
    
    echo -n "  JWT Secret [Press Enter to auto-generate 256-bit key]: "
    read -s JWT_SECRET
    echo ""
    if [ -z "$JWT_SECRET" ]; then
        JWT_SECRET=$(generate_jwt_secret)
        echo -e "  ${GREEN}Auto-generated 256-bit JWT secret${NC}"
    fi
    
    JWT_SECRET_JSON=$(cat <<EOF
{
    "secret": "${JWT_SECRET}",
    "algorithm": "HS256",
    "expiration_hours": 24,
    "refresh_expiration_days": 7
}
EOF
)
    
    create_or_update_secret \
        "${PROJECT_NAME}/${ENVIRONMENT}/jwt" \
        "$JWT_SECRET_JSON" \
        "JWT signing secret for ${PROJECT_NAME} ${ENVIRONMENT}"
    echo ""
    
    # ==========================================
    # Stripe API Keys
    # ==========================================
    echo -e "${BLUE}[4/5] Stripe API Keys${NC}"
    
    read -p "  Stripe Secret Key [sk_test_xxx or sk_live_xxx]: " STRIPE_SECRET_KEY
    if [ -z "$STRIPE_SECRET_KEY" ]; then
        STRIPE_SECRET_KEY="sk_test_placeholder_replace_me"
        echo -e "  ${YELLOW}Using placeholder - remember to update with real key${NC}"
    fi
    
    read -p "  Stripe Webhook Secret [whsec_xxx]: " STRIPE_WEBHOOK_SECRET
    if [ -z "$STRIPE_WEBHOOK_SECRET" ]; then
        STRIPE_WEBHOOK_SECRET="whsec_placeholder_replace_me"
        echo -e "  ${YELLOW}Using placeholder - remember to update with real key${NC}"
    fi
    
    STRIPE_SECRET=$(cat <<EOF
{
    "secret_key": "${STRIPE_SECRET_KEY}",
    "webhook_secret": "${STRIPE_WEBHOOK_SECRET}",
    "api_version": "2023-10-16"
}
EOF
)
    
    create_or_update_secret \
        "${PROJECT_NAME}/${ENVIRONMENT}/stripe" \
        "$STRIPE_SECRET" \
        "Stripe API credentials for ${PROJECT_NAME} ${ENVIRONMENT}"
    echo ""
    
    # ==========================================
    # OpenSearch Credentials
    # ==========================================
    echo -e "${BLUE}[5/5] OpenSearch Credentials${NC}"
    
    OPENSEARCH_HOST=$(prompt_or_generate "  OpenSearch Host" "https://${PROJECT_NAME}-${ENVIRONMENT}.${AWS_REGION}.es.amazonaws.com" "false")
    OPENSEARCH_USERNAME=$(prompt_or_generate "  OpenSearch Username" "admin" "false")
    
    echo -n "  OpenSearch Password [Press Enter to auto-generate]: "
    read -s OPENSEARCH_PASSWORD
    echo ""
    if [ -z "$OPENSEARCH_PASSWORD" ]; then
        OPENSEARCH_PASSWORD=$(generate_password)
        echo -e "  ${GREEN}Auto-generated password${NC}"
    fi
    
    OPENSEARCH_SECRET=$(cat <<EOF
{
    "host": "${OPENSEARCH_HOST}",
    "port": 443,
    "username": "${OPENSEARCH_USERNAME}",
    "password": "${OPENSEARCH_PASSWORD}",
    "ssl": true
}
EOF
)
    
    create_or_update_secret \
        "${PROJECT_NAME}/${ENVIRONMENT}/opensearch" \
        "$OPENSEARCH_SECRET" \
        "OpenSearch credentials for ${PROJECT_NAME} ${ENVIRONMENT}"
    echo ""
}

# Function to display summary
display_summary() {
    echo -e "${GREEN}=============================================${NC}"
    echo -e "${GREEN}  Secrets Created Successfully!${NC}"
    echo -e "${GREEN}=============================================${NC}"
    echo ""
    echo -e "${BLUE}Created secrets:${NC}"
    echo "  • ${PROJECT_NAME}/${ENVIRONMENT}/database"
    echo "  • ${PROJECT_NAME}/${ENVIRONMENT}/redis"
    echo "  • ${PROJECT_NAME}/${ENVIRONMENT}/jwt"
    echo "  • ${PROJECT_NAME}/${ENVIRONMENT}/stripe"
    echo "  • ${PROJECT_NAME}/${ENVIRONMENT}/opensearch"
    echo ""
    echo -e "${YELLOW}To view a secret:${NC}"
    echo "  aws secretsmanager get-secret-value --secret-id ${PROJECT_NAME}/${ENVIRONMENT}/database --region ${AWS_REGION}"
    echo ""
    echo -e "${YELLOW}To list all secrets:${NC}"
    echo "  aws secretsmanager list-secrets --filter Key=name,Values=${PROJECT_NAME}/${ENVIRONMENT} --region ${AWS_REGION}"
    echo ""
    echo -e "${RED}IMPORTANT:${NC}"
    echo "  1. Update placeholder values for Stripe keys with real credentials"
    echo "  2. Update database/redis/opensearch hosts after Terraform deployment"
    echo "  3. Store the auto-generated passwords securely"
    echo ""
}

# Run the script
check_aws_cli
setup_secrets
display_summary
