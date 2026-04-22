package com.javalearn.embeddings;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
public class ApplicationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    public void testEmbedding() {
        float[] embedding = embeddingModel.embed("I'm learning Spring AI");
        assertNotNull(embedding);
        System.out.println("Embedding dimension: " + embedding.length);
        System.out.println("First 5 values: " + java.util.Arrays.toString(java.util.Arrays.copyOf(embedding, 5)));
    }

    @Test
    public void testEmbeddingResponse() {
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of("I'm learning Spring AI"));
        assertNotNull(response);
        System.out.println("Model: " + response.getMetadata().getModel());
        System.out.println("Embedding count: " + response.getResults().size());
    }
}
