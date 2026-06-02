# AI Code Chat

基于 Spring AI 2.0 + RAG 技术的智能问答系统，能够基于本地文档知识库回答用户问题，支持多轮对话记忆和 MCP 工具调用。

## 技术栈

- **JDK 21**
- **Spring Boot 4.0.6**
- **Spring AI 2.0.0-M8**（RAG + ChatClient + VectorStore + MCP Client + ChatMemory）
- **DeepSeek V4 Pro**（LLM 对话模型）
- **all-MiniLM-L6-v2**（本地 ONNX Embedding 模型）
- **Maven**

## 快速开始

### 1. 配置 API Key

编辑 `src/main/resources/application-local.yaml`，填入 DeepSeek API Key：

```yaml
spring:
  ai:
    openai:
      api-key: sk-your-key-here
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-v4-pro
          temperature: 0.7
```

### 2. 添加知识文档

将需要检索的文档放入 `src/main/resources/docs/` 目录，支持 `.md`、`.txt` 格式。应用启动时自动加载、分片并写入向量库。

### 3. 启动应用

```bash
./mvnw spring-boot:run
```

应用默认运行在 `http://localhost:8080`。

## API 接口

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/chat` | POST | 同步问答，支持多轮对话记忆 |
| `/api/chat/stream` | POST | 流式问答，SSE 格式逐字返回 |

**请求体：**

```json
{
  "question": "你的问题",
  "conversationId": "可选，传入则复用历史上下文，不传则自动生成"
}
```

**响应体：**

```json
{
  "answer": "基于文档和上下文的答案",
  "conversationId": "会话ID，后续对话传入此值可保持记忆"
}
```

### 单轮对话示例

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Java 的学习路线是什么？"}'
```

### 多轮对话示例

```bash
# 第一轮 — 设置个人信息
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "我叫张三，是一名后端工程师", "conversationId": "my-session"}'

# 第二轮 — 模型记住上下文
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "我叫什么名字？我的职业是什么？", "conversationId": "my-session"}'
```

## 功能特性

### RAG 文档问答

`QuestionAnswerAdvisor` 自动从向量库检索相关文档片段，注入 Prompt 后由 LLM 生成答案，答案会引用文档来源。

### 多轮对话记忆

`MessageChatMemoryAdvisor` + `MessageWindowChatMemory` 实现窗口式对话记忆（默认保留最近 20 条消息）。传入 `conversationId` 即可跨请求保持上下文，不传则自动生成新会话 ID。

### MCP 客户端

项目集成了 `spring-ai-starter-mcp-client`，启动时自动连接配置的 MCP 服务器，将其工具注册到 `ChatClient`，LLM 可在对话中自动调用外部工具。

**MCP 服务器推荐资源：**
- [mcp.so](https://mcp.so) — MCP 服务器发现平台
- [OpenTools](https://opentools.com) — MCP 工具市场
- [GitHub MCP Server](https://github.com/github/github-mcp-server) — GitHub 操作集成
- [Brave Search MCP](https://brave.com/search/api/) — 联网搜索
- [Filesystem MCP](https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem) — 文件系统操作

在 `application-local.yaml` 中配置 MCP 连接：

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        type: SYNC
        streamable-http:
          connections:
            weather:
              url: http://localhost:8080
              endpoint: /mcp
```

## 项目结构

```
src/main/java/com/hong/aicodechat/
├── AiCodeChatApplication.java    # 启动类
├── config/
│   └── AIConfig.java               # VectorStore、ChatClient、ChatMemory、MCP Bean 配置
├── controller/
│   └── ChatController.java         # 问答接口（支持 conversationId）
└── service/
    └── DocumentLoaderService.java  # 文档加载、分片服务

src/main/resources/
├── application.yaml                # 主配置
├── application-local.yaml          # 本地配置（API Key + MCP，已 gitignore）
└── docs/                           # 知识库文档目录
```

## 架构

```
POST /api/chat
       │
       ▼
  ChatController
       │
       ▼
   ChatClient ── MessageChatMemoryAdvisor ── ChatMemory (对话记忆，窗口 20 条)
       │
       ├── QuestionAnswerAdvisor ── VectorStore (SimpleVectorStore)
       │                                    │
       │                                   EmbeddingModel (本地 ONNX)
       │
       ├── SyncMcpToolCallbackProvider ── MCP Servers (外部工具)
       │
       ▼
   ChatModel (DeepSeek V4 Pro)
       │
       ▼
  RAG + Memory + MCP 增强后的回答
```

启动时 `DocumentLoaderService` 扫描 `docs/` 目录 → `TokenTextSplitter` 分片 → `EmbeddingModel` 向量化 → 存入 `SimpleVectorStore`。问答时 `MessageChatMemoryAdvisor` 注入历史上下文 → `QuestionAnswerAdvisor` 检索相关文档片段 → LLM 生成答案，并可调用 MCP 工具获取外部数据。
