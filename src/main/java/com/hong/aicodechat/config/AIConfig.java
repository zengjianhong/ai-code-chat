package com.hong.aicodechat.config;

import com.hong.aicodechat.service.DocumentLoaderService;
import com.hong.aicodechat.tool.WeatherTool;
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
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public ToolCallbackProvider toolCallbackProvider(List<McpSyncClient> mcpClients,
                                                      WeatherTool weatherTool) {
        List<ToolCallback> callbacks = new ArrayList<>();

        // Register weather tool
        callbacks.add(FunctionToolCallback.builder("getWeather", weatherTool::getWeather)
                .description("查询指定城市的天气信息，返回温度、天气状况、湿度、风速等")
                .inputType(Map.class)
                .build());

        // Register MCP tools
        if (!mcpClients.isEmpty()) {
            log.info("Registering MCP tools from {} client(s)", mcpClients.size());
            var mcpProvider = SyncMcpToolCallbackProvider.builder()
                    .mcpClients(mcpClients)
                    .build();
            for (ToolCallback cb : mcpProvider.getToolCallbacks()) {
                callbacks.add(cb);
            }
        } else {
            log.info("No MCP clients configured.");
        }

        log.info("Total registered tools: {}", callbacks.size());
        return ToolCallbackProvider.from(callbacks);
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
                ).defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultSystem("""
                        You are a helpful assistant that answers questions based on the provided documents.
                        If the answer cannot be found in the documents, say so honestly.
                        Always cite which document your information comes from when possible.
                        You have access to MCP tools — use them when appropriate to fetch external data.
                        """)
                .build();
    }
}
