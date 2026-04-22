package com.javalearn.chatmemory.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话记忆控制器
 * <p>
 * 演示三种对话记忆方案：
 * 1. MessageChatMemoryAdvisor + InMemory - 内存存储，消息列表注入
 * 2. PromptChatMemoryAdvisor + InMemory - 内存存储，拼接到系统提示
 * 3. MessageChatMemoryAdvisor + JDBC    - 数据库持久化
 */
@RestController
@RequestMapping("/chat")
public class ChatMemoryController {

    private final ChatClient messageMemoryChatClient;
    private final ChatClient promptMemoryChatClient;
    private final ChatClient jdbcMemoryChatClient;

    public ChatMemoryController(
            @Qualifier("messageMemoryChatClient") ChatClient messageMemoryChatClient,
            @Qualifier("promptMemoryChatClient") ChatClient promptMemoryChatClient,
            @Qualifier("jdbcMemoryChatClient") ChatClient jdbcMemoryChatClient) {
        this.messageMemoryChatClient = messageMemoryChatClient;
        this.promptMemoryChatClient = promptMemoryChatClient;
        this.jdbcMemoryChatClient = jdbcMemoryChatClient;
    }

    /**
     * 方式一：MessageChatMemoryAdvisor（推荐）
     * 通过 conversationId 区分不同的会话
     * <p>
     * 测试流程：
     * GET /chat/message?conversationId=user1&message=我叫张三，我是Java开发者
     * GET /chat/message?conversationId=user1&message=我叫什么名字？我擅长什么？
     * GET /chat/message?conversationId=user2&message=我叫什么名字？（不同会话，不知道）
     */
    @GetMapping("/message")
    public String messageMemory(@RequestParam String conversationId,
                                @RequestParam String message) {
        return messageMemoryChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * 方式一：MessageChatMemoryAdvisor 流式响应
     */
    @GetMapping("/message-stream")
    public Flux<String> messageMemoryStream(@RequestParam String conversationId,
                                             @RequestParam String message) {
        return messageMemoryChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * 方式二：PromptChatMemoryAdvisor
     * 将历史对话拼接到系统提示中，适用于不支持长上下文消息列表的模型
     * <p>
     * 测试流程同上
     */
    @GetMapping("/prompt")
    public String promptMemory(@RequestParam String conversationId,
                               @RequestParam String message) {
        return promptMemoryChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * 方式三：JDBC 持久化对话记忆
     * 对话记录存入 PostgreSQL，重启应用后仍然保留
     * <p>
     * 测试流程：
     * GET /chat/jdbc?conversationId=customer-001&message=我想查询订单状态，我的订单号是ORD-20250101
     * GET /chat/jdbc?conversationId=customer-001&message=帮我再查一下我的订单号是多少
     * （重启应用后）
     * GET /chat/jdbc?conversationId=customer-001&message=你还记得我的订单号吗？
     */
    @GetMapping("/jdbc")
    public String jdbcMemory(@RequestParam String conversationId,
                             @RequestParam String message) {
        return jdbcMemoryChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * JDBC 持久化 - 流式响应
     */
    @GetMapping("/jdbc-stream")
    public Flux<String> jdbcMemoryStream(@RequestParam String conversationId,
                                          @RequestParam String message) {
        return jdbcMemoryChatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
