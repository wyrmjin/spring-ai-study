package com.javalearn.mcpclient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Client 配置
 * <p>
 * Spring AI 自动配置 MCP Client，并通过 ToolCallbackProvider
 * 自动发现 MCP Server 暴露的工具，注入到 ChatClient 中。
 */
@Configuration
public class McpClientConfig {

    @Bean
    public ChatClient mcpChatClient(ChatModel chatModel, ToolCallbackProvider mcpTools) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个智能助手，可以通过工具获取实时信息。请用中文回答。")
                .defaultToolCallbacks(mcpTools)
                .build();
    }
}
