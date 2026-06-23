package com.springlibrary.repository;

import com.springlibrary.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findByAuthor(String author);

    List<Book> findByTitle(String title);

    Optional<Book> findByIsbn(String isbn);
}
