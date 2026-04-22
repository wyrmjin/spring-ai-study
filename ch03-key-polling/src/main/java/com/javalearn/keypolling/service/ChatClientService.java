package com.javalearn.keypolling.service;

import com.javalearn.keypolling.model.KeyInfo;
import com.javalearn.keypolling.repository.KeyInfoRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
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
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(keyInfo.getBaseUrl()).apiKey(keyInfo.getKey()).completionsPath(keyInfo.getCompletionsPath()).build();
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(keyInfo.getModel()).build();
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build();

        return ChatClient.builder(openAiChatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }
}
