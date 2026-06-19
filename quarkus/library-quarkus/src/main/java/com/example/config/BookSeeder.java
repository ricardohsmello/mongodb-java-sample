package com.example.config;

import com.example.model.Book;
import com.example.repository.BookRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class BookSeeder {

    private static final Logger LOG = Logger.getLogger(BookSeeder.class);

    @Inject
    BookRepository bookRepository;

    void onStart(@Observes StartupEvent event) {
        if (bookRepository.count() > 0) {
            LOG.info("Seed skipped — collection already has data.");
            return;
        }

        LOG.info("Seeding books collection...");
        bookRepository.persist(seed());
        LOG.infof("Seed complete — %d books inserted.", bookRepository.count());
    }

    private List<Book> seed() {
        return List.of(
            book("Clean Code", 464, 2008, List.of("Robert C. Martin")),
            book("The Pragmatic Programmer", 352, 1999, List.of("Andrew Hunt", "David Thomas")),
            book("Designing Data-Intensive Applications", 600, 2017, List.of("Martin Kleppmann")),
            book("Domain-Driven Design", 560, 2003, List.of("Eric Evans")),
            book("Refactoring", 448, 1999, List.of("Martin Fowler")),
            book("A Philosophy of Software Design", 190, 2018, List.of("John Ousterhout")),
            book("Release It!", 376, 2018, List.of("Michael T. Nygard")),
            book("Database Internals", 472, 2019, List.of("Alex Petrov"))
        );
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
}
