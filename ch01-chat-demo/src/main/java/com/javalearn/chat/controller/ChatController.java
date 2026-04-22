package com.javalearn.chat.controller;

import com.javalearn.chat.vo.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;


@RestController
public class ChatController {

    private final ChatClient openAiChatClient;
    private final ChatClient anthropicChatClient;

    public ChatController(
            @Qualifier("openAiChatModel") ChatModel openAiChatModel,
            @Qualifier("anthropicChatModel") ChatModel anthropicChatModel
    ) {
//        SimpleLoggerAdvisor customLogger = new SimpleLoggerAdvisor(
//                request -> "请求: " + request.prompt().getUserMessage(),
//                response -> "响应: " + response.getResult(),
//                0
//        );
        this.openAiChatClient = ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor()).build();
        this.anthropicChatClient = ChatClient.builder(anthropicChatModel).build();
    }

    @GetMapping("/chat/openai")
    public String chatOpenAi(String prompt) {
        return openAiChatClient.prompt().user(prompt).call().content();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(String prompt) {
        return openAiChatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    @Value("classpath:prompt.st")
    private Resource prompt;

    @GetMapping("/bean")
    public Book bean(String author){
        Map<String, Object> map = Map.of("author", author);
        PromptTemplate promptTemplate = PromptTemplate.builder().resource(prompt).variables(map).build();
        return openAiChatClient.prompt().user(promptTemplate.render()).call().entity(Book.class);
    }

}
