package com.javalearn.rag.etl;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ETL 服务 - 文档读取、切分、写入向量库
 * <p>
 * ETL = Extract(提取) → Transform(转换/切分) → Load(加载到向量库)
 */
@Service
public class DocumentEtlService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentEtlService(VectorStore vectorStore, TokenTextSplitter textSplitter) {
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
    }

    /**
     * 加载 PDF 文档到向量库
     */
    public int loadPdf(Resource pdfResource, String sourceName) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource);
        List<Document> documents = reader.get();
        documents.forEach(doc -> doc.getMetadata().put("source", sourceName));

        List<Document> splitDocs = textSplitter.apply(documents);
        vectorStore.add(splitDocs);
        return splitDocs.size();
    }

    /**
     * 加载任意格式文档（通过 Tika）到向量库
     */
    public int loadDocument(Resource resource, String sourceName) {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        documents.forEach(doc -> doc.getMetadata().put("source", sourceName));

        List<Document> splitDocs = textSplitter.apply(documents);
        vectorStore.add(splitDocs);
        return splitDocs.size();
    }

    /**
     * 直接加载文本内容到向量库
     */
    public int loadText(String content, String sourceName) {
        Document doc = new Document(content);
        doc.getMetadata().put("source", sourceName);

        List<Document> splitDocs = textSplitter.apply(List.of(doc));
        vectorStore.add(splitDocs);
        return splitDocs.size();
    }

    /**
     * 搜索相似文档
     */
    public List<Document> search(String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.5)
                        .build()
        );
    }
}
