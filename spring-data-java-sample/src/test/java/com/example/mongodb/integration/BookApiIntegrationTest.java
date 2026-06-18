package com.example.mongodb.integration;

import com.example.mongodb.model.Book;
import com.example.mongodb.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

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
    @DisplayName("Integration: Should create, retrieve, update, and delete a book")
    void testFullBookLifecycle() throws Exception {
        Book newBook = new Book("Test Book", "Test Author", "123-456-789", 2024, 29.99);

        MvcResult createResult = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Book")))
                .andExpect(jsonPath("$.author", is("Test Author")))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Book createdBook = objectMapper.readValue(responseBody, Book.class);
        String bookId = createdBook.getId();

        assertThat(bookId).isNotNull();
        assertThat(bookRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookId)))
                .andExpect(jsonPath("$.title", is("Test Book")))
                .andExpect(jsonPath("$.author", is("Test Author")))
                .andExpect(jsonPath("$.price", is(29.99)));

        Book updatedBook = new Book(bookId, "Test Book - Updated", "Test Author", "123-456-789", 2024, 39.99);

        mockMvc.perform(put("/api/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Book - Updated")))
                .andExpect(jsonPath("$.price", is(39.99)));

        Book bookFromDb = bookRepository.findById(bookId).orElse(null);
        assertThat(bookFromDb).isNotNull();
        assertThat(bookFromDb.getTitle()).isEqualTo("Test Book - Updated");
        assertThat(bookFromDb.getPrice()).isEqualTo(39.99);

        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNoContent());

        assertThat(bookRepository.count()).isEqualTo(0);
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @Test
    @DisplayName("Integration: Should retrieve all books")
    void testGetAllBooks() throws Exception {

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Book 1", "Book 2", "Book 3")));
    }

    @Test
    @DisplayName("Integration: Should find books by author")
    void testFindBooksByAuthor() throws Exception {
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50));
        bookRepository.save(new Book("Clean Architecture", "Robert C. Martin", "978-0134494166", 2017, 29.99));

        mockMvc.perform(get("/api/books/author/Robert C. Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].author", everyItem(is("Robert C. Martin"))))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Clean Code", "Clean Architecture")));
    }

    @Test
    @DisplayName("Integration: Should find books by title")
    void testFindBooksByTitle() throws Exception {
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50));

        mockMvc.perform(get("/api/books/title/Clean Code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));
    }

    @Test
    @DisplayName("Integration: Should find book by ISBN")
    void testFindBookByIsbn() throws Exception {
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50));

        mockMvc.perform(get("/api/books/isbn/978-0132350884"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("978-0132350884")))
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @DisplayName("Integration: Should return 404 for non-existent ISBN")
    void testFindBookByIsbn_NotFound() throws Exception {
        mockMvc.perform(get("/api/books/isbn/000-000-000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration: Should return book count")
    void testGetBookCount() throws Exception {
        mockMvc.perform(get("/api/books/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        bookRepository.save(new Book("Book 1", "Author 1", "111-111-111", 2020, 10.00));
        bookRepository.save(new Book("Book 2", "Author 2", "222-222-222", 2021, 20.00));
        bookRepository.save(new Book("Book 3", "Author 3", "333-333-333", 2022, 30.00));

        mockMvc.perform(get("/api/books/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    @DisplayName("Integration: Should delete all books")
    void testDeleteAllBooks() throws Exception {
        bookRepository.save(new Book("Book 1", "Author 1", "111-111-111", 2020, 10.00));
        bookRepository.save(new Book("Book 2", "Author 2", "222-222-222", 2021, 20.00));
        bookRepository.save(new Book("Book 3", "Author 3", "333-333-333", 2022, 30.00));

        assertThat(bookRepository.count()).isEqualTo(3);

        mockMvc.perform(delete("/api/books"))
                .andExpect(status().isNoContent());

        // Verify
        assertThat(bookRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Integration: Should return 404 when updating non-existent book")
    void testUpdateNonExistentBook() throws Exception {
        Book updatedBook = new Book("999", "Non-existent", "Unknown", "000-000-000", 2024, 0.0);

        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration: Should return 404 when deleting non-existent book")
    void testDeleteNonExistentBook() throws Exception {
        mockMvc.perform(delete("/api/books/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration: Should handle multiple operations correctly")
    void testMultipleOperations() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Book book = new Book("Book " + i, "Author " + i, "ISBN-" + i, 2020 + i, 10.0 * i);
            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(book)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/books/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        assertThat(bookRepository.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("Integration: Should return empty list when no books match criteria")
    void testEmptyResults() throws Exception {
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99));

        mockMvc.perform(get("/api/books/author/Non-existent Author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/books/title/Non-existent Title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
