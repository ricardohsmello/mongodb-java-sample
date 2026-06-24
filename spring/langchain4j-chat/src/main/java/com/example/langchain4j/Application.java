package com.example.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Step 1: the simplest possible flow.
 *
 *   Question -> OpenAI -> Answer
 *
 * No MongoDB, no embeddings, no RAG yet. The goal is just to prove that we can
 * talk to OpenAI through LangChain4j. The chat model is injected via the
 * constructor (constructor injection) from {@code OpenAiConfig}.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    private final ChatModel chatModel;

    public Application(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        String question = "In one sentence, what is a tropical rainforest?";

        String answer = chatModel.chat(question);

        System.out.println("Q: " + question);
        System.out.println("A: " + answer);
    }
}
