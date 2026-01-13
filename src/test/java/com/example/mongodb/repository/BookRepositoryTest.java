package com.example.mongodb.repository;

import com.example.mongodb.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Book Repository Unit Tests")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        book1 = new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99);
        book2 = new Book("Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50);
        book3 = new Book("Clean Architecture", "Robert C. Martin", "978-0134494166", 2017, 29.99);
    }

    @Test
    @DisplayName("Should save and retrieve a book")
    void testSaveAndFindBook() {
        Book savedBook = bookRepository.save(book1);

        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Clean Code");
        assertThat(savedBook.getAuthor()).isEqualTo("Robert C. Martin");

        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should find all books")
    void testFindAllBooks() {
        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        List<Book> books = bookRepository.findAll();

        assertThat(books).isNotNull();
        assertThat(books).hasSize(3);
        assertThat(books).extracting(Book::getTitle)
                .contains("Clean Code", "Effective Java", "Clean Architecture");
    }

    @Test
    @DisplayName("Should find books by author")
    void testFindByAuthor() {
        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        List<Book> martinBooks = bookRepository.findByAuthor("Robert C. Martin");

        assertThat(martinBooks).isNotNull();
        assertThat(martinBooks).hasSize(2);
        assertThat(martinBooks).extracting(Book::getTitle)
                .contains("Clean Code", "Clean Architecture");
    }

    @Test
    @DisplayName("Should find books by title")
    void testFindByTitle() {
        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> books = bookRepository.findByTitle("Clean Code");

        assertThat(books).isNotNull();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Clean Code");
        assertThat(books.get(0).getAuthor()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("Should find book by ISBN")
    void testFindByIsbn() {
        bookRepository.save(book1);
        bookRepository.save(book2);

        Book foundBook = bookRepository.findByIsbn("978-0132350884");

        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo("Clean Code");
        assertThat(foundBook.getIsbn()).isEqualTo("978-0132350884");
    }

    @Test
    @DisplayName("Should return null when ISBN not found")
    void testFindByIsbn_NotFound() {
        bookRepository.save(book1);

        Book foundBook = bookRepository.findByIsbn("000-000-000");

        assertThat(foundBook).isNull();
    }

    @Test
    @DisplayName("Should update a book")
    void testUpdateBook() {
        Book savedBook = bookRepository.save(book1);
        String bookId = savedBook.getId();

        savedBook.setPrice(39.99);
        savedBook.setTitle("Clean Code - Second Edition");
        bookRepository.save(savedBook);

        Optional<Book> updatedBook = bookRepository.findById(bookId);

        assertThat(updatedBook).isPresent();
        assertThat(updatedBook.get().getPrice()).isEqualTo(39.99);
        assertThat(updatedBook.get().getTitle()).isEqualTo("Clean Code - Second Edition");
    }

    @Test
    @DisplayName("Should delete a book by ID")
    void testDeleteBookById() {
        Book savedBook = bookRepository.save(book1);
        String bookId = savedBook.getId();

        bookRepository.deleteById(bookId);

        Optional<Book> deletedBook = bookRepository.findById(bookId);
        assertThat(deletedBook).isEmpty();
    }

    @Test
    @DisplayName("Should delete all books")
    void testDeleteAllBooks() {
        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);

        assertThat(bookRepository.count()).isEqualTo(3);

        bookRepository.deleteAll();

        assertThat(bookRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should count books correctly")
    void testCountBooks() {
        assertThat(bookRepository.count()).isEqualTo(0);

        bookRepository.save(book1);
        assertThat(bookRepository.count()).isEqualTo(1);

        bookRepository.save(book2);
        assertThat(bookRepository.count()).isEqualTo(2);

        bookRepository.save(book3);
        assertThat(bookRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should check if book exists by ID")
    void testExistsById() {
        Book savedBook = bookRepository.save(book1);

        assertThat(bookRepository.existsById(savedBook.getId())).isTrue();
        assertThat(bookRepository.existsById("non-existent-id")).isFalse();
    }

    @Test
    @DisplayName("Should return empty list when no books match author")
    void testFindByAuthor_NoMatch() {
        bookRepository.save(book1);
        bookRepository.save(book2);

        List<Book> books = bookRepository.findByAuthor("Non-existent Author");

        assertThat(books).isNotNull();
        assertThat(books).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no books match title")
    void testFindByTitle_NoMatch() {
        bookRepository.save(book1);

        List<Book> books = bookRepository.findByTitle("Non-existent Title");

        assertThat(books).isNotNull();
        assertThat(books).isEmpty();
    }
}
