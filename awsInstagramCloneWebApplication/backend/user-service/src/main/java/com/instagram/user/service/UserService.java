package com.instagram.user.service;

import com.instagram.user.dto.UserDto;
import com.instagram.user.entity.Follow;
import com.instagram.user.entity.UserProfile;
import com.instagram.user.repository.FollowRepository;
import com.instagram.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;

    public UserDto.UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(profile);
    }

    public UserDto.UserProfileResponse getUserProfileByUsername(String username) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(profile);
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return; // Already following
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        followRepository.save(follow);

        // Update counts
        userProfileRepository.findByUserId(followerId).ifPresent(p -> {
            p.setFollowingCount(p.getFollowingCount() + 1);
            userProfileRepository.save(p);
        });
        userProfileRepository.findByUserId(followingId).ifPresent(p -> {
            p.setFollowersCount(p.getFollowersCount() + 1);
            userProfileRepository.save(p);
        });
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("Not following"));
        followRepository.delete(follow);

        // Update counts
        userProfileRepository.findByUserId(followerId).ifPresent(p -> {
            p.setFollowingCount(Math.max(0, p.getFollowingCount() - 1));
            userProfileRepository.save(p);
        });
        userProfileRepository.findByUserId(followingId).ifPresent(p -> {
            p.setFollowersCount(Math.max(0, p.getFollowersCount() - 1));
            userProfileRepository.save(p);
        });
    }

    @Transactional
    public UserDto.UserProfileResponse createProfile(Long userId, String username, String email, String fullName) {
        if (userProfileRepository.findByUserId(userId).isPresent()) {
             throw new RuntimeException("Profile already exists");
        }
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .username(username)
                .fullName(fullName)
                .followersCount(0)
                .followingCount(0)
                .postsCount(0)
                .build();
        return mapToResponse(userProfileRepository.save(profile));
    }

    private UserDto.UserProfileResponse mapToResponse(UserProfile profile) {
        return UserDto.UserProfileResponse.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .followersCount(profile.getFollowersCount())
                .followingCount(profile.getFollowingCount())
                .postsCount(profile.getPostsCount())
                .build();
    }
}
