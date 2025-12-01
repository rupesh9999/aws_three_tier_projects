#!/bin/bash

# Initialize LocalStack services for StreamFlix

echo "Initializing LocalStack services..."

# Create S3 buckets
awslocal s3 mb s3://streamflix-source-content
awslocal s3 mb s3://streamflix-transcoded-content
awslocal s3 mb s3://streamflix-static-assets

# Set CORS for S3 buckets
awslocal s3api put-bucket-cors --bucket streamflix-transcoded-content --cors-configuration '{
  "CORSRules": [{
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "HEAD"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3600
  }]
}'

# Create SQS queues
awslocal sqs create-queue --queue-name streamflix-transcoding-queue
awslocal sqs create-queue --queue-name streamflix-notification-queue
awslocal sqs create-queue --queue-name streamflix-analytics-queue

# Create dead letter queues
awslocal sqs create-queue --queue-name streamflix-transcoding-dlq
awslocal sqs create-queue --queue-name streamflix-notification-dlq
awslocal sqs create-queue --queue-name streamflix-analytics-dlq

# Verify SES identity (simulated)
awslocal ses verify-email-identity --email-address noreply@streamflix.local

echo "LocalStack initialization complete!"
echo ""
echo "S3 Buckets:"
awslocal s3 ls
echo ""
echo "SQS Queues:"
awslocal sqs list-queues
