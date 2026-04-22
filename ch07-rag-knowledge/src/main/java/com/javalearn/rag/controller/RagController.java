package com.javalearn.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import com.javalearn.rag.etl.DocumentEtlService;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 知识库问答控制器
 * <p>
 * 演示：
 * 1. ETL - 文档导入向量库（Extract → Transform → Load）
 * 2. 手动 RAG - 向量检索 + 提示拼接
 * 3. 直接向量搜索
 * <p>
 * Spring AI 2.0.0-M4 中 QuestionAnswerAdvisor 尚未包含在核心包中，
 * 这里使用手动 RAG 模式展示 RAG 原理。
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    private final ChatClient ragChatClient;
    private final DocumentEtlService etlService;
    private final VectorStore vectorStore;

    public RagController(ChatClient ragChatClient,
                         DocumentEtlService etlService,
                         VectorStore vectorStore) {
        this.ragChatClient = ragChatClient;
        this.etlService = etlService;
        this.vectorStore = vectorStore;
    }

    /**
     * 加载文本内容到向量库
     * <p>
     * POST /rag/load/text
     * Body: { "content": "...", "sourceName": "产品手册" }
     */
    @PostMapping("/load/text")
    public Map<String, Object> loadText(@RequestBody Map<String, String> request) {
        int count = etlService.loadText(request.get("content"), request.get("sourceName"));
        return Map.of("status", "ok", "chunks", count, "source", request.get("sourceName"));
    }

    /**
     * 加载本地文件到向量库（PDF、DOCX、TXT等）
     * <p>
     * GET /rag/load/file?path=/path/to/file.pdf&sourceName=产品手册
     */
    @GetMapping("/load/file")
    public Map<String, Object> loadFile(@RequestParam String path,
                                         @RequestParam String sourceName) {
        FileSystemResource resource = new FileSystemResource(new File(path));
        int count;
        if (path.toLowerCase().endsWith(".pdf")) {
            count = etlService.loadPdf(resource, sourceName);
        } else {
            count = etlService.loadDocument(resource, sourceName);
        }
        return Map.of("status", "ok", "chunks", count, "source", sourceName);
    }

    /**
     * RAG 问答 - 手动检索 + 提示拼接
     * <p>
     * 步骤：
     * 1. 用用户问题检索向量库中的相关文档
     * 2. 将检索结果作为上下文拼接到提示中
     * 3. 发送给 LLM 生成回答
     * <p>
     * GET /rag/ask?question=Spring AI支持哪些向量数据库
     */
    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        // Step 1: 检索相关文档
        List<Document> docs = etlService.search(question, 5);

        // Step 2: 拼接上下文
        String context = docs.stream()
                .map(doc -> "[" + doc.getMetadata().get("source") + "]\n" + doc.getText())
                .collect(Collectors.joining("\n\n"));

        // Step 3: 发送给 LLM
        String userMessage = "参考信息：\n" + context + "\n\n问题：" + question;
        return ragChatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * RAG 问答 - 流式响应
     * <p>
     * GET /rag/ask-stream?question=Spring AI是什么
     */
    @GetMapping("/ask-stream")
    public Flux<String> askStream(@RequestParam String question) {
        List<Document> docs = etlService.search(question, 5);
        String context = docs.stream()
                .map(doc -> "[" + doc.getMetadata().get("source") + "]\n" + doc.getText())
                .collect(Collectors.joining("\n\n"));

        String userMessage = "参考信息：\n" + context + "\n\n问题：" + question;
        return ragChatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * 直接搜索向量库（不经过LLM）
     * <p>
     * GET /rag/search?query=向量数据库&topK=5
     */
    @GetMapping("/search")
    public List<String> search(@RequestParam String query,
                                @RequestParam(defaultValue = "5") int topK) {
        List<Document> docs = etlService.search(query, topK);
        return docs.stream()
                .map(doc -> "[" + doc.getMetadata().get("source") + "] " + doc.getText())
                .toList();
    }
}
