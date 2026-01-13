package com.example.mongodb.controller;

import com.example.mongodb.model.Book;
import com.example.mongodb.repository.BookRepository;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable String author) {
        List<Book> books = bookRepository.findByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<List<Book>> getBooksByTitle(@PathVariable String title) {
        List<Book> books = bookRepository.findByTitle(title);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book != null) {
            return ResponseEntity.ok(book);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookRepository.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book bookDetails) {
        Optional<Book> bookOptional = bookRepository.findById(id);

        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            book.setTitle(bookDetails.getTitle());
            book.setAuthor(bookDetails.getAuthor());
            book.setIsbn(bookDetails.getIsbn());
            book.setPublishedYear(bookDetails.getPublishedYear());
            book.setPrice(bookDetails.getPrice());

            Book updatedBook = bookRepository.save(book);
            return ResponseEntity.ok(updatedBook);
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllBooks() {
        bookRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getBookCount() {
        long count = bookRepository.count();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/replaceByTitle/{title}")
    public ResponseEntity<String> replaceOneByTitle(@PathVariable String title, @RequestBody Book replacementBook) {
        Query query = new Query(Criteria.where("title").is(title));

        UpdateResult result = mongoTemplate.replace(query, replacementBook, "books");

        if (result.getMatchedCount() > 0) {
            if (result.getModifiedCount() > 0) {
                return ResponseEntity.ok("Book with title '" + title + "' was replaced successfully. Matched: " +
                                       result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
            } else {
                return ResponseEntity.ok("Book with title '" + title + "' was found but not modified (same content). Matched: " +
                                       result.getMatchedCount());
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                           .body("No book found with title '" + title + "'");
    }
}
