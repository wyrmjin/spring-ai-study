package com.javalearn.keypolling.service;

import com.javalearn.keypolling.model.KeyInfo;
import com.javalearn.keypolling.repository.KeyInfoRepository;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class ChatClientService {

    @Autowired
    private KeyInfoRepository keyInfoRepository;

    public ChatClient getChatClient(){
        List<KeyInfo> all = keyInfoRepository.findAll();
        int i = ThreadLocalRandom.current().nextInt(all.size());
        KeyInfo keyInfo = all.get(i);
        // Spring AI 2.0.x 基于 OpenAI 官方 Java SDK 构建：OpenAiApi 已移除，改为官方 OpenAIClient
        // （自动在 baseUrl 后拼接 /chat/completions，因此 KeyInfo.completionsPath 不再参与构建，保留字段仅作记录）。
        // 注意：OpenAiChatModel.Builder 要求同时提供同步与异步两个 client，
        // 否则 build() 会尝试自行构建缺失的一侧并因缺少凭据而失败。
        OpenAIClient openAiClient = OpenAIOkHttpClient.builder()
                .baseUrl(keyInfo.getBaseUrl())
                .apiKey(keyInfo.getKey())
                .build();
        OpenAIClientAsync openAiClientAsync = OpenAIOkHttpClientAsync.builder()
                .baseUrl(keyInfo.getBaseUrl())
                .apiKey(keyInfo.getKey())
                .build();
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(keyInfo.getModel()).build();
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiClient(openAiClient)
                .openAiClientAsync(openAiClientAsync)
                .options(openAiChatOptions)
                .build();

        return ChatClient.builder(openAiChatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }
}
