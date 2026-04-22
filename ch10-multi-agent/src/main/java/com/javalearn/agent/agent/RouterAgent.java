package com.javalearn.agent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * Router Agent - 路由智能体
 * <p>
 * 负责分析用户意图，将请求路由到最合适的专业 Agent。
 * 这是 Multi-Agent 编排的核心组件。
 */
@Component
public class RouterAgent {

    private final ChatClient routerClient;

    public RouterAgent(ChatModel chatModel) {
        this.routerClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是一个意图分类器。分析用户的问题，判断应该由哪个专业Agent处理。
                        只返回一个类别名称，不要有任何其他内容。

                        可选类别：
                        - TECHNICAL: 技术问题（编程、框架、工具、架构）
                        - CREATIVE: 创意内容（写作、故事、文案、翻译）
                        - GENERAL: 一般问题（闲聊、常识、其他）

                        示例：
                        "Java怎么连接数据库" → TECHNICAL
                        "帮我写一首春天的诗" → CREATIVE
                        "今天星期几" → GENERAL
                        """)
                .build();
    }

    /**
     * 分析用户意图，返回类别
     */
    public String route(String userMessage) {
        String category = routerClient.prompt()
                .user(userMessage)
                .call()
                .content()
                .trim();
        // 清理可能的格式化
        return category.replaceAll("[^A-Z]", "");
    }
}
