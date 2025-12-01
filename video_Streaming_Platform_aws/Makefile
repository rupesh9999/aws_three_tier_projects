# StreamFlix Makefile
# Provides easy-to-use commands for development, testing, and deployment

.PHONY: help build test clean deploy dev prod
.DEFAULT_GOAL := help

# Variables
DOCKER_COMPOSE = docker compose
MVN = mvn
NPM = npm

# Colors for output
GREEN = \033[0;32m
YELLOW = \033[1;33m
NC = \033[0m # No Color

help: ## Show this help message
	@echo "StreamFlix - Netflix-like Streaming Platform"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

# =============================================================================
# Development Commands
# =============================================================================

dev: ## Start all services in development mode
	@echo "$(GREEN)Starting StreamFlix in development mode...$(NC)"
	$(DOCKER_COMPOSE) up -d postgres redis elasticsearch localstack
	@echo "Waiting for services to be ready..."
	@sleep 10
	$(DOCKER_COMPOSE) up -d

dev-frontend: ## Start only the frontend in development mode
	@echo "$(GREEN)Starting frontend development server...$(NC)"
	cd frontend && $(NPM) run dev

dev-backend: ## Start backend services with hot reload
	@echo "$(GREEN)Starting backend services...$(NC)"
	cd backend && $(MVN) spring-boot:run -pl api-gateway &
	cd backend && $(MVN) spring-boot:run -pl auth-service &
	cd backend && $(MVN) spring-boot:run -pl content-service &

stop: ## Stop all running services
	@echo "$(YELLOW)Stopping all services...$(NC)"
	$(DOCKER_COMPOSE) down

restart: stop dev ## Restart all services

logs: ## View logs for all services
	$(DOCKER_COMPOSE) logs -f

logs-api: ## View API Gateway logs
	$(DOCKER_COMPOSE) logs -f api-gateway

logs-auth: ## View Auth Service logs
	$(DOCKER_COMPOSE) logs -f auth-service

# =============================================================================
# Build Commands
# =============================================================================

build: build-frontend build-backend ## Build all projects

build-frontend: ## Build the frontend application
	@echo "$(GREEN)Building frontend...$(NC)"
	cd frontend && $(NPM) ci && $(NPM) run build

build-backend: ## Build all backend services
	@echo "$(GREEN)Building backend services...$(NC)"
	cd backend && $(MVN) clean package -DskipTests

build-docker: ## Build all Docker images
	@echo "$(GREEN)Building Docker images...$(NC)"
	$(DOCKER_COMPOSE) build

build-auth-service: ## Build auth service only
	@echo "$(GREEN)Building auth service...$(NC)"
	cd backend && $(MVN) clean package -pl auth-service -am -DskipTests

build-content-service: ## Build content service only
	@echo "$(GREEN)Building content service...$(NC)"
	cd backend && $(MVN) clean package -pl content-service -am -DskipTests

# =============================================================================
# Test Commands
# =============================================================================

test: test-frontend test-backend ## Run all tests

test-frontend: ## Run frontend tests
	@echo "$(GREEN)Running frontend tests...$(NC)"
	cd frontend && $(NPM) run test

test-frontend-coverage: ## Run frontend tests with coverage
	@echo "$(GREEN)Running frontend tests with coverage...$(NC)"
	cd frontend && $(NPM) run test:coverage

test-backend: ## Run backend tests
	@echo "$(GREEN)Running backend tests...$(NC)"
	cd backend && $(MVN) test

test-backend-integration: ## Run backend integration tests
	@echo "$(GREEN)Running backend integration tests...$(NC)"
	cd backend && $(MVN) verify -Pintegration-tests

lint: lint-frontend lint-backend ## Run all linters

lint-frontend: ## Lint frontend code
	@echo "$(GREEN)Linting frontend...$(NC)"
	cd frontend && $(NPM) run lint

lint-backend: ## Lint backend code
	@echo "$(GREEN)Linting backend...$(NC)"
	cd backend && $(MVN) checkstyle:check spotbugs:check

# =============================================================================
# Database Commands
# =============================================================================

db-migrate: ## Run database migrations
	@echo "$(GREEN)Running database migrations...$(NC)"
	cd backend && $(MVN) flyway:migrate -pl auth-service
	cd backend && $(MVN) flyway:migrate -pl content-service
	cd backend && $(MVN) flyway:migrate -pl playback-service

db-reset: ## Reset and reinitialize database
	@echo "$(YELLOW)Resetting database...$(NC)"
	$(DOCKER_COMPOSE) down -v
	$(DOCKER_COMPOSE) up -d postgres
	@sleep 5
	make db-migrate

db-shell: ## Connect to PostgreSQL shell
	$(DOCKER_COMPOSE) exec postgres psql -U streamflix -d streamflix

# =============================================================================
# Infrastructure Commands
# =============================================================================

infra-init: ## Initialize Terraform
	@echo "$(GREEN)Initializing Terraform...$(NC)"
	cd infrastructure/terraform && terraform init

infra-plan: ## Plan infrastructure changes
	@echo "$(GREEN)Planning infrastructure changes...$(NC)"
	cd infrastructure/terraform && terraform plan

infra-apply: ## Apply infrastructure changes
	@echo "$(YELLOW)Applying infrastructure changes...$(NC)"
	cd infrastructure/terraform && terraform apply

infra-destroy: ## Destroy infrastructure
	@echo "$(YELLOW)Destroying infrastructure...$(NC)"
	cd infrastructure/terraform && terraform destroy

k8s-apply: ## Apply Kubernetes manifests
	@echo "$(GREEN)Applying Kubernetes manifests...$(NC)"
	kubectl apply -f infrastructure/k8s/base/
	kubectl apply -f infrastructure/k8s/services/
	kubectl apply -f infrastructure/k8s/networking/

k8s-status: ## Check Kubernetes deployment status
	kubectl get pods -n streamflix
	kubectl get services -n streamflix

# =============================================================================
# Clean Commands
# =============================================================================

clean: clean-frontend clean-backend ## Clean all build artifacts

clean-frontend: ## Clean frontend build artifacts
	@echo "$(GREEN)Cleaning frontend...$(NC)"
	cd frontend && rm -rf node_modules dist .vite coverage

clean-backend: ## Clean backend build artifacts
	@echo "$(GREEN)Cleaning backend...$(NC)"
	cd backend && $(MVN) clean

clean-docker: ## Remove all Docker images and volumes
	@echo "$(YELLOW)Cleaning Docker resources...$(NC)"
	$(DOCKER_COMPOSE) down -v --rmi all
	docker system prune -f

# =============================================================================
# Documentation
# =============================================================================

docs: ## Generate documentation
	@echo "$(GREEN)Generating documentation...$(NC)"
	cd backend && $(MVN) javadoc:aggregate
	@echo "API documentation available in backend/target/site/apidocs"

docs-api: ## Open API documentation
	@echo "$(GREEN)Opening API documentation...$(NC)"
	open http://localhost:8080/swagger-ui.html

# =============================================================================
# Utility Commands
# =============================================================================

install: ## Install all dependencies
	@echo "$(GREEN)Installing dependencies...$(NC)"
	cd frontend && $(NPM) ci
	cd backend && $(MVN) dependency:resolve

update: ## Update all dependencies
	@echo "$(GREEN)Updating dependencies...$(NC)"
	cd frontend && $(NPM) update
	cd backend && $(MVN) versions:use-latest-releases

shell-redis: ## Connect to Redis CLI
	$(DOCKER_COMPOSE) exec redis redis-cli

shell-api: ## Shell into API Gateway container
	$(DOCKER_COMPOSE) exec api-gateway /bin/sh

health: ## Check health of all services
	@echo "$(GREEN)Checking service health...$(NC)"
	@curl -s http://localhost:8080/actuator/health | jq . || echo "API Gateway: DOWN"
	@curl -s http://localhost:8081/actuator/health | jq . || echo "Auth Service: DOWN"
	@curl -s http://localhost:8082/actuator/health | jq . || echo "Content Service: DOWN"
	@curl -s http://localhost:8084/actuator/health | jq . || echo "Playback Service: DOWN"
	@curl -s http://localhost:3000/health || echo "Frontend: DOWN"
