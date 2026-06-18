package com.example.mongodb.controller;

import com.example.mongodb.model.Book;
import com.example.mongodb.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(BookController.class)
@DisplayName("Book Controller Unit Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private List<Book> bookList;

    @BeforeEach
    void setUp() {
        book1 = new Book("1", "Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99);
        book2 = new Book("2", "Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50);
        bookList = Arrays.asList(book1, book2);
    }

    @Test
    @DisplayName("GET /api/books - Should return all books")
    void testGetAllBooks() throws Exception {
        when(bookRepository.findAll()).thenReturn(bookList);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[1].title", is("Effective Java")));

        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return book when found")
    void testGetBookById_Found() throws Exception {
        when(bookRepository.findById("1")).thenReturn(Optional.of(book1));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")));

        verify(bookRepository, times(1)).findById("1");
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return 404 when not found")
    void testGetBookById_NotFound() throws Exception {
        when(bookRepository.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());

        verify(bookRepository, times(1)).findById("999");
    }

    @Test
    @DisplayName("GET /api/books/author/{author} - Should return books by author")
    void testGetBooksByAuthor() throws Exception {
        List<Book> martinBooks = Arrays.asList(book1);
        when(bookRepository.findByAuthor("Robert C. Martin")).thenReturn(martinBooks);

        mockMvc.perform(get("/api/books/author/Robert C. Martin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is("Robert C. Martin")));

        verify(bookRepository, times(1)).findByAuthor("Robert C. Martin");
    }

    @Test
    @DisplayName("GET /api/books/title/{title} - Should return books by title")
    void testGetBooksByTitle() throws Exception {
        List<Book> cleanCodeBooks = Arrays.asList(book1);
        when(bookRepository.findByTitle("Clean Code")).thenReturn(cleanCodeBooks);

        mockMvc.perform(get("/api/books/title/Clean Code"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")));

        verify(bookRepository, times(1)).findByTitle("Clean Code");
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - Should return book when found")
    void testGetBookByIsbn_Found() throws Exception {
        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(book1);

        mockMvc.perform(get("/api/books/isbn/978-0132350884"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isbn", is("978-0132350884")))
                .andExpect(jsonPath("$.title", is("Clean Code")));

        verify(bookRepository, times(1)).findByIsbn("978-0132350884");
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - Should return 404 when not found")
    void testGetBookByIsbn_NotFound() throws Exception {
        when(bookRepository.findByIsbn("999-999-999")).thenReturn(null);

        mockMvc.perform(get("/api/books/isbn/999-999-999"))
                .andExpect(status().isNotFound());

        verify(bookRepository, times(1)).findByIsbn("999-999-999");
    }

    @Test
    @DisplayName("POST /api/books - Should create new book")
    void testCreateBook() throws Exception {
        Book newBook = new Book("Test Book", "Test Author", "123-456-789", 2024, 29.99);
        Book savedBook = new Book("3", "Test Book", "Test Author", "123-456-789", 2024, 29.99);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("3")))
                .andExpect(jsonPath("$.title", is("Test Book")))
                .andExpect(jsonPath("$.author", is("Test Author")));

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Should update existing book")
    void testUpdateBook_Found() throws Exception {
        Book updatedBook = new Book("1", "Clean Code - Updated", "Robert C. Martin", "978-0132350884", 2008, 35.99);

        when(bookRepository.findById("1")).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Clean Code - Updated")))
                .andExpect(jsonPath("$.price", is(35.99)));

        verify(bookRepository, times(1)).findById("1");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Should return 404 when book not found")
    void testUpdateBook_NotFound() throws Exception {
        Book updatedBook = new Book("999", "Non-existent Book", "Unknown", "000-000-000", 2024, 0.0);

        when(bookRepository.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNotFound());

        verify(bookRepository, times(1)).findById("999");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should delete book when found")
    void testDeleteBook_Found() throws Exception {
        when(bookRepository.existsById("1")).thenReturn(true);
        doNothing().when(bookRepository).deleteById("1");

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookRepository, times(1)).existsById("1");
        verify(bookRepository, times(1)).deleteById("1");
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should return 404 when not found")
    void testDeleteBook_NotFound() throws Exception {
        when(bookRepository.existsById("999")).thenReturn(false);

        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());

        verify(bookRepository, times(1)).existsById("999");
        verify(bookRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("DELETE /api/books - Should delete all books")
    void testDeleteAllBooks() throws Exception {
        doNothing().when(bookRepository).deleteAll();

        mockMvc.perform(delete("/api/books"))
                .andExpect(status().isNoContent());

        verify(bookRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("GET /api/books/count - Should return book count")
    void testGetBookCount() throws Exception {
        when(bookRepository.count()).thenReturn(5L);

        mockMvc.perform(get("/api/books/count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("5"));

        verify(bookRepository, times(1)).count();
    }
}
