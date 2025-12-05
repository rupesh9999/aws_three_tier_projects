# AWS Instagram Clone

A production-grade, microservices-based social media application built on AWS.

## Architecture
The application follows a microservices architecture pattern:

- **Frontend**: React 19, TypeScript, Tailwind CSS, Vite.
- **Backend**: Spring Boot 3, Java 21.
- **Database**: PostgreSQL (User, Post, Notification), Redis (Feed), S3 (Media).
- **Infrastructure**: Kubernetes (EKS), Helm, Argo CD.
- **Observability**: Prometheus, Grafana, Fluent Bit, OpenSearch.

## Microservices
1.  **Auth Service**: Handles user registration, login, and JWT token generation.
2.  **User Service**: Manages user profiles, followers, and following.
3.  **Post Service**: Handles post creation, media upload, and post retrieval.
4.  **Feed Service**: Aggregates posts from followed users to create a personalized feed.
5.  **Notification Service**: Manages real-time notifications for likes, comments, and follows.
6.  **AI Service**: Provides AI-powered features like caption generation and story ideas.
7.  **API Gateway**: Central entry point for all client requests.

## Getting Started

### Prerequisites
- Java 21
- Node.js 20+
- Docker
- Kubernetes Cluster (EKS or Minikube)
- Helm
- kubectl

### Running Locally
See the [EXECUTION-GUIDE.md](./EXECUTION-GUIDE.md) for detailed instructions on how to build, deploy, and run the application.

## CI/CD
The project includes a `Jenkinsfile` for building, testing, and pushing Docker images to a registry.

## GitOps
Deployment is managed via Argo CD. Application manifests are located in `k8s/argocd/`.
