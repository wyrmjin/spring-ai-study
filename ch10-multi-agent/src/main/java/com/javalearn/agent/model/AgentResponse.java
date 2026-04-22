package com.javalearn.agent.model;

/**
 * Agent 响应结果
 */
public record AgentResponse(
        String agentName,
        String category,
        String content
) {
}
