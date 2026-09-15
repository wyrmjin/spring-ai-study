package com.javalearn.functioncall.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @Project: com.ningning0111.controller
 * @Author: pgthinker
 * @GitHub: https://github.com/ningning0111
 * @Date: 2024/7/19 12:53
 * @Description:
 */
@RestController
public class FunctionCallController {

    private final ChatClient chatClient;

    // Spring AI 2.0.x 移除了 OpenAiChatOptions.toolNames()，
    // 工具改为按请求注册：prompt 级 .tools(...)（或 ChatClient 构建时 .defaultTools(...)）
    // 注：.tools(Object...) 是统一入口，可接收 ToolCallback、ToolCallbackProvider、
    // @Tool 注解对象、Function 等；原先的 .toolCallbacks(...) 已标记为过时
    private final ToolCallback currentTimeFunc;

    public FunctionCallController(ChatModel chatModel, ToolCallback currentTimeFunc) {
        this.chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        this.currentTimeFunc = currentTimeFunc;
    }
    @GetMapping("/fc")
    public Object functionCall(@RequestParam String location) {
        UserMessage userMessage = new UserMessage(location);

        /**
         * 函数调用在以下模型上受到支持：
         * gpt-4o，gpt-4o-2024-05-13，gpt-4-turbo，gpt-4-turbo-2024-04-09，
         * gpt-4-turbo-preview，gpt-4-0125-preview，gpt-4-1106-preview，
         * gpt-3.5-turbo-0125 和 gpt-3.5-turbo-1106
         * 需要确保是纯净的模型，一些黑心的API中转商就可能会用国产模型代替GPT 造成函数无法调用
         */
        Prompt prompt = new Prompt(List.of(userMessage));

        return chatClient.prompt(prompt)
                .tools(currentTimeFunc)
                .call()
                .chatResponse();
    }

    @GetMapping("/fc-stream")
    public Flux<ChatResponse> functionStreamCall(@RequestParam String location) {
        UserMessage userMessage = new UserMessage(location);
        OpenAiChatOptions options = OpenAiChatOptions.builder().streamUsage(true).build();
        Prompt prompt = new Prompt(List.of(userMessage), options);
        return chatClient.prompt(prompt)
                .tools(currentTimeFunc)
                .stream()
                .chatResponse();
    }
}
