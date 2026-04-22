package com.javalearn.functioncall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final String TOOL_NAME = "currentTime";

    private OpenAiChatOptions.Builder baseOptions() {
        return OpenAiChatOptions.builder().toolNames(TOOL_NAME);
    }


    private final ChatClient chatClient;

    public FunctionCallController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
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
        Prompt prompt = new Prompt(List.of(userMessage), baseOptions().build());
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

        return response;
    }

    @GetMapping("/fc-stream")
    public Flux<ChatResponse> functionStreamCall(@RequestParam String location) {
        UserMessage userMessage = new UserMessage(location);
        OpenAiChatOptions options = baseOptions().streamUsage(true).build();
        Prompt prompt = new Prompt(List.of(userMessage), options);
        Flux<ChatResponse> stream = chatClient.prompt(prompt).stream().chatResponse();
        return stream;
    }
}
