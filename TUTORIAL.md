# Spring AI 2.0 学习教程

> 基于 Spring AI 2.0.0-M4 + Spring Boot 4.0.5 + Java 21
> 模型提供商：智谱GLM (glm-4.7-flash / embedding-3) via OpenAI 兼容 API
> 向量数据库：PGVector (PostgreSQL)

---

## 目录

- [ch01-chat-demo - 基础对话](#ch01-chat-demo---基础对话)
- [ch02-embeddings-demo - Embedding 与向量存储](#ch02-embeddings-demo---embedding-与向量存储)
- [ch03-key-polling - API Key 轮询](#ch03-key-polling---api-key-轮询)
- [ch04-function-call - 函数调用基础](#ch04-function-call---函数调用基础)
- [ch05-chat-memory - 对话记忆](#ch05-chat-memory---对话记忆)
- [ch06-advanced-tool-call - 高级工具调用](#ch06-advanced-tool-call---高级工具调用)
- [ch07-rag-knowledge - RAG 知识库问答](#ch07-rag-knowledge---rag-知识库问答)
- [ch08-mcp-client - MCP 客户端](#ch08-mcp-client---mcp-客户端)
- [ch09-mcp-server - MCP 服务端](#ch09-mcp-server---mcp-服务端)
- [ch10-multi-agent - 多智能体协作](#ch10-multi-agent---多智能体协作)
- [环境依赖](#环境依赖)

---

## ch01-chat-demo - 基础对话

**端口**: 8848 | **依赖**: spring-ai-starter-model-openai

### 知识点

| 主题 | 说明 |
|------|------|
| ChatClient | Spring AI 核心 API，流式构建器模式 |
| 同步调用 | `chatClient.prompt().user(msg).call().content()` |
| 流式调用 | `chatClient.prompt().user(msg).stream().content()` → `Flux<String>` |
| Prompt 模板 | 使用 StringTemplate (`.st` 文件) + 变量渲染 |
| 结构化输出 | `.call().entity(Book.class)` 将 LLM 输出自动映射为 Java 对象 |
| SimpleLoggerAdvisor | 内置日志拦截器，打印请求/响应 |
| 多模型切换 | 通过 `@Qualifier` 注入不同的 ChatModel |

### API 接口

```
GET /chat/openai?prompt=你好                     → 同步对话
GET /stream?prompt=讲个故事                        → 流式对话 (SSE)
GET /bean?author=余华                              → Prompt模板 + 结构化输出 (返回Book对象)
```

### 关键代码

```java
// 构建 ChatClient
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(new SimpleLoggerAdvisor())
    .build();

// 同步调用
String result = chatClient.prompt().user("你好").call().content();

// 结构化输出
Book book = chatClient.prompt().user(promptTemplate.render()).call().entity(Book.class);
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `ChatController.java` | REST 控制器，演示同步/流式/模板/结构化输出 |
| `Book.java` | 结构化输出的 POJO |
| `prompt.st` | StringTemplate 模板文件：`Which is {author}'s most popular book?` |

---

## ch02-embeddings-demo - Embedding 与向量存储

**端口**: 无 (测试模块) | **依赖**: spring-ai-starter-model-openai, spring-ai-starter-vector-store-pgvector, spring-ai-tika-document-reader

### 知识点

| 主题 | 说明 |
|------|------|
| Embedding 模型 | 将文本转换为高维向量（智谱 embedding-3, 维度 2048） |
| VectorStore | 向量存储抽象，PGVector 实现 |
| 文档入库 | `vectorStore.add(documents)` 将文档向量化后存入数据库 |
| 相似度搜索 | `vectorStore.similaritySearch(SearchRequest)` |
| TikaDocumentReader | 读取 PDF/DOCX 等多种格式文档 |
| TokenTextSplitter | 按 token 数切分长文档为小块 |
| 搜索阈值 | `similarityThreshold()` 过滤低相似度结果 |
| 文档删除 | `vectorStore.delete(List<id>)` 按 ID 删除 |

### 测试用例

```java
// 1. 添加文档
List<Document> docs = List.of(new Document("Spring AI 是...", Map.of("topic", "spring-ai")));
vectorStore.add(docs);

// 2. 文件入库 (PDF)
TikaDocumentReader reader = new TikaDocumentReader(resource);
List<Document> docs = reader.get();
tokenTextSplitter.transform(docs);  // 切分
vectorStore.add(docs);              // 入库

// 3. 相似度搜索
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder().query("什么是Spring AI").topK(3).build()
);

// 4. 带阈值搜索
vectorStore.similaritySearch(
    SearchRequest.builder().query("向量存储").topK(5).similarityThreshold(0.5).build()
);
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `VectorStoreTest.java` | 测试类：添加文档、文件入库、搜索、删除 |
| `DocumentTransferConfig.java` | TokenTextSplitter Bean 配置 |
| `test.pdf` | 测试用 PDF 文件 |

### 前置条件

需要运行 PostgreSQL + PGVector 扩展，数据库名 `spring-ai-study`。

---

## ch03-key-polling - API Key 轮询

**端口**: 无 (需查 application.yml) | **依赖**: spring-boot-starter-data-jpa, spring-ai-starter-model-openai (排除自动配置)

### 知识点

| 主题 | 说明 |
|------|------|
| 多 Key 管理 | 数据库存储多个 API Key，随机轮询使用 |
| 手动构建 ChatClient | 手动创建 OpenAiApi → OpenAiChatModel → ChatClient |
| 排除自动配置 | 禁用 Spring AI 自动配置，完全手动控制 |
| JPA 实体 | KeyInfo 存储每个 Key 的 base-url、model 等信息 |

### API 接口

```
GET /keypolling/chat?keyId=1    → 使用指定 Key 对话
```

### 关键代码

```java
// 从数据库随机选 Key
KeyInfo keyInfo = keyInfoRepository.findRandomKey();
// 手动构建 ChatClient
OpenAiApi api = OpenAiApi.builder().apiKey(keyInfo.getKey()).baseUrl(keyInfo.getBaseUrl()).build();
OpenAiChatModel model = OpenAiChatModel.builder().openAiApi(api).build();
ChatClient client = ChatClient.builder(model).build();
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `KeyInfo.java` | JPA 实体，存储 Key 信息 |
| `KeyInfoRepository.java` | JPA 仓库，含随机查询 |
| `ChatClientService.java` | 核心逻辑：选 Key → 构建 Client → 调用 |
| `ChatController.java` | REST 控制器 |

---

## ch04-function-call - 函数调用基础

**端口**: 8849 | **依赖**: spring-ai-starter-model-openai

### 知识点

| 主题 | 说明 |
|------|------|
| FunctionToolCallback | 编程式定义工具函数 |
| Function<Request, Response> | 工具类实现 Function 接口 |
| toolNames | 通过 `OpenAiChatOptions.builder().toolNames("toolName")` 注册工具 |
| 流式工具调用 | 流式模式下也支持工具调用 |

### API 接口

```
GET /fc?location=北京                  → 同步函数调用
GET /fc-stream?location=上海           → 流式函数调用
```

### 关键代码

```java
// 定义函数
public class CurrentTimeFunc implements Function<Request, Response> {
    public record Request(String location) {}
    public record Response(String time) {}

    @Override
    public Response apply(Request request) {
        return new Response(new Date(System.currentTimeMillis()).toString());
    }
}

// 注册为工具
@Bean
public ToolCallback currentTimeFunc() {
    return FunctionToolCallback.builder("currentTime", new CurrentTimeFunc())
        .description("Get the current time in location")
        .inputType(CurrentTimeFunc.Request.class)
        .build();
}

// 使用工具
Prompt prompt = new Prompt(messages, OpenAiChatOptions.builder().toolNames("currentTime").build());
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `CurrentTimeFunc.java` | 工具函数实现 |
| `FunctionCallConfig.java` | 注册 ToolCallback Bean |
| `FunctionCallController.java` | REST 控制器，演示同步/流式工具调用 |

---

## ch05-chat-memory - 对话记忆

**端口**: 8850 | **依赖**: spring-ai-starter-model-openai, spring-ai-starter-model-chat-memory-repository-jdbc, spring-boot-starter-jdbc, postgresql

### 知识点

| 主题 | 说明 |
|------|------|
| MessageWindowChatMemory | 滑动窗口记忆管理，保留最近 N 条消息 |
| InMemoryChatMemoryRepository | 内存存储，重启丢失（适合测试） |
| JdbcChatMemoryRepository | 数据库持久化（PostgreSQL），重启保留 |
| MessageChatMemoryAdvisor | 将历史消息作为消息列表注入（推荐，保留对话结构） |
| PromptChatMemoryAdvisor | 将历史消息拼接到系统提示文本中 |
| conversationId | 通过会话 ID 区分不同对话 |

### API 接口

```
# 方式一：MessageChatMemoryAdvisor + 内存
GET /chat/message?conversationId=user1&message=我叫张三，我是Java开发者
GET /chat/message?conversationId=user1&message=我叫什么名字？
GET /chat/message?conversationId=user1&message=我叫什么名字？（不同conversationId，不记得）

# 方式一流式
GET /chat/message-stream?conversationId=user1&message=继续聊

# 方式二：PromptChatMemoryAdvisor + 内存
GET /chat/prompt?conversationId=user1&message=我叫什么名字？

# 方式三：JDBC 持久化（重启后记忆仍在）
GET /chat/jdbc?conversationId=customer-001&message=我的订单号是ORD-20250101
GET /chat/jdbc?conversationId=customer-001&message=我的订单号是多少？
GET /chat/jdbc-stream?conversationId=customer-001&message=还记得我的订单号吗？
```

### 关键代码

```java
// 构建 ChatMemory
ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(new InMemoryChatMemoryRepository())
    .maxMessages(20)
    .build();

// JDBC 持久化
ChatMemory jdbcChatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(jdbcChatMemoryRepository)  // Spring AI 自动配置
    .maxMessages(20)
    .build();

// 使用 Advisor
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultSystem("你是一个友好的AI助手...")
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
    .build();

// 调用时指定 conversationId
chatClient.prompt()
    .user(message)
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
    .call()
    .content();
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `ChatMemoryConfig.java` | 配置三种 ChatClient：内存消息式、内存提示式、JDBC持久化 |
| `ChatMemoryController.java` | REST 控制器，6 个端点覆盖三种方案 |

### 前置条件

JDBC 模式需要 PostgreSQL，数据库 `spring-ai-study`。Spring AI 会自动建表。

---

## ch06-advanced-tool-call - 高级工具调用

**端口**: 8851 | **依赖**: spring-ai-starter-model-openai

### 知识点

| 主题 | 说明 |
|------|------|
| @Tool 注解 | 声明式定义工具，替代 FunctionToolCallback |
| @ToolParam 注解 | 描述工具参数，帮助模型理解参数含义 |
| ToolContext | 运行时上下文传递（如租户ID、用户信息） |
| returnDirect | `returnDirect=true` 工具结果直接返回给用户，不再经过 LLM |
| ToolCallbacks.from() | 从 @Tool 注解类批量生成 ToolCallback 数组 |
| defaultTools() | ChatClient Builder 注册默认工具 |
| 多工具协调 | 注册多个工具类，模型根据意图自动选择 |

### @Tool vs FunctionToolCallback 对比

| 特性 | FunctionToolCallback (ch04) | @Tool 注解 (ch06) |
|------|---------------------------|-------------------|
| 定义方式 | 实现 Function 接口 | 方法上加 @Tool |
| 参数描述 | 需要额外 inputType | @ToolParam 注解 |
| 多方法 | 一个类一个工具 | 一个类多个工具方法 |
| ToolContext | 不支持 | 方法参数注入 |
| returnDirect | 不支持 | @Tool(returnDirect=true) |
| 注册方式 | 手动构建 Bean | ToolCallbacks.from() 或 defaultTools() |

### API 接口

```
# 使用默认工具（OrderTools + ProductTools）
GET /tool/chat?message=帮我查一下订单ORD-001的状态         → returnDirect，直接返回
GET /tool/chat?message=搜索一下MacBook                      → 模型选择 ProductTools
GET /tool/chat?message=北京天气怎么样？顺便看看现在几点了    → 多工具协调
GET /tool/chat?message=帮我取消订单ORD-002，原因是买重复了  → 模型选择取消工具

# 运行时动态添加工具 + ToolContext
GET /tool/dynamic?message=查询订单ORD-001                   → 注入 tenantId 上下文

# 流式响应
GET /tool/stream?message=帮我查订单ORD-003，再看看有什么iPad
```

### 关键代码

```java
// 声明式工具定义
public class OrderTools {
    @Tool(description = "根据订单号查询订单信息", returnDirect = true)
    public String queryOrder(
        @ToolParam(description = "订单编号，格式为 ORD-XXX") String orderId,
        ToolContext toolContext) {
        String tenantId = (String) toolContext.getContext().get("tenantId");
        // ...
    }
}

// 注册工具
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultTools(new OrderTools(), new ProductTools())
    .build();

// 动态工具 + ToolContext
ToolCallback[] callbacks = ToolCallbacks.from(new OrderTools());
ChatClient.create(chatModel)
    .prompt()
    .tools(callbacks)
    .toolContext(Map.of("tenantId", "store-001"))
    .call()
    .content();
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `OrderTools.java` | 订单工具集：查询(returnDirect)、取消、天气、时间，演示 ToolContext |
| `ProductTools.java` | 商品工具集：搜索、查价格，演示多工具协调 |
| `ToolConfig.java` | 工具注册配置 |
| `AdvancedToolController.java` | REST 控制器，演示默认/动态/流式三种模式 |

---

## ch07-rag-knowledge - RAG 知识库问答

**端口**: 8852 | **依赖**: spring-ai-starter-model-openai, spring-ai-starter-vector-store-pgvector, spring-ai-pdf-document-reader, spring-ai-tika-document-reader, postgresql

### 知识点

| 主题 | 说明 |
|------|------|
| ETL Pipeline | Extract → Transform → Load 三步流水线 |
| PagePdfDocumentReader | 按页读取 PDF 文档 |
| TikaDocumentReader | 读取 DOCX、PPTX、HTML 等多种格式 |
| TokenTextSplitter | 按 token 数切分文档为小块（chunk） |
| VectorStore.add() | 文档向量化后写入 PGVector |
| 手动 RAG | 向量检索 → 拼接上下文 → 发送 LLM |
| SearchRequest | 配置 topK（返回数量）和 similarityThreshold（相似度阈值） |

### RAG 工作原理

```
用户提问
   ↓
① 向量搜索：用用户问题检索向量库，找到最相关的文档片段
   ↓
② 提示拼接：将检索到的文档作为"参考信息"拼接到用户问题前
   ↓
③ LLM 生成：发送拼接后的提示给大模型，基于上下文生成回答
   ↓
返回回答
```

### API 接口

```
# Step 1: 导入知识到向量库

## 导入文本
POST /rag/load/text
Content-Type: application/json
{ "content": "Spring AI 是一个...", "sourceName": "Spring AI 文档" }

## 导入文件（PDF/DOCX/TXT等）
GET /rag/load/file?path=/path/to/file.pdf&sourceName=产品手册

# Step 2: RAG 问答

## Naive RAG（手动检索+提示拼接）
GET /rag/ask?question=Spring AI支持哪些向量数据库
GET /rag/ask-stream?question=Spring AI是什么

## 直接向量搜索（不经过LLM）
GET /rag/search?query=向量数据库&topK=5
```

### 推荐测试流程

```bash
# 1. 先导入示例知识
curl -X POST http://localhost:8852/rag/load/text \
  -H "Content-Type: application/json" \
  -d '{"content": "Spring AI 支持 PGVector、Chroma、Pinecone 等向量数据库。ChatClient 是核心 API。Advisor 是拦截器。", "sourceName": "Spring AI 文档"}'

# 2. RAG 问答
curl "http://localhost:8852/rag/ask?question=Spring AI支持哪些向量数据库"

# 3. 查看检索结果
curl "http://localhost:8852/rag/search?query=向量数据库&topK=3"
```

### 关键代码

```java
// ETL: 加载 → 切分 → 入库
public int loadText(String content, String sourceName) {
    Document doc = new Document(content);
    doc.getMetadata().put("source", sourceName);
    List<Document> splitDocs = textSplitter.apply(List.of(doc));
    vectorStore.add(splitDocs);
    return splitDocs.size();
}

// 手动 RAG
public String ask(String question) {
    List<Document> docs = vectorStore.similaritySearch(
        SearchRequest.builder().query(question).topK(5).similarityThreshold(0.5).build()
    );
    String context = docs.stream()
        .map(doc -> "[" + doc.getMetadata().get("source") + "]\n" + doc.getText())
        .collect(Collectors.joining("\n\n"));
    String userMessage = "参考信息：\n" + context + "\n\n问题：" + question;
    return chatClient.prompt().user(userMessage).call().content();
}
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `RagConfig.java` | ChatClient 和 TokenTextSplitter 配置 |
| `DocumentEtlService.java` | ETL 核心逻辑：PDF/文件/文本加载，切分，入库 |
| `RagController.java` | REST 控制器，导入/问答/搜索 |
| `docs/sample-knowledge.txt` | 示例知识文档 |

### 前置条件

PostgreSQL + PGVector，数据库 `spring-ai-study`，embedding 维度 2048。

---

## ch08-mcp-client - MCP 客户端

**端口**: 8853 | **依赖**: spring-ai-starter-model-openai, spring-ai-starter-mcp-client

### 知识点

| 主题 | 说明 |
|------|------|
| MCP (Model Context Protocol) | 标准化的模型上下文协议，用于工具/资源共享 |
| MCP Client | 连接 MCP Server，发现并使用其提供的工具 |
| SSE 传输 | Server-Sent Events，HTTP 长连接传输 MCP 消息 |
| ToolCallbackProvider | 自动发现 MCP Server 暴露的所有工具 |
| SyncMcpToolCallbackProvider | 同步模式的 MCP 工具提供者 |
| 自动配置 | Spring Boot Starter 自动配置 MCP Client 连接 |

### MCP 架构图

```
┌─────────────────┐     SSE/HTTP      ┌─────────────────┐
│   ch08 Client   │ ←───────────────→ │   ch09 Server   │
│   (端口 8853)    │                   │   (端口 8854)    │
│                 │                   │                 │
│  ChatClient     │    发现工具        │  WeatherTools   │
│  + MCP Tools ────────→ ←──────────────  SystemTools   │
└─────────────────┘                   └─────────────────┘
```

### API 接口

```
# 使用 MCP 工具对话（模型自动选择远程工具）
GET /mcp/chat?message=帮我查一下北京现在的天气
GET /mcp/chat?message=现在几点了？

# 流式
GET /mcp/chat-stream?message=北京和上海的天气对比

# 查看可用工具
GET /mcp/tools

# 查看 MCP Server 连接状态
GET /mcp/servers
```

### 使用步骤

```bash
# 1. 先启动 ch09-mcp-server（端口 8854）
# 2. 再启动 ch08-mcp-client（端口 8853）
# 3. 测试
curl "http://localhost:8853/mcp/tools"
curl "http://localhost:8853/mcp/chat?message=北京天气怎么样"
```

### 关键代码

```java
// 自动配置 MCP Client 后，注入 ToolCallbackProvider
@Bean
public ChatClient mcpChatClient(ChatModel chatModel, ToolCallbackProvider mcpTools) {
    return ChatClient.builder(chatModel)
        .defaultToolCallbacks(mcpTools)  // 自动使用 MCP Server 的工具
        .build();
}

// YAML 配置 SSE 连接
spring.ai.mcp.client.sse.connections.my-tools-server.url=http://localhost:8854
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `McpClientConfig.java` | ChatClient 配置，注入 MCP 工具 |
| `McpClientController.java` | REST 控制器，对话/工具列表/服务器状态 |
| `application.yml` | MCP Client SSE 连接配置 |

### 前置条件

必须先启动 ch09-mcp-server (端口 8854)。

---

## ch09-mcp-server - MCP 服务端

**端口**: 8854 | **依赖**: spring-ai-starter-mcp-server-webmvc

### 知识点

| 主题 | 说明 |
|------|------|
| MCP Server | 通过 MCP 协议向外部暴露工具 |
| SSE 协议 | Server-Sent Events，供远程 Client 连接 |
| @Tool 暴露 | 使用 @Tool 注解的工具类自动注册为 MCP 工具 |
| spring-ai-starter-mcp-server-webmvc | 基于 Spring WebMVC 的 MCP Server Starter |

### API 接口

本模块作为 MCP Server 运行，不直接提供 REST API。
MCP Client 通过 SSE 连接到 `http://localhost:8854/sse` 来发现和使用工具。

暴露的工具：

| 工具名 | 说明 |
|--------|------|
| `getWeather` | 查询城市天气（温度、湿度、天气状况） |
| `getForecast` | 查询城市未来几天天气预报 |
| `getCurrentTime` | 获取当前日期和时间 |
| `getServerStatus` | 获取服务器运行状态（CPU、内存） |

### 关键代码

```java
// 只需 @Service + @Tool，Spring AI 自动注册为 MCP 工具
@Service
public class WeatherTools {
    @Tool(description = "查询指定城市的当前天气情况")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        return String.format("城市: %s\n天气: %s\n温度: %d°C", city, condition, temp);
    }
}
```

```yaml
# application.yml
spring.ai.mcp.server:
  name: my-tools-server
  version: 1.0.0
  type: SYNC
  protocol: SSE       # 使用 SSE 供远程连接
  capabilities:
    tool: true         # 启用工具能力
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `WeatherTools.java` | 天气工具集：getWeather, getForecast |
| `SystemTools.java` | 系统工具集：getCurrentTime, getServerStatus |
| `application.yml` | MCP Server 配置（SSE 协议） |

---

## ch10-multi-agent - 多智能体协作

**端口**: 8855 | **依赖**: spring-ai-starter-model-openai

### 知识点

| 主题 | 说明 |
|------|------|
| Router Agent 模式 | 路由智能体分析意图，分发到专业 Agent |
| 多 ChatClient 实例 | 每个 Agent 有独立的 ChatClient（不同 system prompt） |
| 意图分类 | LLM 充当分类器，判断问题属于哪个领域 |
| Agent 工具化 | TechnicalAgent 自带 TechTools，展示 Agent + Tool 的结合 |

### 架构图

```
                ┌─────────────────┐
                │  用户问题        │
                └────────┬────────┘
                         ↓
                ┌─────────────────┐
                │   RouterAgent   │  ← 意图分类
                │ (分类为 TECH /  │
                │  CREATIVE /     │
                │  GENERAL)       │
                └────────┬────────┘
                    ┌────┼────┐
              ┌─────┘    │    └─────┐
              ↓          ↓          ↓
   ┌──────────────┐ ┌──────────┐ ┌──────────┐
   │TechnicalAgent│ │Creative  │ │General   │
   │ + TechTools  │ │Agent     │ │Agent     │
   └──────────────┘ └──────────┘ └──────────┘
```

### API 接口

```
# Multi-Agent 对话（自动路由）
GET /agent/chat?message=Spring AI的ChatClient怎么用       → TechnicalAgent
GET /agent/chat?message=帮我写一首关于AI的诗               → CreativeAgent
GET /agent/chat?message=你好，今天心情怎么样               → GeneralAgent
GET /agent/chat?message=查询一下Spring Boot的版本信息       → TechnicalAgent + TechTools

# 仅测试路由分类
GET /agent/route?message=Java怎么连接数据库                 → {"category":"TECHNICAL","agent":"TechnicalAgent"}
```

### 关键代码

```java
// Router Agent - 意图分类
@Component
public class RouterAgent {
    private final ChatClient routerClient;

    public RouterAgent(ChatModel chatModel) {
        this.routerClient = ChatClient.builder(chatModel)
            .defaultSystem("你是一个意图分类器...可选类别：TECHNICAL / CREATIVE / GENERAL")
            .build();
    }

    public String route(String userMessage) {
        return routerClient.prompt().user(userMessage).call().content().trim();
    }
}

// Controller 路由分发
String category = routerAgent.route(message);
String content = switch (category) {
    case "TECHNICAL" -> technicalAgent.answer(message);
    case "CREATIVE"  -> creativeAgent.answer(message);
    default          -> generalAgent.answer(message);
};
```

### 关键文件

| 文件 | 作用 |
|------|------|
| `RouterAgent.java` | 路由智能体，LLM 意图分类 |
| `TechnicalAgent.java` | 技术Agent + 内置 TechTools（版本查询、概念说明） |
| `CreativeAgent.java` | 创意Agent，专精写作 |
| `GeneralAgent.java` | 通用Agent，处理一般问题 |
| `MultiAgentController.java` | REST 控制器，路由分发 |
| `AgentResponse.java` | 响应 record，包含 agentName/category/content |

---

## 环境依赖

### 基础环境

| 组件 | 版本要求 |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| PostgreSQL | 14+ (需 PGVector 扩展) |
| 智谱AI API Key | 需申请（已在配置文件中内置） |

### 环境变量配置

复制 `.env.example` 为 `.env`，填写实际值：

```bash
cp .env.example .env
```

```properties
# 智谱 GLM API Key（申请地址：https://open.bigmodel.cn）
ZHIPU_API_KEY=xxxxxx

# PostgreSQL 数据库账号密码
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### PGVector 安装（Docker）

```bash
# 1. 创建数据卷
docker volume create pg_data

# 2. 创建初始化脚本 init.sql
echo "CREATE EXTENSION IF NOT EXISTS vector;" > init.sql

# 3. 启动 PGVector 容器
docker run \
  --name pgvector-pg18 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=spring-ai-study \
  -v pg_data:/var/lib/postgresql \
  -v ./init.sql:/docker-entrypoint-initdb.d/init.sql \
  -p 5432:5432 \
  -d pgvector/pgvector:pg18-trixie
```

### 模块依赖关系

```
独立运行:        ch01, ch03, ch04, ch05(非JDBC), ch06, ch10
需要 PostgreSQL:  ch02, ch05(JDBC模式), ch07
需要先启动 ch09:  ch08 (ch09 → ch08 顺序)
```

### 端口分配

| 模块 | 端口       |
|------|----------|
| ch01-chat-demo | 8848     |
| ch02-embeddings-demo | 无 (测试模块) |
| ch03-key-polling | 8849     |
| ch04-function-call | 8850     |
| ch05-chat-memory | 8851     |
| ch06-advanced-tool-call | 8852     |
| ch07-rag-knowledge | 8853     |
| ch08-mcp-client | 8854     |
| ch09-mcp-server | 8855     |
| ch10-multi-agent | 8856     |

### 学习路径建议

```
ch01(基础对话) → ch04(函数调用) → ch06(@Tool注解) → ch05(对话记忆)
                                                          ↓
ch02(Embedding) → ch07(RAG知识库)                   ch10(多Agent)
                                                          ↓
                                              ch09(MCP Server) → ch08(MCP Client)
```
