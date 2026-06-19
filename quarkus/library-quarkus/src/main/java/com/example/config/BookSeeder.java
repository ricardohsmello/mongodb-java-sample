package com.example.config;

import com.example.model.Author;
import com.example.model.Book;
import com.example.model.Review;
import com.example.repository.BookRepository;
import com.example.repository.ReviewRepository;
import com.example.service.ReviewService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class BookSeeder {

    private static final Logger LOG = Logger.getLogger(BookSeeder.class);

    @Inject BookRepository bookRepository;
    @Inject ReviewRepository reviewRepository;
    @Inject ReviewService reviewService;

    void onStart(@Observes StartupEvent event) {
        if (bookRepository.count() > 0) {
            LOG.info("Seed skipped — collection already has data.");
            return;
        }

        LOG.info("Seeding authors...");
        Author.persist(seedAuthors());
        LOG.infof("%d authors inserted.", Author.count());

        LOG.info("Seeding books...");
        List<Book> books = seedBooks();
        bookRepository.persist(books);
        LOG.infof("%d books inserted.", books.size());

        LOG.info("Seeding reviews...");
        seedReviews(books);
        LOG.infof("Seed complete — %d reviews inserted.", reviewRepository.count());
    }

    // ── Authors ───────────────────────────────────────────────────────────────

    private List<Author> seedAuthors() {
        return List.of(
            author("Robert C. Martin",   "American", 1952),
            author("Martin Fowler",      "British",  1963),
            author("Eric Evans",         "American", 1957),
            author("Martin Kleppmann",   "German",   1985),
            author("Alex Petrov",        "Russian",  1986),
            author("Michael T. Nygard",  "American", 1971),
            author("John Ousterhout",    "American", 1954),
            author("Andrew Hunt",        "American", 1964),
            author("David Thomas",       "British",  1956),
            author("Kent Beck",          "American", 1961),
            author("Erich Gamma",        "Swiss",    1961),
            author("Sam Newman",         "British",  1978),
            author("Donald Knuth",       "American", 1938),
            author("Gregor Hohpe",       "German",   1971),
            author("Bobby Woolf",        "American", 1965),
            author("Joshua Bloch",       "American", 1961),
            author("Brian Goetz",        "American", 1967),
            author("Gene Kim",           "American", 1971),
            author("Jez Humble",         "British",  1977),
            author("Fred Brooks",        "American", 1931)
        );
    }

    // ── Books ─────────────────────────────────────────────────────────────────

    private List<Book> seedBooks() {
        return List.of(
            // Clean Code / Best Practices
            book("Clean Code",                                         464,  2008, List.of("Robert C. Martin")),
            book("The Clean Coder",                                    256,  2011, List.of("Robert C. Martin")),
            book("Clean Architecture",                                 432,  2017, List.of("Robert C. Martin")),
            book("The Pragmatic Programmer",                           352,  1999, List.of("Andrew Hunt", "David Thomas")),
            book("Code Complete",                                      960,  2004, List.of("Steve McConnell")),
            book("Refactoring",                                        448,  1999, List.of("Martin Fowler")),
            book("Working Effectively with Legacy Code",               456,  2004, List.of("Michael Feathers")),
            book("A Philosophy of Software Design",                    190,  2018, List.of("John Ousterhout")),
            book("Implementation Patterns",                            176,  2007, List.of("Kent Beck")),
            book("97 Things Every Programmer Should Know",             258,  2010, List.of("Kevlin Henney")),

            // Architecture & Design
            book("Domain-Driven Design",                               560,  2003, List.of("Eric Evans")),
            book("Design Patterns",                                    395,  1994, List.of("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides")),
            book("Enterprise Integration Patterns",                    736,  2003, List.of("Gregor Hohpe", "Bobby Woolf")),
            book("Building Microservices",                             616,  2021, List.of("Sam Newman")),
            book("Microservices Patterns",                             520,  2018, List.of("Chris Richardson")),
            book("Fundamentals of Software Architecture",              422,  2020, List.of("Mark Richards", "Neal Ford")),
            book("Release It!",                                        376,  2018, List.of("Michael T. Nygard")),
            book("Head First Design Patterns",                         694,  2004, List.of("Eric Freeman", "Elisabeth Robson")),
            book("Growing Object-Oriented Software",                   384,  2009, List.of("Steve Freeman", "Nat Pryce")),
            book("Pattern-Oriented Software Architecture",             476,  1996, List.of("Frank Buschmann")),

            // Databases & Data
            book("Designing Data-Intensive Applications",              600,  2017, List.of("Martin Kleppmann")),
            book("Database Internals",                                 472,  2019, List.of("Alex Petrov")),
            book("NoSQL Distilled",                                    192,  2012, List.of("Martin Fowler", "Pramod Sadalage")),
            book("MongoDB: The Definitive Guide",                      514,  2019, List.of("Shannon Bradshaw")),
            book("Redis in Action",                                    312,  2013, List.of("Josiah Carlson")),
            book("Elasticsearch: The Definitive Guide",                724,  2015, List.of("Clinton Gormley", "Zachary Tong")),
            book("Seven Databases in Seven Weeks",                     354,  2012, List.of("Eric Redmond", "Jim Wilson")),
            book("SQL Antipatterns",                                   352,  2010, List.of("Bill Karwin")),
            book("High Performance MySQL",                             828,  2012, List.of("Baron Schwartz")),
            book("The Art of PostgreSQL",                              394,  2019, List.of("Dimitri Fontaine")),

            // Distributed Systems & DevOps
            book("Site Reliability Engineering",                       552,  2016, List.of("Betsy Beyer", "Chris Jones")),
            book("The Phoenix Project",                                432,  2013, List.of("Gene Kim", "Kevin Behr", "George Spafford")),
            book("The DevOps Handbook",                                480,  2016, List.of("Gene Kim", "Jez Humble", "Patrick Debois")),
            book("Accelerate",                                         288,  2018, List.of("Nicole Forsgren", "Jez Humble", "Gene Kim")),
            book("Kafka: The Definitive Guide",                        320,  2017, List.of("Neha Narkhede", "Gwen Shapira")),
            book("Kubernetes in Action",                               624,  2017, List.of("Marko Lukša")),
            book("Docker Deep Dive",                                   228,  2023, List.of("Nigel Poulton")),
            book("Terraform: Up & Running",                            392,  2022, List.of("Yevgeniy Brikman")),
            book("Cloud Native Patterns",                              395,  2019, List.of("Cornelia Davis")),
            book("Continuous Delivery",                                512,  2010, List.of("Jez Humble", "David Farley")),

            // CS Fundamentals & Career
            book("Introduction to Algorithms",                        1292,  2022, List.of("Thomas Cormen")),
            book("The Art of Computer Programming",                   3168,  1968, List.of("Donald Knuth")),
            book("Structure and Interpretation of Computer Programs",  657,  1996, List.of("Harold Abelson", "Gerald Sussman")),
            book("Algorithms",                                         992,  2011, List.of("Robert Sedgewick", "Kevin Wayne")),
            book("Effective Java",                                     412,  2018, List.of("Joshua Bloch")),
            book("Java Concurrency in Practice",                       384,  2006, List.of("Brian Goetz")),
            book("Test-Driven Development",                            240,  2002, List.of("Kent Beck")),
            book("The Mythical Man-Month",                             336,  1975, List.of("Fred Brooks")),
            book("Extreme Programming Explained",                      224,  2004, List.of("Kent Beck")),
            book("Soft Skills",                                        504,  2014, List.of("John Sonmez"))
        );
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    private void seedReviews(List<Book> books) {
        Map<String, String> id = books.stream()
                .collect(Collectors.toMap(b -> b.title, b -> b.id));

        // Clean Code — 7 reviews (trims to 5 most recent)
        String cc = id.get("Clean Code");
        reviewService.create(review(cc, "alice",   5.0, "A must-read for every developer.",               90));
        reviewService.create(review(cc, "bob",     4.0, "Great principles, some examples feel dated.",    75));
        reviewService.create(review(cc, "carol",   5.0, "Changed how I think about naming.",              60));
        reviewService.create(review(cc, "dave",    3.5, "Useful but too Java-centric.",                   45));
        reviewService.create(review(cc, "eve",     4.5, "The chapter on functions alone is worth it.",    30));
        reviewService.create(review(cc, "frank",   4.0, "Classic. Re-read it every year.",               12));
        reviewService.create(review(cc, "grace",   5.0, "Best book on clean code practices.",             2));

        // The Pragmatic Programmer — 5 reviews
        String pp = id.get("The Pragmatic Programmer");
        reviewService.create(review(pp, "heidi",   5.0, "Timeless advice for software craftsmen.",       80));
        reviewService.create(review(pp, "ivan",    4.5, "DRY and KISS principles explained perfectly.",  55));
        reviewService.create(review(pp, "judy",    4.0, "A bit dated but still very relevant.",          35));
        reviewService.create(review(pp, "karl",    5.0, "Required reading at our company.",              18));
        reviewService.create(review(pp, "laura",   4.5, "Covers so much more than just programming.",     4));

        // Designing Data-Intensive Applications — 6 reviews (trims to 5)
        String ddia = id.get("Designing Data-Intensive Applications");
        reviewService.create(review(ddia, "mike",   5.0, "The best book on distributed systems.",        70));
        reviewService.create(review(ddia, "nancy",  5.0, "Dense but incredibly rewarding.",              55));
        reviewService.create(review(ddia, "oscar",  4.5, "Essential for any backend engineer.",          40));
        reviewService.create(review(ddia, "paula",  5.0, "Explains CAP theorem better than anyone.",     25));
        reviewService.create(review(ddia, "quinn",  4.0, "Great reference, not beginner-friendly.",      11));
        reviewService.create(review(ddia, "rachel", 5.0, "Should be mandatory in CS degrees.",            2));

        // Domain-Driven Design — 4 reviews
        String ddd = id.get("Domain-Driven Design");
        reviewService.create(review(ddd, "sam",    4.0, "Heavy read but foundational.",                  65));
        reviewService.create(review(ddd, "tina",   3.5, "Great concepts, very dry examples.",            40));
        reviewService.create(review(ddd, "ursula", 4.5, "Finally understood bounded contexts.",          20));
        reviewService.create(review(ddd, "victor", 5.0, "Changed how I design entire systems.",           5));

        // Refactoring — 4 reviews
        String ref = id.get("Refactoring");
        reviewService.create(review(ref, "wendy",  5.0, "The refactoring catalog is invaluable.",        50));
        reviewService.create(review(ref, "xavier", 4.0, "2nd edition updated to JavaScript examples.",   35));
        reviewService.create(review(ref, "yvonne", 4.5, "Changed how I approach messy code.",            18));
        reviewService.create(review(ref, "zach",   5.0, "Pair this with Working Effectively with Legacy Code.", 4));

        // Design Patterns — 4 reviews
        String gof = id.get("Design Patterns");
        reviewService.create(review(gof, "alice",  4.0, "Dense but essential reference.",               100));
        reviewService.create(review(gof, "bob",    3.5, "Academic but the patterns are timeless.",       72));
        reviewService.create(review(gof, "carol",  4.5, "The Gang of Four classic — still relevant.",    38));
        reviewService.create(review(gof, "dave",   5.0, "Every pattern explained with clarity.",          9));

        // Effective Java — 4 reviews
        String ej = id.get("Effective Java");
        reviewService.create(review(ej, "eve",    5.0, "Best Java book ever written.",                   45));
        reviewService.create(review(ej, "frank",  4.5, "Bloch distills years of JDK experience.",        28));
        reviewService.create(review(ej, "grace",  5.0, "Every item is a lesson in good API design.",     12));
        reviewService.create(review(ej, "heidi",  4.0, "A must for any serious Java developer.",          3));

        // Building Microservices — 3 reviews
        String ms = id.get("Building Microservices");
        reviewService.create(review(ms, "ivan",   5.0, "The definitive guide to microservices.",         33));
        reviewService.create(review(ms, "judy",   4.0, "2nd edition adds great Kubernetes coverage.",    16));
        reviewService.create(review(ms, "karl",   4.5, "Practical, thorough, and well-structured.",       7));

        // The Phoenix Project — 4 reviews
        String phoenix = id.get("The Phoenix Project");
        reviewService.create(review(phoenix, "laura",  5.0, "The DevOps novel. Couldn't put it down.",   55));
        reviewService.create(review(phoenix, "mike",   4.5, "Teaches DevOps principles through story.",  38));
        reviewService.create(review(phoenix, "nancy",  4.0, "Great for managers and developers alike.",  20));
        reviewService.create(review(phoenix, "oscar",  5.0, "Every IT team should read this together.",   8));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Author author(String name, String nationality, int birthYear) {
        Author a = new Author();
        a.id = new ObjectId().toHexString();
        a.name = name;
        a.nationality = nationality;
        a.birthYear = birthYear;
        return a;
    }

    private Book book(String title, int pages, int year, List<String> authors) {
        Book b = new Book();
        b.id = new ObjectId().toHexString();
        b.title = title;
        b.pages = pages;
        b.year = year;
        b.authors = authors;
        return b;
    }

    private Review review(String bookId, String user, double rating, String text, int daysAgo) {
        Review r = new Review();
        r.bookId = bookId;
        r.user = user;
        r.rating = rating;
        r.text = text;
        r.createdAt = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        return r;
    }

}
