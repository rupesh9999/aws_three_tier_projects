package com.instagram.feed.controller;

import com.instagram.feed.dto.FeedDto;
import com.instagram.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{userId}")
    public ResponseEntity<FeedDto.FeedResponse> getUserFeed(@PathVariable Long userId) {
        return ResponseEntity.ok(feedService.getUserFeed(userId));
    }
}
