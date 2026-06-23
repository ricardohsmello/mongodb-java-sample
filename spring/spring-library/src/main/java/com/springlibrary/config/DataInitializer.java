package com.springlibrary.config;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    private static final int TOTAL_BOOKS = 10_000_000;
    private static final int BATCH_SIZE  = 10_000;

    private static final Random rnd = new Random();
    private final long runPrefix = Math.abs(rnd.nextLong()) % 1_000_000L;

    private static final String[] TITLE_PREFIXES = {
            "The Art of", "Introduction to", "Mastering", "Learning", "Advanced",
            "Complete Guide to", "Practical", "Professional", "Essential", "Modern"
    };
    private static final String[] TITLE_SUBJECTS = {
            "Programming", "Data Science", "Machine Learning", "Web Development",
            "Cloud Computing", "Algorithms", "Databases", "Software Engineering",
            "Artificial Intelligence", "Cybersecurity", "DevOps", "Mobile Development"
    };

    private static final String[] FIRST_NAMES = {
            "John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Lisa",
            "James", "Mary", "William", "Jennifer", "Richard", "Patricia", "Thomas"
    };
    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Wilson", "Anderson", "Taylor", "Moore"
    };

    public DataInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        MongoCollection<Document> books = mongoTemplate.getDb().getCollection("books");

        long existing = books.estimatedDocumentCount();
        if (existing > 0) {
            System.out.println("DB already has ~" + existing + " docs, skipping init.");
            return;
        }

        System.out.println("Inserting " + TOTAL_BOOKS + " books in batches of " + BATCH_SIZE + "…");
        long startedAt = System.currentTimeMillis();

        insertBooksBulk(books);

        long seconds = (System.currentTimeMillis() - startedAt) / 1000;
        double docsPerSecond = (seconds == 0) ? TOTAL_BOOKS : (TOTAL_BOOKS / (double) seconds);
        System.out.println("Done: " + TOTAL_BOOKS + " docs in " + seconds + "s (" + (long) docsPerSecond + " docs/s).");
    }

    private void insertBooksBulk(MongoCollection<Document> books) {
        List<Document> batch = new ArrayList<>(BATCH_SIZE);
        InsertManyOptions options = new InsertManyOptions().ordered(false);

        for (int i = 0; i < TOTAL_BOOKS; i++) {
            batch.add(makeDoc(i));
            if (batch.size() == BATCH_SIZE) {
                books.insertMany(batch, options);
                logProgress(i + 1, TOTAL_BOOKS);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            books.insertMany(batch, options);
            logProgress(TOTAL_BOOKS, TOTAL_BOOKS);
        }
    }

    private void logProgress(int done, int total) {
        if (done % 100_000 == 0 || done == total) {
            double pct = (done * 100.0) / total;
            System.out.println("Progress: " + done + "/" + total + " (" + String.format("%.2f", pct) + "%)");
        }
    }

    private Document makeDoc(long index) {
        return new Document("title", generateRandomTitle())
                .append("author", generateRandomAuthor())
                .append("isbn", generateUniqueIsbn(index))
                .append("publishedYear", 1990 + rnd.nextInt(36))
                .append("price", Math.round((19.99 + rnd.nextDouble() * 80.0) * 100.0) / 100.0);
    }

    private String generateUniqueIsbn(long counter) {
        long value = (runPrefix * 10_000_000L) + (counter % 10_000_000L);
        return "978-" + String.format("%010d", value);
    }

    private String generateRandomTitle() {
        String prefix = TITLE_PREFIXES[rnd.nextInt(TITLE_PREFIXES.length)];
        String subject = TITLE_SUBJECTS[rnd.nextInt(TITLE_SUBJECTS.length)];
        int edition = rnd.nextInt(5) + 1;
        return rnd.nextBoolean()
                ? prefix + " " + subject
                : prefix + " " + subject + " - " + edition + "th Edition";
    }

    private String generateRandomAuthor() {
        String author = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
        if (rnd.nextInt(10) < 3) {
            author += ", " + FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)] + " " + LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
        }
        return author;
    }
}
