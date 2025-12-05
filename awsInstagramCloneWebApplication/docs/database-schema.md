# Database Schema Design

## User Service (PostgreSQL)
**Table: users**
- `id`: BIGSERIAL (PK)
- `username`: VARCHAR (Unique)
- `email`: VARCHAR (Unique)
- `password`: VARCHAR
- `full_name`: VARCHAR
- `bio`: TEXT
- `profile_picture_url`: VARCHAR
- `created_at`: TIMESTAMP

**Table: follows**
- `id`: BIGSERIAL (PK)
- `follower_id`: BIGINT (FK -> users.id)
- `following_id`: BIGINT (FK -> users.id)
- `created_at`: TIMESTAMP

## Post Service (PostgreSQL)
**Table: posts**
- `id`: BIGSERIAL (PK)
- `user_id`: BIGINT
- `image_url`: VARCHAR
- `caption`: TEXT
- `created_at`: TIMESTAMP
- `likes_count`: INT
- `comments_count`: INT

## Notification Service (PostgreSQL)
**Table: notifications**
- `id`: BIGSERIAL (PK)
- `recipient_id`: BIGINT
- `sender_id`: BIGINT
- `type`: VARCHAR (LIKE, COMMENT, FOLLOW)
- `message`: TEXT
- `related_entity_id`: BIGINT (PostId, UserId)
- `is_read`: BOOLEAN
- `created_at`: TIMESTAMP
