package com.javalearn.mcpclient.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * MCP Client 控制器
 * <p>
 * 演示如何通过 MCP Client 连接外部 MCP Server 并使用其工具。
 * <p>
 * 使用前需要先启动 ch09-mcp-server（端口 8854），
 * 本模块会通过 SSE 连接到 MCP Server 并获取工具列表。
 * <p>
 * 测试：
 * GET /mcp/chat?message=帮我查一下北京现在的天气
 * GET /mcp/chat?message=现在几点了？
 * GET /mcp/tools - 查看可用的 MCP 工具
 */
@RestController
@RequestMapping("/mcp")
public class McpClientController {

    private final ChatClient mcpChatClient;
    private final ToolCallbackProvider mcpTools;
    private final List<McpSyncClient> mcpSyncClients;

    public McpClientController(ChatClient mcpChatClient,
                               ToolCallbackProvider mcpTools,
                               List<McpSyncClient> mcpSyncClients) {
        this.mcpChatClient = mcpChatClient;
        this.mcpTools = mcpTools;
        this.mcpSyncClients = mcpSyncClients;
    }

    /**
     * 使用 MCP 工具进行对话
     * <p>
     * GET /mcp/chat?message=帮我查一下北京现在的天气
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return mcpChatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式响应
     */
    @GetMapping("/chat-stream")
    public Flux<String> chatStream(@RequestParam String message) {
        return mcpChatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    /**
     * 查看可用的 MCP 工具列表
     */
    @GetMapping("/tools")
    public List<String> listTools() {
        ToolCallback[] callbacks = mcpTools.getToolCallbacks();
        return Arrays.stream(callbacks)
                .map(cb -> cb.getToolDefinition().name() + ": " + cb.getToolDefinition().description())
                .toList();
    }

    /**
     * 查看 MCP Server 连接状态
     */
    @GetMapping("/servers")
    public List<String> listServers() {
        return mcpSyncClients.stream()
                .map(client -> String.valueOf(client.getServerInfo()))
                .toList();
    }
}
