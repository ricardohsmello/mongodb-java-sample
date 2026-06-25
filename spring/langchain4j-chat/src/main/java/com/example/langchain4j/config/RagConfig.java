package com.example.langchain4j.config;

import com.example.langchain4j.Assistant;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Every LangChain4j building block of the RAG pipeline is wired here, one bean each.
 *
 *   VoyageAiEmbeddingModel      -> turns text into a vector
 *   MongoDbEmbeddingStore       -> stores vectors and runs Atlas Vector Search
 *   OpenAiChatModel             -> writes the final answer
 *   EmbeddingStoreContentRetriever -> finds the most relevant documents for a question
 *   AiServices                  -> glues retriever + chat model into our Assistant
 */
@Configuration
public class RagConfig {

    @Bean
    public MongoClient mongoClient(@Value("${mongodb.uri}") String uri) {
        return MongoClients.create(uri);
    }

    /** Voyage AI: text -> embedding. */
    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${voyage.api-key}") String apiKey,
            @Value("${voyage.model}") String model) {

        return VoyageAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .build();
    }

    /** MongoDB Atlas: the vector database. createIndex(true) builds the Vector Search index for us. */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            @Value("${mongodb.uri}") String uri,
            @Value("${mongodb.database}") String database,
            @Value("${mongodb.collection}") String collection,
            @Value("${mongodb.index-name}") String indexName) {

        return MongoDbEmbeddingStore.builder()
                .fromClient(mongoClient(uri))
                .databaseName(database)
                .collectionName(collection)
                .indexName(indexName)
                .createIndex(true)
                .indexMapping(IndexMapping.builder()
                        .dimension(1024)                       // matches voyage-3-large
                        .metadataFieldNames(Set.of())          // no metadata filters in this demo
                        .build())
                .build();
    }

    /** OpenAI: the chat model that generates the answer. */
    @Bean
    public ChatModel chatModel(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model) {

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .build();
    }

    /**
     * The Assistant: AiServices connects the retriever (Vector Search) to the chat model.
     * On every call it retrieves matching documents and feeds them to OpenAI as context.
     */
    @Bean
    public Assistant assistant(ChatModel chatModel,
                               EmbeddingStore<TextSegment> embeddingStore,
                               EmbeddingModel embeddingModel) {

        var retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3) // top 3 most relevant documents
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(retriever)
                .build();
    }
}
