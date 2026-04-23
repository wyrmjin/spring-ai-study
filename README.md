# Spring AI 2.0 学习教程

基于 **Spring AI 2.0.0-M4** + **Spring Boot 4.0.5** + **Java 21** 的 Spring AI 渐进式教程，通过 10 个独立模块讲解 Spring AI 核心功能。

- **AI 提供商**: 智谱 GLM (glm-4.7-flash / embedding-3) via OpenAI 兼容 API
- **向量数据库**: PGVector (PostgreSQL)

## 模块概览

| 模块 | 端口 | 功能 |
|------|------|------|
| ch01-chat-demo | 8848 | 基础对话（同步/流式/Prompt模板/结构化输出） |
| ch02-embeddings-demo | - | Embedding 与 PGVector 向量存储 |
| ch03-key-polling | 8849 | API Key 轮询 |
| ch04-function-call | 8849 | 函数调用基础（FunctionToolCallback） |
| ch05-chat-memory | 8850 | 对话记忆（内存/JDBC 持久化） |
| ch06-advanced-tool-call | 8851 | 高级工具调用（@Tool / ToolContext / returnDirect） |
| ch07-rag-knowledge | 8852 | RAG 知识库问答 |
| ch08-mcp-client | 8853 | MCP 客户端 |
| ch09-mcp-server | 8854 | MCP 服务端 |
| ch10-multi-agent | 8855 | 多智能体协作（Router 模式） |

## 快速开始

### 1. 环境准备

- Java 21+
- Maven 3.9+
- Docker（用于运行 PGVector）

### 2. 环境变量

```bash
cp .env.example .env
```

编辑 `.env` 填写实际值：

```properties
API_KEY=API密钥
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### 3. 启动 PGVector

```bash
docker volume create pg_data

echo "CREATE EXTENSION IF NOT EXISTS vector;" > init.sql

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

### 4. 构建与运行

```bash
# 构建所有模块
mvn clean install

# 运行某个模块
mvn spring-boot:run -pl ch01-chat-demo
```

## 学习路径

```
ch01(基础对话) → ch04(函数调用) → ch06(@Tool注解) → ch05(对话记忆)
                                                          ↓
ch02(Embedding) → ch07(RAG知识库)                   ch10(多Agent)
                                                          ↓
                                              ch09(MCP Server) → ch08(MCP Client)
```

## 模块依赖

- **独立运行**: ch01, ch03, ch04, ch05(非JDBC), ch06, ch10
- **需要 PostgreSQL**: ch02, ch05(JDBC模式), ch07
- **需先启动 ch09**: ch08（ch09 → ch08 顺序启动）

## 详细教程

完整的知识点讲解、API 接口、关键代码和测试方法请参考 [TUTORIAL.md](TUTORIAL.md)。
