package com.javalearn.chatmemory.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话记忆配置
 * <p>
 * 演示两种 Advisor 用法：
 * 1. MessageChatMemoryAdvisor + 内存存储 - 将历史消息作为消息列表注入（推荐）
 * 2. MessageChatMemoryAdvisor + JDBC 存储 - 数据库持久化
 * <p>
 * 注：旧版的 PromptChatMemoryAdvisor（将历史拼接到系统提示文本）已在
 * Spring AI 2.0.x 中移除，统一使用 MessageChatMemoryAdvisor 注入消息列表。
 */
@Configuration
public class ChatMemoryConfig {


    private static SimpleLoggerAdvisor customLogger;

    static {
        customLogger = new SimpleLoggerAdvisor(
                request -> "请求: 用户消息" + request.prompt().getUserMessage()
                        +". 系统消息:" + request.prompt().getSystemMessages()
                +".content:"+request.prompt().getContents(),
                response -> "响应: " + response.getResult(),
                0
        );
    }

    /**
     * 方式一：基于内存的 ChatMemory（适用于演示和测试）
     */
    @Bean
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    /**
     * 方式二：基于 JDBC 的 ChatMemory（持久化到 PostgreSQL）
     * 需要注入 Spring AI 自动配置的 JdbcChatMemoryRepository
     */
    @Bean
    public ChatMemory jdbcChatMemory(ChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    /**
     * 使用 MessageChatMemoryAdvisor 的 ChatClient
     * 将对话历史作为消息列表注入，保留完整对话结构
     */
    @Bean("messageMemoryChatClient")
    public ChatClient messageMemoryChatClient(ChatModel chatModel,
                                              ChatMemory inMemoryChatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个友好的AI助手，请用中文回答用户的问题。记住用户在对话中提供的信息。")
                .defaultAdvisors(
                        customLogger,
                        MessageChatMemoryAdvisor.builder(inMemoryChatMemory).build()

                )
                .build();
    }

    /**
     * 使用 JDBC 持久化的 ChatClient（对话记忆存入数据库）
     */
    @Bean("jdbcMemoryChatClient")
    public ChatClient jdbcMemoryChatClient(ChatModel chatModel,
                                           ChatMemory jdbcChatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个专业的客服助手，请用中文回答用户的问题。记住用户在对话中提供的信息。")
                .defaultAdvisors(
                        customLogger,
                        MessageChatMemoryAdvisor.builder(jdbcChatMemory).build()
                )
                .build();
    }
}
