package com.instagram.post.controller;

import com.instagram.post.dto.PostDto;
import com.instagram.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto.PostResponse> createPost(
            @RequestParam("userId") Long userId,
            @RequestParam("caption") String caption,
            @RequestParam("image") MultipartFile image
    ) {
        return ResponseEntity.ok(postService.createPost(userId, caption, image));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDto.PostResponse>> getPostsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto.PostResponse> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }
}
