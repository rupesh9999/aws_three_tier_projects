package com.instagram.feed.service;

import com.instagram.feed.dto.FeedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final RedisTemplate<String, Object> redisTemplate;
    // In a real microservices architecture, we would use FeignClient or WebClient to call Post Service and User Service
    // For this MVP, we will simulate fetching data

    public FeedDto.FeedResponse getUserFeed(Long userId) {
        String feedKey = "user:feed:" + userId;
        
        // Try to get from cache
        // List<FeedDto.PostSummary> cachedFeed = (List<FeedDto.PostSummary>) redisTemplate.opsForValue().get(feedKey);
        // if (cachedFeed != null) {
        //     return FeedDto.FeedResponse.builder().posts(cachedFeed).build();
        // }

        // Fallback: Fetch from Post Service (Simulated)
        List<FeedDto.PostSummary> feed = generateMockFeed();

        // Cache the feed
        redisTemplate.opsForValue().set(feedKey, feed, 10, TimeUnit.MINUTES);

        return FeedDto.FeedResponse.builder().posts(feed).build();
    }

    private List<FeedDto.PostSummary> generateMockFeed() {
        List<FeedDto.PostSummary> posts = new ArrayList<>();
        posts.add(FeedDto.PostSummary.builder()
                .id(1L)
                .userId(101L)
                .username("john_doe")
                .imageUrl("https://via.placeholder.com/600")
                .caption("Beautiful sunset!")
                .likesCount(120)
                .commentsCount(15)
                .createdAt("2023-10-27T10:00:00")
                .build());
        posts.add(FeedDto.PostSummary.builder()
                .id(2L)
                .userId(102L)
                .username("jane_smith")
                .imageUrl("https://via.placeholder.com/600")
                .caption("Coffee time ☕")
                .likesCount(85)
                .commentsCount(5)
                .createdAt("2023-10-27T09:30:00")
                .build());
        return posts;
    }
}
