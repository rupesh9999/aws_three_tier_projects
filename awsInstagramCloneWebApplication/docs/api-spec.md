# API Specifications

## Auth Service
- `POST /api/v1/auth/register`: Register a new user.
- `POST /api/v1/auth/login`: Authenticate user and return JWT.

## User Service
- `GET /api/v1/users/{userId}`: Get user profile.
- `PUT /api/v1/users/me`: Update current user profile.
- `POST /api/v1/users/{userId}/follow/{targetId}`: Follow a user.
- `POST /api/v1/users/{userId}/unfollow/{targetId}`: Unfollow a user.

## Post Service
- `POST /api/v1/posts`: Create a new post (multipart/form-data).
- `GET /api/v1/posts/user/{userId}`: Get posts by user.
- `GET /api/v1/posts/{postId}`: Get post details.

## Feed Service
- `GET /api/v1/feed`: Get current user's feed.

## Notification Service
- `GET /api/v1/notifications/{userId}`: Get user notifications.

## AI Service
- `POST /api/v1/ai/generate-caption`: Generate caption for an image.
- `GET /api/v1/ai/generate-story-idea`: Generate story ideas.
