package com.example.mongodb;

import com.example.mongodb.model.Book;
import com.example.mongodb.repository.BookRepository;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final MongoTemplate mongoTemplate;

    private static final int TOTAL_BOOKS = 10_000_000;
    private static final int BATCH_SIZE = 50_000;
    private static final int NUM_THREADS = 8;
    private static final Random random = new Random();

    private final long runPrefix = Math.abs(random.nextLong()) % 1_000_000L;

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

    public DataInitializer(BookRepository bookRepository, MongoTemplate mongoTemplate) {
        this.bookRepository = bookRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        long existing = bookRepository.count();
        if (existing > 0) {
            System.out.println("Database already contains " + existing + " books. Skipping initialization.");
            return;
        }

        System.out.println("Initializing database with " + TOTAL_BOOKS + " random books...");
        long startTime = System.currentTimeMillis();

        insertBooksParallel();

        long endTime = System.currentTimeMillis();
        long durationSeconds = (endTime - startTime) / 1000;
        double throughput = TOTAL_BOOKS / (double) durationSeconds;

        System.out.println("Successfully initialized " + TOTAL_BOOKS + " books in " + durationSeconds + " seconds!");
        System.out.println("Throughput: " + String.format("%.0f", throughput) + " docs/second");
    }

    private void insertBooksParallel() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<?>> futures = new ArrayList<>();

        MongoCollection<Document> collection = mongoTemplate.getDb()
                .getCollection("books")
                .withWriteConcern(WriteConcern.UNACKNOWLEDGED);

        int booksPerThread = TOTAL_BOOKS / NUM_THREADS;

        for (int threadId = 0; threadId < NUM_THREADS; threadId++) {
            final int startId = threadId * booksPerThread;
            final int endId = (threadId == NUM_THREADS - 1) ? TOTAL_BOOKS : (threadId + 1) * booksPerThread;
            final int finalThreadId = threadId;

            futures.add(executor.submit(() -> {
                insertBooksForThread(collection, startId, endId, finalThreadId);
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
    }

    private void insertBooksForThread(MongoCollection<Document> collection, int start, int end, int threadId) {
        Random threadRandom = new Random(threadId);
        List<Document> batch = new ArrayList<>(BATCH_SIZE);
        int booksCreated = 0;
        int totalForThread = end - start;

        for (int i = start; i < end; i++) {
            Document doc = new Document()
                    .append("title", generateRandomTitle(threadRandom))
                    .append("author", generateRandomAuthor(threadRandom))
                    .append("isbn", generateUniqueIsbn(i))
                    .append("publishedYear", generateRandomYear(threadRandom))
                    .append("price", generateRandomPrice(threadRandom));

            batch.add(doc);
            booksCreated++;

            if (batch.size() == BATCH_SIZE) {
                collection.insertMany(batch, new com.mongodb.client.model.InsertManyOptions().ordered(false));
                batch.clear();

                System.out.println("Thread " + threadId + " progress: " + booksCreated + "/" + totalForThread +
                        " (" + String.format("%.2f", (booksCreated * 100.0 / totalForThread)) + "%)");
            }
        }

        if (!batch.isEmpty()) {
            collection.insertMany(batch, new com.mongodb.client.model.InsertManyOptions().ordered(false));
            System.out.println("Thread " + threadId + " completed: " + booksCreated + "/" + totalForThread);
        }
    }

    private String generateUniqueIsbn(long counter) {
        long value = (runPrefix * 10_000_000L) + (counter % 10_000_000L);
        return "978-" + String.format("%010d", value);
    }

    private String generateRandomTitle(Random rnd) {
        String prefix = TITLE_PREFIXES[rnd.nextInt(TITLE_PREFIXES.length)];
        String subject = TITLE_SUBJECTS[rnd.nextInt(TITLE_SUBJECTS.length)];
        int edition = rnd.nextInt(5) + 1;

        if (rnd.nextBoolean()) {
            return prefix + " " + subject;
        } else {
            return prefix + " " + subject + " - " + edition + "th Edition";
        }
    }

    private String generateRandomAuthor(Random rnd) {
        String firstName = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];

        if (rnd.nextInt(10) < 3) {
            String firstName2 = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)];
            String lastName2 = LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
            return firstName + " " + lastName + ", " + firstName2 + " " + lastName2;
        }
        return firstName + " " + lastName;
    }

    private int generateRandomYear(Random rnd) {
        return 1990 + rnd.nextInt(36);
    }

    private double generateRandomPrice(Random rnd) {
        return Math.round((19.99 + rnd.nextDouble() * 80.0) * 100.0) / 100.0;
    }
}