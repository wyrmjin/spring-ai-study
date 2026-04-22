package com.javalearn.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 系统工具 - 通过 MCP 暴露
 */
@Service
public class SystemTools {

    @Tool(description = "获取当前日期和时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "获取服务器运行状态信息")
    public String getServerStatus() {
        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory() / 1024 / 1024;
        long freeMem = runtime.freeMemory() / 1024 / 1024;
        long usedMem = totalMem - freeMem;
        int processors = runtime.availableProcessors();

        return String.format("服务器状态:\nCPU核心: %d\n总内存: %dMB\n已用: %dMB\n可用: %dMB",
                processors, totalMem, usedMem, freeMem);
    }
}
