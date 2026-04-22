package com.javalearn.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 知识库配置
 * <p>
 * 在 Spring AI 2.0.0-M4 中，QuestionAnswerAdvisor 和 RetrievalAugmentationAdvisor
 * 尚未包含在核心包中。本模块使用手动 RAG 模式（检索 + 拼接提示）演示 RAG 原理。
 */
@Configuration
public class RagConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();
    }

    @Bean
    public ChatClient ragChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个知识库问答助手。请基于提供的上下文信息回答用户问题。" +
                        "如果上下文中没有相关信息，请诚实地说不知道。请用中文回答。")
                .build();
    }
}
