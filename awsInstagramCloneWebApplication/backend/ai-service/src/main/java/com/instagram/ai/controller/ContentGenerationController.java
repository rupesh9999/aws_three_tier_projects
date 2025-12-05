package com.instagram.ai.controller;

import com.instagram.ai.dto.AiDto;
import com.instagram.ai.service.ContentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ContentGenerationController {

    private final ContentGenerationService contentService;

    @PostMapping("/generate-caption")
    public ResponseEntity<AiDto.GenerationResponse> generateCaption(@RequestBody AiDto.CaptionRequest request) {
        return ResponseEntity.ok(contentService.generateCaption(request));
    }

    @GetMapping("/generate-story-idea")
    public ResponseEntity<AiDto.GenerationResponse> generateStoryIdea() {
        return ResponseEntity.ok(contentService.generateStoryIdea());
    }
}
