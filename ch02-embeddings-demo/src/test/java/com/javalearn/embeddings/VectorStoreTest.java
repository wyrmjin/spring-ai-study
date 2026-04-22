package com.javalearn.embeddings;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VectorStoreTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DocumentTransformer tokenTextSplitter;

    @Test
    @Order(1)
    public void testAddDocuments() {
        List<Document> documents = List.of(
                new Document("Spring AI 是一个用于构建AI应用的Spring框架", Map.of("topic", "spring-ai")),
                new Document("向量数据库用于存储和检索高维向量数据", Map.of("topic", "vector-db")),
                new Document("Embedding模型将文本转换为固定维度的向量表示", Map.of("topic", "embedding")),
                new Document("RAG（检索增强生成）结合了检索和生成两个步骤", Map.of("topic", "rag"))
        );

        vectorStore.add(documents);
        System.out.println("成功添加 " + documents.size() + " 个文档到向量存储");
    }

    @Value("classpath:test.pdf")
    private Resource resource;

    @Test
    @Order(1)
    public void testAddFile(){
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        List<Document> documents = tikaDocumentReader.get();
        List<Document> transform = tokenTextSplitter.transform(documents);
        vectorStore.add(transform);
    }

    @Test
    @Order(2)
    public void testSimilaritySearch() {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("什么是Spring AI").topK(3).build()
        );

        assertNotNull(results);
        assertFalse(results.isEmpty());
        System.out.println("查询 '什么是Spring AI' 返回 " + results.size() + " 个结果:");
        results.forEach(doc -> System.out.println("  - [score=" + doc.getMetadata().get("distance") + "] " + doc.getText()));
    }

    @Test
    @Order(2)
    public void testSimilaritySearchWithThreshold() {
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("向量存储").topK(5).similarityThreshold(0.5).build()
        );

        assertNotNull(results);
        System.out.println("查询 '向量存储' (threshold=0.5) 返回 " + results.size() + " 个结果:");
        results.forEach(doc -> System.out.println("  - " + doc.getText()));
    }

    @Test
    @Order(3)
    public void testDeleteDocuments() {
        // 先添加一个临时文档
        Document tempDoc = new Document("这是一个临时文档，用于测试删除功能", Map.of("temp", "true"));
        vectorStore.add(List.of(tempDoc));

        String docId = tempDoc.getId();
        System.out.println("添加临时文档，id=" + docId);

        // 删除文档
        vectorStore.delete(List.of(docId));
        System.out.println("已删除文档 id=" + docId);

        // 验证删除：搜索应该找不到该文档
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("临时文档").topK(10).build()
        );
        boolean stillExists = results.stream().anyMatch(doc -> docId.equals(doc.getId()));
        assertFalse(stillExists, "文档应该已被删除");
        System.out.println("验证通过：文档已被成功删除");
    }
}
