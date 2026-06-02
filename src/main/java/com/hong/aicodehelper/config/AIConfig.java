package com.hong.aicodehelper.config;

import com.hong.aicodehelper.service.DocumentLoaderService;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Slf4j
@Configuration
public class AIConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    @Primary
    public ToolCallbackProvider mcpToolCallbackProvider(List<McpSyncClient> mcpClients) {
        if (mcpClients.isEmpty()) {
            log.info("No MCP clients configured, skipping MCP tool registration.");
            return ToolCallbackProvider.from();
        }
        log.info("Registering MCP tools from {} client(s)", mcpClients.size());
        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpClients)
                .build();
    }

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
    public ChatClient chatClient(ChatModel chatModel, VectorStore vectorStore,
                                  ChatMemory chatMemory,
                                  ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        new SimpleLoggerAdvisor()
                )
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultSystem("""
                        You are a helpful assistant that answers questions based on the provided documents.
                        If the answer cannot be found in the documents, say so honestly.
                        Always cite which document your information comes from when possible.
                        You have access to MCP tools — use them when appropriate to fetch external data.
                        """)
                .build();
    }
}
