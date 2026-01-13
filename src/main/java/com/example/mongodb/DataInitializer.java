package com.example.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
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
        MongoCollection<Document> col = mongoTemplate.getDb()
                .getCollection("books");

        long existing = col.estimatedDocumentCount();
        if (existing > 0) {
            System.out.println("DB already has ~" + existing + " docs, skipping init.");
            return;
        }

        System.out.println("Inserting " + TOTAL_BOOKS + " books in batches of " + BATCH_SIZE + "…");
        long t0 = System.currentTimeMillis();

        insertBooksBulk(col);

        long secs = (System.currentTimeMillis() - t0) / 1000;
        double tps = (secs == 0) ? TOTAL_BOOKS : (TOTAL_BOOKS / (double) secs);
        System.out.println("Done: " + TOTAL_BOOKS + " docs in " + secs + "s (" + (long)tps + " docs/s).");
    }

    private void insertBooksBulk(MongoCollection<Document> col) {
        List<Document> batch = new ArrayList<>(BATCH_SIZE);
        InsertManyOptions opts = new InsertManyOptions().ordered(false);

        for (int i = 0; i < TOTAL_BOOKS; i++) {
            batch.add(makeDoc(i));
            if (batch.size() == BATCH_SIZE) {
                col.insertMany(batch, opts);
                logProgress(i + 1, TOTAL_BOOKS);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            col.insertMany(batch, opts);
            logProgress(TOTAL_BOOKS, TOTAL_BOOKS);
        }
    }

    private void logProgress(int done, int total) {
        if (done % (100_000) == 0 || done == total) {
            double pct = (done * 100.0) / total;
            System.out.println("Progress: " + done + "/" + total + " (" + String.format("%.2f", pct) + "%)");
        }
    }

    private Document makeDoc(long i) {
        Random r = rnd;
        String title = generateRandomTitle(r);
        String author = generateRandomAuthor(r);
        String isbn = generateUniqueIsbn(i);
        int year = 1990 + r.nextInt(36);
        double price = Math.round((19.99 + r.nextDouble() * 80.0) * 100.0) / 100.0;

        return new Document("title", title)
                .append("author", author)
                .append("isbn", isbn)
                .append("publishedYear", year)
                .append("price", price);
    }

    private String generateUniqueIsbn(long counter) {
        long value = (runPrefix * 10_000_000L) + (counter % 10_000_000L);
        return "978-" + String.format("%010d", value);
    }

    private String generateRandomTitle(Random r) {
        String prefix = TITLE_PREFIXES[r.nextInt(TITLE_PREFIXES.length)];
        String subject = TITLE_SUBJECTS[r.nextInt(TITLE_SUBJECTS.length)];
        int edition = r.nextInt(5) + 1;
        return r.nextBoolean()
                ? prefix + " " + subject
                : prefix + " " + subject + " - " + edition + "th Edition";
    }

    private String generateRandomAuthor(Random r) {
        String first = FIRST_NAMES[r.nextInt(FIRST_NAMES.length)];
        String last  = LAST_NAMES[r.nextInt(LAST_NAMES.length)];
        if (r.nextInt(10) < 3) {
            String first2 = FIRST_NAMES[r.nextInt(FIRST_NAMES.length)];
            String last2  = LAST_NAMES[r.nextInt(LAST_NAMES.length)];
            return first + " " + last + ", " + first2 + " " + last2;
        }
        return first + " " + last;
    }
}
