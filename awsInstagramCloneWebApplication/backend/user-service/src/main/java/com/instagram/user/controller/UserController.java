package com.instagram.user.controller;

import com.instagram.user.dto.UserDto;
import com.instagram.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto.UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto.UserProfileResponse> getUserProfileByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserProfileByUsername(username));
    }

    @PostMapping("/{userId}/follow/{followingId}")
    public ResponseEntity<Void> followUser(@PathVariable Long userId, @PathVariable Long followingId) {
        userService.followUser(userId, followingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/unfollow/{followingId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long userId, @PathVariable Long followingId) {
        userService.unfollowUser(userId, followingId);
        return ResponseEntity.ok().build();
    }

    // Internal endpoint for Auth Service to create profile on registration
    @PostMapping("/internal/create")
    public ResponseEntity<UserDto.UserProfileResponse> createProfile(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String fullName
    ) {
        return ResponseEntity.ok(userService.createProfile(userId, username, email, fullName));
    }
}
