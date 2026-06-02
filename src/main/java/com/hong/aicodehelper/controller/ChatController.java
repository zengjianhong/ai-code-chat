package com.hong.aicodehelper.controller;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("Chat request: {}", request.getQuestion());
        String answer = chatClient.prompt()
                .user(request.getQuestion())
                .call()
                .content();
        return ChatResponse.builder()
                .answer(answer)
                .build();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        log.info("Streaming chat request: {}", request.getQuestion());
        return chatClient.prompt()
                .user(request.getQuestion())
                .stream()
                .content();
    }

    @Data
    public static class ChatRequest {
        private String question;
    }

    @Data
    @Builder
    public static class ChatResponse {
        private String answer;
    }
}
