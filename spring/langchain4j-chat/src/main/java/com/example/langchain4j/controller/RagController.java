package com.example.langchain4j.controller;

import com.example.langchain4j.service.Assistant;
import com.example.langchain4j.service.IngestionService;
import com.example.langchain4j.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST entry points for the demo:
 *
 *   POST /ingest  -> load + embed + store the sample documents
 *   POST /search  -> semantic search only (see what Vector Search returns)
 *   POST /chat    -> full RAG: search + OpenAI answer
 *   GET  /health  -> simple liveness check
 */
@RestController
public class RagController {

    private final Assistant assistant;
    private final IngestionService ingestionService;
    private final SearchService searchService;

    public RagController(Assistant assistant,
                         IngestionService ingestionService,
                         SearchService searchService) {
        this.assistant = assistant;
        this.ingestionService = ingestionService;
        this.searchService = searchService;
    }

    public record ChatRequest(String question) {}
    public record ChatResponse(String answer) {}

    /** Semantic search only: Question -> Voyage embedding -> Vector Search -> matching docs. */
    @PostMapping("/search")
    public List<SearchService.Match> search(@RequestBody ChatRequest request) {
        return searchService.search(request.question());
    }

    /** Full RAG: Question -> Vector Search -> OpenAI -> Answer. */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return new ChatResponse(assistant.answer(request.question()));
    }

    /** Documents -> Voyage AI -> MongoDB Atlas. */
    @PostMapping("/ingest")
    public Map<String, Object> ingest() throws Exception {
        int count = ingestionService.ingest();
        return Map.of("ingested", count);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
