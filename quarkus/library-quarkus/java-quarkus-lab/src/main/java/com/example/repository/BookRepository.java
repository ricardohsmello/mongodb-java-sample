package com.example.repository;

import com.example.model.Book;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class BookRepository implements PanacheMongoRepositoryBase<Book, String> {

    public List<Book> findByPagesGreaterThan(int minPages) {
        return find("pages > ?1", minPages).list();
    }

    public boolean updateBook(Book book) {
        Book existing = findById(book.id);
        if (existing == null) {
            return false;
        }
        existing.title = book.title;
        existing.pages = book.pages;
        existing.year = book.year;
        persistOrUpdate(existing);

        return true;
    }

    public boolean deleteBook(String id) {
        Book existing = findById(id);
        if (existing == null) {
            return false;
        }
        delete(existing);
        return true;
    }

}