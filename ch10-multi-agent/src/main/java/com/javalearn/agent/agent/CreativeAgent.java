package com.javalearn.agent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * 创意 Agent - 专注创意内容
 */
@Component
public class CreativeAgent {

    private final ChatClient creativeClient;

    public CreativeAgent(ChatModel chatModel) {
        this.creativeClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是一个富有创造力的作家和内容创作者。
                        擅长：诗歌、故事、文案、翻译、创意写作。
                        回答时请：
                        1. 内容生动有趣
                        2. 语言优美流畅
                        3. 适当使用修辞手法
                        请用中文回答。
                        """)
                .build();
    }

    public String answer(String question) {
        return creativeClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
