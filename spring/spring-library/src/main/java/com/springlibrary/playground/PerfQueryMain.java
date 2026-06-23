package com.springlibrary.playground;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.springlibrary.model.Book;
import com.springlibrary.util.BookCodec;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.limit;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

public class PerfQueryMain {

    private static final int PAGE_SIZE = 25;

    public static void main(String[] args) {
        String uri = arg(args, 0, System.getenv().getOrDefault("MONGODB_URI", "mongodb://localhost:28000"));
        String dbName = arg(args, 1, "bookstore");
        String collectionName = arg(args, 2, "books");

        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase database = client.getDatabase(dbName);
            MongoCollection<Document> books = database.getCollection(collectionName);

            Document sample = books.find().first();
            System.out.println("Sample doc: " + (sample != null ? sample.toJson() : "COLLECTION EMPTY"));

            System.out.println("\n-- Recent books (publishedYear > 2010), sorted ascending --");
            findByYearGreaterThan(books, 2010);

            System.out.println("\n-- Books priced between 20 and 40 --");
            findByPriceBetween(books, 20.0, 40.0);

            System.out.println("\n-- Count of books per publishedYear (aggregation) --");
            countByYear(books);

            System.out.println("\n-- Same query decoded straight into Book via a custom codec --");
            findAsBooks(database, collectionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findByYearGreaterThan(MongoCollection<Document> books, int year) {
        try (MongoCursor<Document> cursor = books.find(gt("publishedYear", year))
                .sort(ascending("publishedYear"))
                .projection(include("title", "publishedYear", "price"))
                .limit(PAGE_SIZE)
                .iterator()) {
            print(cursor);
        }
    }

    private static void findByPriceBetween(MongoCollection<Document> books, double min, double max) {
        try (MongoCursor<Document> cursor = books.find(gte("price", min))
                .filter(lte("price", max))
                .projection(include("title", "price"))
                .limit(PAGE_SIZE)
                .iterator()) {
            print(cursor);
        }
    }

    private static void countByYear(MongoCollection<Document> books) {
        try (MongoCursor<Document> cursor = books.aggregate(List.of(
                group("$publishedYear", sum("total", 1)),
                sort(descending("total")),
                limit(10))).iterator()) {
            print(cursor);
        }
    }

    private static void findAsBooks(MongoDatabase database, String collectionName) {
        CodecRegistry registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(new BookCodec()),
                MongoClientSettings.getDefaultCodecRegistry());

        MongoCollection<Book> books = database.getCollection(collectionName, Book.class)
                .withCodecRegistry(registry);

        books.find(gt("publishedYear", 2010)).limit(5).forEach(System.out::println);
    }

    private static void print(MongoCursor<Document> cursor) {
        while (cursor.hasNext()) {
            System.out.println(cursor.next().toJson());
        }
    }

    private static String arg(String[] args, int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }
}
