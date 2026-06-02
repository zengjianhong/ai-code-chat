package com.hong.aicodechat.controller;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : UUID.randomUUID().toString().replace("-", "");
        log.info("Chat request [{}]: {}", conversationId, request.getQuestion());

        String answer = chatClient.prompt()
                .user(request.getQuestion())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return ChatResponse.builder()
                .answer(answer)
                .conversationId(conversationId)
                .build();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : UUID.randomUUID().toString().replace("-", "");
        log.info("Streaming chat request [{}]: {}", conversationId, request.getQuestion());

        return chatClient.prompt()
                .user(request.getQuestion())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    @Data
    public static class ChatRequest {
        private String question;
        private String conversationId;
    }

    @Data
    @Builder
    public static class ChatResponse {
        private String answer;
        private String conversationId;
    }
}
