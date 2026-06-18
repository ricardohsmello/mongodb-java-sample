package com.example.service;

import com.example.model.Book;
import com.example.repository.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class BookService {

    @Inject
    BookRepository bookRepository;

    public List<Book> findAll() {
        return bookRepository.listAll();
    }

    public List<Book> findByMinPages(int minPages) {
        return bookRepository.findByPagesGreaterThan(minPages);
    }

    public String create(Book book) {
        book.id = new ObjectId().toHexString();
        bookRepository.persist(book);
        return book.id;
    }

    public boolean update(String id, Book book) {
        book.id = id;
        return bookRepository.updateBook(book);
    }

    public boolean delete(String id) {
        return bookRepository.deleteBook(id);
    }

}
