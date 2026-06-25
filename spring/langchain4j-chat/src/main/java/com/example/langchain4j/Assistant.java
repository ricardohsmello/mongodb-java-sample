package com.example.langchain4j;

/**
 * The RAG assistant. LangChain4j generates the implementation at runtime via AiServices.
 * Each call: question -> retrieve documents from MongoDB -> ask OpenAI with that context.
 */
public interface Assistant {

    String answer(String question);
}
