package com.javalearn.functioncall.config;

import com.javalearn.functioncall.function.CurrentTimeFunc;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FunctionCallConfig {

    @Bean
    public ToolCallback currentTimeFunc(){
        return FunctionToolCallback.builder("currentTime", new CurrentTimeFunc())
                .description("Get the current time in location")
                .inputType(CurrentTimeFunc.Request.class)
                .build();
    }
}
