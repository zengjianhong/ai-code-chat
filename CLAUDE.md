# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## 项目概述

AI Code Chat — 基于 Spring AI 2.0 + RAG 的智能编程问答系统，前端 Vue3 + Vite，后端 Spring Boot。

## 常用命令

### 后端
```bash
# 编译
./mvnw compile

# 启动 (Spring Boot, 端口 8080, profile: local)
./mvnw spring-boot:run

# 注意: 需要配置 src/main/resources/application-local.yaml (API Key 等)
```

### 前端
```bash
cd ai-code-chat-frontend
npm install    # 安装依赖
npm run dev    # 启动开发服务器 (端口 3000)
```

### 测试 API
```bash
# 同步问答
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"你的问题"}'

# 流式问答 (SSE)
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"question":"你的问题"}'
```

## 技术栈

| 层面 | 技术 |
|---|---|
| JDK | 21 |
| 框架 | Spring Boot 4.0.6 |
| AI | Spring AI 2.0.0-M8 (ChatClient, VectorStore, MCP Client, ChatMemory) |
| LLM | DeepSeek V4 Pro ( OpenAI-compatible ) |
| Embedding | all-MiniLM-L6-v2 (本地 ONNX, 384 维) |
| 前端 | Vue 3 + Vite + Marked |
| 构建 | Maven (后端) + npm (前端) |

## 后端架构

```
ChatController
    │
    ▼
ChatClient
    │
    ├── MessageChatMemoryAdvisor  (多轮对话记忆, 窗口 20 条)
    │       └── MessageWindowChatMemory
    │
    ├── QuestionAnswerAdvisor  (RAG 文档检索)
    │       └── SimpleVectorStore  (内存向量库)
    │             └── TransformersEmbeddingModel  (本地 ONNX)
    │
    └── ToolCallbackProvider
          ├── getWeather  (查询天气, FunctionToolCallback)
          └── MCP Tools  (外部 MCP 服务器, SyncMcpToolCallbackProvider)
```

- `DocumentLoaderService` — 启动时扫描 `src/main/resources/docs/*`，`TokenTextSplitter` 分片 (chunk=500)，向量化后写入 VectorStore
- `WeatherTool` — 调用 wttr.in 免费 API，LLM 自动决定何时调用
- `WebConfig` — CORS 配置，允许 localhost 跨域

## 配置文件

- `application.yaml` — 主配置，设置 `spring.profiles.active: local`
- `application-local.yaml` — **本地配置，已 gitignore**，包含 API Key、MCP 连接等敏感信息。模板:

```yaml
spring:
  ai:
    openai:
      api-key: sk-xxx
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-v4-pro
    mcp:
      client:
        enabled: true
        type: SYNC
```

## 前端架构

```
ai-code-chat-frontend/src/
├── main.js              # Vue 入口
├── App.vue              # 主布局 (侧边栏 + 聊天区 + 输入框)
├── api/
│   └── chat.js          # SSE 流式请求 (Fetch API, AbortController)
└── components/
    ├── ChatSidebar.vue  # 会话列表 (localStorage 持久化)
    ├── ChatMessage.vue  # 消息气泡 (Markdown 渲染)
    └── ChatInput.vue    # 输入框 + 发送/停止按钮
```

数据流:
1. 用户输入 → `App.vue handleSend()` → `chat.js sendMessage()` → `POST /api/chat/stream`
2. SSE 流式读取 → 逐 token 解析 `data:` 行 → `requestAnimationFrame` 逐帧渲染
3. 对话结束 → 保存到 `conversations` ref → `localStorage` 持久化
