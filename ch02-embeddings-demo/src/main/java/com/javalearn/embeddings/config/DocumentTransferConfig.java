package com.javalearn.embeddings.config;

import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DocumentTransferConfig {
    @Bean
    public DocumentTransformer documentTransformer(){
        return TokenTextSplitter.builder().build();
    }
}
