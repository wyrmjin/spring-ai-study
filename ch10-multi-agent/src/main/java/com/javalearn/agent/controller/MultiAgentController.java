package com.javalearn.agent.controller;

import com.javalearn.agent.agent.*;
import com.javalearn.agent.model.AgentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Multi-Agent 控制器
 * <p>
 * 演示 Router Agent 模式：
 * 1. RouterAgent 分析用户意图
 * 2. 将请求路由到专业 Agent（Technical / Creative / General）
 * 3. 专业 Agent 处理并返回结果
 * <p>
 * 测试：
 * GET /agent/chat?message=Spring AI的ChatClient怎么用
 * GET /agent/chat?message=帮我写一首关于AI的诗
 * GET /agent/chat?message=你好，今天心情怎么样
 * GET /agent/chat?message=查询一下Spring Boot的版本信息
 */
@RestController
@RequestMapping("/agent")
public class MultiAgentController {

    private final RouterAgent routerAgent;
    private final Map<String, Object> agents;

    public MultiAgentController(RouterAgent routerAgent,
                                TechnicalAgent technicalAgent,
                                CreativeAgent creativeAgent,
                                GeneralAgent generalAgent) {
        this.routerAgent = routerAgent;
        this.agents = Map.of(
                "TECHNICAL", technicalAgent,
                "CREATIVE", creativeAgent,
                "GENERAL", generalAgent
        );
    }

    /**
     * Multi-Agent 路由对话
     */
    @GetMapping("/chat")
    public AgentResponse chat(@RequestParam String message) {
        // Step 1: Router 分析意图
        String category = routerAgent.route(message);

        // Step 2: 路由到专业 Agent
        String content = switch (category) {
            case "TECHNICAL" -> ((TechnicalAgent) agents.get("TECHNICAL")).answer(message);
            case "CREATIVE" -> ((CreativeAgent) agents.get("CREATIVE")).answer(message);
            default -> ((GeneralAgent) agents.get("GENERAL")).answer(message);
        };

        String agentName = switch (category) {
            case "TECHNICAL" -> "TechnicalAgent";
            case "CREATIVE" -> "CreativeAgent";
            default -> "GeneralAgent";
        };

        return new AgentResponse(agentName, category, content);
    }

    /**
     * 仅测试路由分类（不执行实际回答）
     */
    @GetMapping("/route")
    public Map<String, String> route(@RequestParam String message) {
        String category = routerAgent.route(message);
        String agentName = switch (category) {
            case "TECHNICAL" -> "TechnicalAgent";
            case "CREATIVE" -> "CreativeAgent";
            default -> "GeneralAgent";
        };
        return Map.of(
                "message", message,
                "category", category,
                "agent", agentName
        );
    }
}
