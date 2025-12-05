package com.instagram.ai.service;

import com.instagram.ai.dto.AiDto;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ContentGenerationService {

    private final Random random = new Random();

    public AiDto.GenerationResponse generateCaption(AiDto.CaptionRequest request) {
        // Mock LLM call
        simulateLatency();
        String[] captions = {
                "Living my best life! ✨",
                "Just another day in paradise 🌴",
                "Good vibes only ✌️",
                "Throwback to this amazing moment 📸",
                "Can't believe this happened! 😲"
        };
        String generatedCaption = captions[random.nextInt(captions.length)];
        
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            generatedCaption += " (" + request.getContext() + ")";
        }

        return AiDto.GenerationResponse.builder()
                .content(generatedCaption)
                .build();
    }

    public AiDto.GenerationResponse generateStoryIdea() {
        // Mock LLM call
        simulateLatency();
        String[] ideas = {
                "Poll: Pizza vs Burger? 🍕🍔",
                "Quiz: Guess the song! 🎵",
                "Ask me anything! ❓",
                "Behind the scenes of my day 🎥",
                "Throwback Thursday! 🔙"
        };
        return AiDto.GenerationResponse.builder()
                .content(ideas[random.nextInt(ideas.length)])
                .build();
    }

    private void simulateLatency() {
        try {
            Thread.sleep(500 + random.nextInt(1000)); // 0.5 - 1.5s latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
