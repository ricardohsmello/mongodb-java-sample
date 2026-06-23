package com.springlibrary.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlibrary.dto.request.BookRequest;
import com.springlibrary.dto.response.BookResponse;
import com.springlibrary.model.Book;
import com.springlibrary.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Book API Integration Tests")
class BookApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create, retrieve, update, and delete a book")
    void fullBookLifecycle() throws Exception {
        BookRequest newBook = new BookRequest("Test Book", "Test Author", "123-456-789", 2024, 29.99);

        MvcResult createResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Book")))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        BookResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), BookResponse.class);
        String id = created.id();

        assertThat(id).isNotNull();
        assertThat(bookRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/books/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.price", is(29.99)));

        BookRequest update = new BookRequest("Test Book - Updated", "Test Author", "123-456-789", 2024, 39.99);

        mockMvc.perform(put("/api/books/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Book - Updated")))
                .andExpect(jsonPath("$.price", is(39.99)));

        mockMvc.perform(delete("/api/books/" + id))
                .andExpect(status().isNoContent());

        assertThat(bookRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return a paginated list of books")
    void listsBooksPaginated() throws Exception {
        bookRepository.saveAll(List.of(
                new Book("Book A", "Author 1", "111", 2020, 10.0),
                new Book("Book B", "Author 2", "222", 2021, 20.0),
                new Book("Book C", "Author 3", "333", 2022, 30.0)));

        mockMvc.perform(get("/api/books").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)));
    }

    @Test
    @DisplayName("Should find books by author")
    void findsBooksByAuthor() throws Exception {
        bookRepository.saveAll(List.of(
                new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99),
                new Book("Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50),
                new Book("Clean Architecture", "Robert C. Martin", "978-0134494166", 2017, 29.99)));

        mockMvc.perform(get("/api/books/author/Robert C. Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Clean Code", "Clean Architecture")));
    }

    @Test
    @DisplayName("Should find a book by ISBN")
    void findsBookByIsbn() throws Exception {
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99));

        mockMvc.perform(get("/api/books/isbn/978-0132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @DisplayName("Should return 404 for an unknown ISBN")
    void findsBookByIsbn_notFound() throws Exception {
        mockMvc.perform(get("/api/books/isbn/000-000-000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when updating a missing book")
    void updateMissingBook() throws Exception {
        BookRequest update = new BookRequest("Ghost", "Nobody", "000", 2024, 1.0);

        mockMvc.perform(put("/api/books/missing-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for an invalid request body")
    void rejectsInvalidBody() throws Exception {
        BookRequest invalid = new BookRequest("", "", "", -1, 0.0);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasSize(4)));
    }
}
