package com.example.service;

import com.example.model.Book;
import com.example.repository.BookRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BookService {

    private final BookRepository bookRepository;
    private final MongoCollection<Document> collection;

    @Inject
    BookService(MongoDatabase db, BookRepository bookRepository) {
        this.collection = db.getCollection("books");
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.listAll();
    }

    public List<Book> findByMinPages(int minPages) {
        return bookRepository.findByPagesGreaterThan(minPages);
    }

    public Book findById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> findByYear(int year) {
        var match = Aggregates.match(Filters.eq("year", year));
        var aggregates = List.of(match);

        return collection
                .aggregate(aggregates)
                .into(new ArrayList<>())
                .stream()
                .map(this::toBook)
                .toList();
    }

    public List<Book> find() {
        var aggregate = List.of(
                Aggregates.match(Filters.exists("title"))
        );

        return collection.aggregate(aggregate)
                .into(new ArrayList<>())
                .stream().map(this::toBook)
                .toList();
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

    private Book toBook(Document doc) {
        Book b = new Book();
        b.id = doc.get("_id").toString();
        b.title = doc.getString("title");
        b.pages = doc.getInteger("pages", 0);
        b.year = doc.getInteger("year", 0);
        b.authors = doc.getList("authors", String.class);
        return b;
    }

    public List<Document> classifyByPageCount() {
        var setStage = new Document("$set", new Document("pageCategory",
                new Document("$switch", new Document()
                        .append("branches", List.of(
                                new Document("case", new Document("$lte", List.of("$pages", 250))).append("then", "short"),
                                new Document("case", new Document("$lte", List.of("$pages", 500))).append("then", "medium")
                        ))
                        .append("default", "long"))));

        return collection.aggregate(List.of(setStage)).into(new ArrayList<>());
    }

    public List<Document> countBooksPerAuthor() {
        return collection.aggregate(List.of(
                Aggregates.unwind("$authors"),
                Aggregates.group("$authors", Accumulators.sum("totalBooks", 1)),
                Aggregates.sort(Sorts.descending("totalBooks"))
        )).into(new ArrayList<>());
    }

    public List<Book> sortedByYear(String order) {
        var sort = "asc".equalsIgnoreCase(order)
                ? Sorts.ascending("year")
                : Sorts.descending("year");

        return collection.aggregate(List.of(Aggregates.sort(sort)))
                .into(new ArrayList<>())
                .stream()
                .map(this::toBook)
                .toList();
    }

    public List<Document> booksWithReviews() {
        return collection.aggregate(List.of(
                Aggregates.lookup("reviews", "_id", "bookId", "allReviews"),
                Aggregates.match(Filters.not(Filters.size("allReviews", 0)))
        )).into(new ArrayList<>());
    }

    public List<Book> longestBooks(int limit) {
        return collection.aggregate(List.of(
                Aggregates.sort(Sorts.descending("pages")),
                Aggregates.limit(limit)
        )).into(new ArrayList<>())
                .stream()
                .map(this::toBook)
                .toList();
    }

}
