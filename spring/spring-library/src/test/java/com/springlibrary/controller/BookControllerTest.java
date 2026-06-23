package com.springlibrary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springlibrary.dto.request.BookRequest;
import com.springlibrary.dto.response.BookResponse;
import com.springlibrary.dto.response.PageResponse;
import com.springlibrary.exception.ResourceNotFoundException;
import com.springlibrary.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@DisplayName("Book Controller Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookResponse cleanCode;

    @BeforeEach
    void setUp() {
        cleanCode = new BookResponse("1", "Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99);
    }

    @Test
    @DisplayName("GET /api/books - returns a page of books")
    void getAllBooks() throws Exception {
        PageResponse<BookResponse> page = PageResponse.of(List.of(cleanCode), 0, 20, 1);
        when(bookService.findAll(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Clean Code")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(true)));

        verify(bookService, times(1)).findAll(0, 20);
    }

    @Test
    @DisplayName("GET /api/books/{id} - returns the book when found")
    void getBookById() throws Exception {
        when(bookService.findById("1")).thenReturn(cleanCode);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.title", is("Clean Code")));
    }

    @Test
    @DisplayName("GET /api/books/{id} - returns 404 when missing")
    void getBookById_notFound() throws Exception {
        when(bookService.findById("999")).thenThrow(ResourceNotFoundException.of("Book", "999"));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("POST /api/books - creates a book and returns 201 with Location")
    void createBook() throws Exception {
        BookRequest request = new BookRequest("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99);
        when(bookService.create(any(BookRequest.class))).thenReturn(cleanCode);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Location", org.hamcrest.Matchers.endsWith("/api/books/1")))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.title", is("Clean Code")));

        verify(bookService, times(1)).create(any(BookRequest.class));
    }

    @Test
    @DisplayName("POST /api/books - returns 400 when the body is invalid")
    void createBook_invalid() throws Exception {
        BookRequest invalid = new BookRequest("", "", "", -1, 0.0);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - returns 204 when deleted")
    void deleteBook() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).delete("1");
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - returns 404 when missing")
    void deleteBook_notFound() throws Exception {
        doThrow(ResourceNotFoundException.of("Book", "999")).when(bookService).delete(eq("999"));

        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
    }
}
