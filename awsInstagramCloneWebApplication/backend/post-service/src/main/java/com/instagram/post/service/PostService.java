package com.instagram.post.service;

import com.instagram.post.dto.PostDto;
import com.instagram.post.entity.Post;
import com.instagram.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    // private final S3Service s3Service; // To be implemented

    public PostDto.PostResponse createPost(Long userId, String caption, MultipartFile image) {
        // Mock S3 upload
        String imageUrl = uploadImageToS3(image);

        Post post = Post.builder()
                .userId(userId)
                .caption(caption)
                .imageUrl(imageUrl)
                .likesCount(0)
                .commentsCount(0)
                .build();
        
        post = postRepository.save(post);
        return mapToResponse(post);
    }

    public List<PostDto.PostResponse> getPostsByUserId(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PostDto.PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToResponse(post);
    }

    private String uploadImageToS3(MultipartFile image) {
        // Placeholder for S3 upload logic
        // In production, use AmazonS3 client to putObject
        return "https://s3.amazonaws.com/my-bucket/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
    }

    private PostDto.PostResponse mapToResponse(Post post) {
        return PostDto.PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt().toString())
                .build();
    }
}
