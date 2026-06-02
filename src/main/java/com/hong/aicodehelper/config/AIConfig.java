package com.hong.aicodehelper.config;

import com.hong.aicodehelper.service.DocumentLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel,
                                    DocumentLoaderService documentLoaderService) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        var documents = documentLoaderService.loadAndSplitDocuments();
        if (!documents.isEmpty()) {
            log.info("Indexing {} document chunks into vector store...", documents.size());
            vectorStore.add(documents);
            log.info("Vector store indexing complete.");
        } else {
            log.warn("No documents found in classpath:docs/ -- RAG will return without context.");
        }

        return vectorStore;
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        new SimpleLoggerAdvisor()
                )
                .defaultSystem("""
                        You are a helpful assistant that answers questions based on the provided documents.
                        If the answer cannot be found in the documents, say so honestly.
                        Always cite which document your information comes from when possible.
                        """)
                .build();
    }
}
