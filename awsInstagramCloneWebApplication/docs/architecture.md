# Architecture Design

## Overview
The AWS Instagram Clone is a microservices-based application designed for scalability and maintainability. It uses a modern tech stack with Spring Boot for the backend, React for the frontend, and AWS services for infrastructure.

## System Architecture

### Components
1.  **Client (Frontend)**: React 19 application served via Nginx.
2.  **API Gateway**: Spring Cloud Gateway acting as the single entry point, handling routing and cross-cutting concerns.
3.  **Microservices**:
    *   **Auth Service**: User identity and access management (JWT).
    *   **User Service**: User profiles and social graph (followers/following).
    *   **Post Service**: Content management (posts, media).
    *   **Feed Service**: Feed generation and retrieval (Fan-out on write).
    *   **Notification Service**: Real-time user notifications.
    *   **AI Service**: GenAI integration for content assistance.
4.  **Data Stores**:
    *   **PostgreSQL**: Relational data for Users, Posts, Notifications.
    *   **Redis**: In-memory cache for Feeds.
    *   **S3**: Object storage for images/media.
    *   **OpenSearch**: Log aggregation and search.

### Diagram
[Client] -> [API Gateway]
                |
    ----------------------------------------------------------------
    |             |             |             |             |      |
[Auth]       [User]        [Post]        [Feed]    [Notification] [AI]
    |             |             |             |             |
  [DB]          [DB]        [DB, S3]      [Redis]         [DB]

## Infrastructure
- **Compute**: AWS EKS (Elastic Kubernetes Service).
- **Networking**: VPC with Public/Private subnets, NAT Gateway.
- **IaC**: Terraform for infrastructure provisioning.
- **GitOps**: Argo CD for continuous deployment.
