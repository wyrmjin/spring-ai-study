package com.javalearn.keypolling.controller;

import com.javalearn.keypolling.service.ChatClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/keypolling")
public class ChatController {

    @Autowired
    private ChatClientService chatClientService;

    @GetMapping(value = "/chat")
    public String chat(Long keyId){
        String content = chatClientService.getChatClient().prompt("你使用的是什么模型").call().content();
        return content;
    }

}
