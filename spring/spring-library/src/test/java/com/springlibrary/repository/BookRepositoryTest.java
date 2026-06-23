package com.springlibrary.repository;

import com.springlibrary.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Book Repository Tests")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book cleanCode;
    private Book effectiveJava;
    private Book cleanArchitecture;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        cleanCode = new Book("Clean Code", "Robert C. Martin", "978-0132350884", 2008, 32.99);
        effectiveJava = new Book("Effective Java", "Joshua Bloch", "978-0134685991", 2017, 45.50);
        cleanArchitecture = new Book("Clean Architecture", "Robert C. Martin", "978-0134494166", 2017, 29.99);
    }

    @Test
    @DisplayName("Should save and retrieve a book by id")
    void savesAndFindsBook() {
        Book saved = bookRepository.save(cleanCode);

        assertThat(saved.getId()).isNotNull();

        Optional<Book> found = bookRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Clean Code");
        assertThat(found.get().getAuthor()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("Should find all books")
    void findsAllBooks() {
        bookRepository.saveAll(List.of(cleanCode, effectiveJava, cleanArchitecture));

        assertThat(bookRepository.findAll())
                .hasSize(3)
                .extracting(Book::getTitle)
                .contains("Clean Code", "Effective Java", "Clean Architecture");
    }

    @Test
    @DisplayName("Should find books by author")
    void findsByAuthor() {
        bookRepository.saveAll(List.of(cleanCode, effectiveJava, cleanArchitecture));

        assertThat(bookRepository.findByAuthor("Robert C. Martin"))
                .hasSize(2)
                .extracting(Book::getTitle)
                .contains("Clean Code", "Clean Architecture");
    }

    @Test
    @DisplayName("Should find books by title")
    void findsByTitle() {
        bookRepository.saveAll(List.of(cleanCode, effectiveJava));

        List<Book> books = bookRepository.findByTitle("Clean Code");

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAuthor()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("Should find a book by ISBN")
    void findsByIsbn() {
        bookRepository.saveAll(List.of(cleanCode, effectiveJava));

        Optional<Book> found = bookRepository.findByIsbn("978-0132350884");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should return empty optional when ISBN is unknown")
    void findsByIsbn_notFound() {
        bookRepository.save(cleanCode);

        assertThat(bookRepository.findByIsbn("000-000-000")).isEmpty();
    }

    @Test
    @DisplayName("Should update an existing book")
    void updatesBook() {
        Book saved = bookRepository.save(cleanCode);

        saved.setTitle("Clean Code - Second Edition");
        saved.setPrice(39.99);
        bookRepository.save(saved);

        Optional<Book> updated = bookRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Clean Code - Second Edition");
        assertThat(updated.get().getPrice()).isEqualTo(39.99);
    }

    @Test
    @DisplayName("Should delete a book by id")
    void deletesBook() {
        Book saved = bookRepository.save(cleanCode);

        bookRepository.deleteById(saved.getId());

        assertThat(bookRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should count books")
    void countsBooks() {
        assertThat(bookRepository.count()).isZero();

        bookRepository.saveAll(List.of(cleanCode, effectiveJava, cleanArchitecture));

        assertThat(bookRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return empty list when no book matches the author")
    void findsByAuthor_noMatch() {
        bookRepository.saveAll(List.of(cleanCode, effectiveJava));

        assertThat(bookRepository.findByAuthor("Non-existent Author")).isEmpty();
    }
}
