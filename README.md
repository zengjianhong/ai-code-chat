# AI Code Helper

基于 Spring AI 2.0 + RAG 技术的智能问答系统，能够基于本地文档知识库回答用户问题。

## 技术栈

- **JDK 21**
- **Spring Boot 4.0.6**
- **Spring AI 2.0.0-M8**（RAG + ChatClient + VectorStore）
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

### 4. 调用接口

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Java 的学习路线是什么？"}'
```

响应示例：

```json
{
  "answer": "根据文档《Java 编程学习路线.md》，Java 学习路线分为..."
}
```

## API 接口

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/chat` | POST | 同步问答，返回完整答案 |
| `/api/chat/stream` | POST | 流式问答，SSE 格式逐字返回 |

**请求体：**

```json
{
  "question": "你的问题"
}
```

**响应体：**

```json
{
  "answer": "基于文档的答案"
}
```

## 项目结构

```
src/main/java/com/hong/aicodehelper/
├── AiCodeHelperApplication.java    # 启动类
├── config/
│   └── AIConfig.java               # VectorStore、ChatClient Bean 配置
├── controller/
│   └── ChatController.java         # 问答接口
└── service/
    └── DocumentLoaderService.java  # 文档加载、分片服务

src/main/resources/
├── application.yaml                # 主配置
├── application-local.yaml          # 本地配置（API Key，已 gitignore）
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
   ChatClient ── QuestionAnswerAdvisor ── VectorStore (SimpleVectorStore)
       │                                        │
       │                                   EmbeddingModel (本地 ONNX)
       │
   ChatModel (DeepSeek V4 Pro)
       │
       ▼
  RAG 增强后的回答
```

启动时 `DocumentLoaderService` 扫描 `docs/` 目录 → `TokenTextSplitter` 分片 → `EmbeddingModel` 向量化 → 存入 `SimpleVectorStore`。问答时 `QuestionAnswerAdvisor` 自动检索相关文档片段注入 Prompt，再由 LLM 生成答案。
