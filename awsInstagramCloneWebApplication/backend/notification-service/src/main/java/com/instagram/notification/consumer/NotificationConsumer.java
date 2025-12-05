package com.instagram.notification.consumer;

import com.instagram.notification.service.NotificationService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @SqsListener("notification-queue")
    public void listen(String message) {
        log.info("Received message from SQS: {}", message);
        // In a real app, we would parse the JSON message to extract details
        // For now, we'll just log it and maybe create a dummy notification
        // Example message: {"recipientId": 1, "message": "Someone liked your post", "type": "LIKE", "relatedEntityId": 123}
        
        // Mock parsing logic
        // notificationService.createNotification(recipientId, messageContent, type, relatedId);
    }
}
