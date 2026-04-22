package com.javalearn.advancedtool.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import com.javalearn.advancedtool.tools.OrderTools;
import com.javalearn.advancedtool.tools.ProductTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 高级工具调用配置
 */
@Configuration
public class ToolConfig {

    /**
     * 从 @Tool 注解类自动生成 ToolCallback 数组
     */
    @Bean
    public ToolCallback[] orderToolCallbacks() {
        return ToolCallbacks.from(new OrderTools());
    }

    @Bean
    public ToolCallback[] productToolCallbacks() {
        return ToolCallbacks.from(new ProductTools());
    }

    /**
     * 带工具的 ChatClient
     */
    @Bean
    public ChatClient toolChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个智能客服助手。你可以查询订单、搜索商品、查询天气和获取时间。请用中文回答。")
                .defaultTools(new OrderTools(), new ProductTools())
                .build();
    }
}
