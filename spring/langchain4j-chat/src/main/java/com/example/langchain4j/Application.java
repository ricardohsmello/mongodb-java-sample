package com.example.langchain4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the web application. The RAG pipeline is now driven through REST endpoints
 * (see RagController): POST /ingest to load documents, POST /chat to ask questions.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
