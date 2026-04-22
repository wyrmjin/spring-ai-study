package com.javalearn.agent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 通用 Agent - 处理一般问题
 */
@Component
public class GeneralAgent {

    private final ChatClient generalClient;

    public GeneralAgent(ChatModel chatModel) {
        this.generalClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个友善的通用助手，能够回答各种一般性问题。请用中文回答。")
                .build();
    }

    public String answer(String question) {
        return generalClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
