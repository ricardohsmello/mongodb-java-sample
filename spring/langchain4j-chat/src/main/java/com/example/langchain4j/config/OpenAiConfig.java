package com.example.langchain4j.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the OpenAI chat model as a Spring bean.
 *
 * LangChain4j component introduced: OpenAiChatModel (an implementation of ChatModel).
 * This is the part of the RAG pipeline that generates the final answer.
 * Values come from application.yml (configuration properties), never hardcoded.
 */
@Configuration
public class OpenAiConfig {

    @Bean
    public ChatModel chatModel(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model) {

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .build();
    }
}
