package com.javalearn.agent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 技术 Agent - 专注技术问题
 * <p>
 * 配备代码搜索和文档查询工具，能更好地回答技术问题
 */
@Component
public class TechnicalAgent {

    private final ChatClient techClient;

    public TechnicalAgent(ChatModel chatModel) {
        this.techClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是一个资深的技术顾问，专精于Java、Spring、AI开发。
                        回答技术问题时请：
                        1. 给出清晰的解释和代码示例
                        2. 提及最佳实践
                        3. 如果涉及工具调用，主动使用可用工具
                        请用中文回答。
                        """)
                .defaultTools(new TechTools())
                .build();
    }

    public String answer(String question) {
        return techClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 技术工具集
     */
    static class TechTools {

        private static final Map<String, String> FRAMEWORK_VERSIONS = Map.of(
                "Spring Boot", "4.0.5",
                "Spring AI", "2.0.0-M4",
                "Java", "21",
                "Maven", "3.9.x"
        );

        @Tool(description = "查询项目使用的技术栈版本信息")
        public String getVersionInfo(@ToolParam(description = "技术名称，如 Spring Boot、Java") String techName) {
            String version = FRAMEWORK_VERSIONS.get(techName);
            if (version == null) {
                return "未找到 " + techName + " 的版本信息。已知技术栈：" + FRAMEWORK_VERSIONS.keySet();
            }
            return techName + " 版本: " + version;
        }

        @Tool(description = "获取Spring AI核心概念说明")
        public String getConceptInfo(@ToolParam(description = "概念名称，如 ChatClient、Advisor、RAG、MCP") String concept) {
            Map<String, String> concepts = Map.of(
                    "ChatClient", "ChatClient是Spring AI的核心API，提供流式构建器模式与AI模型交互。支持同步/流式调用、系统提示、工具调用等。",
                    "Advisor", "Advisor是拦截器模式，可以在请求前后处理。内置的有SimpleLoggerAdvisor、MessageChatMemoryAdvisor等。",
                    "RAG", "RAG(检索增强生成)通过向量搜索相关文档，将检索结果作为上下文增强AI回答的准确性。",
                    "MCP", "MCP(Model Context Protocol)是标准化的模型上下文协议，支持Client/Server架构，用于工具和资源的共享。",
                    "Tool Calling", "Tool Calling让AI模型能够调用外部工具函数。支持@Tool注解和FunctionToolCallback两种定义方式。"
            );
            return concepts.getOrDefault(concept, "未找到概念: " + concept + "。可用概念：" + concepts.keySet());
        }
    }
}
