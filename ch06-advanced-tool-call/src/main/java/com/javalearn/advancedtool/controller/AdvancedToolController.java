package com.javalearn.advancedtool.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import com.javalearn.advancedtool.tools.OrderTools;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 高级工具调用控制器
 * <p>
 * 演示：
 * 1. @Tool 声明式工具 + @ToolParam 参数描述
 * 2. ToolContext 运行时上下文传递
 * 3. returnDirect 直接返回工具结果
 * 4. 多工具协调（模型自动选择）
 */
@RestController
@RequestMapping("/tool")
public class AdvancedToolController {

    private final ChatClient toolChatClient;
    private final ChatModel chatModel;

    public AdvancedToolController(ChatClient toolChatClient, ChatModel chatModel) {
        this.toolChatClient = toolChatClient;
        this.chatModel = chatModel;
    }

    /**
     * 使用默认工具的 ChatClient
     * <p>
     * 测试：
     * GET /tool/chat?message=帮我查一下订单ORD-001的状态
     * GET /tool/chat?message=搜索一下MacBook
     * GET /tool/chat?message=北京天气怎么样？顺便看看现在几点了
     * GET /tool/chat?message=帮我取消订单ORD-002，原因是买重复了
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return toolChatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 运行时动态添加工具 + ToolContext
     * <p>
     * 测试：
     * GET /tool/dynamic?message=查询订单ORD-001
     */
    @GetMapping("/dynamic")
    public String dynamicTools(@RequestParam String message) {
        ToolCallback[] callbacks = ToolCallbacks.from(new OrderTools());

        return ChatClient.create(chatModel)
                .prompt()
                .user(message)
                .tools(callbacks)
                .toolContext(Map.of("tenantId", "store-001"))
                .call()
                .content();
    }

    /**
     * 流式响应 + 工具调用
     * <p>
     * 测试：
     * GET /tool/stream?message=帮我查订单ORD-003，然后再看看有什么iPad商品
     */
    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam String message) {
        return toolChatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
