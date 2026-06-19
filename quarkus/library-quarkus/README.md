# library-quarkus

A hands-on Quarkus + Java project that explores different ways to work with MongoDB using the official Java driver. The project uses a library domain (books, authors, reviews) as a practical context to demonstrate real driver concepts.

## What this project explores

### 1. Panache Repository Pattern — `Book`, `Review`
`Book` and `Review` use `PanacheMongoRepositoryBase`, which separates data access into a dedicated repository class. Business logic lives in the service layer, which uses the repository for standard CRUD and the raw `MongoClient` for operations that need more control.

```
BookResource → BookService → BookRepository (Panache)
                           → MongoCollection<Document> (raw driver)
```

### 2. Panache Active Record Pattern — `Author`
`Author` extends `PanacheMongoEntityBase`, embedding query methods directly on the entity as static methods. No separate repository class is needed.

```java
Author.listAll();
Author.findByNationality("German");
Author.deleteById(id);
```

```
AuthorResource → AuthorService → Author (static Panache methods)
```

### 3. Direct MongoClient — Raw Driver Queries
Some operations bypass Panache entirely and use `MongoCollection<Document>` directly. This is done when the operation needs fine-grained control, such as aggregation pipelines with custom stages or connection-level behavior.

**Example — `findByYear` using `$match` aggregation:**
```java
collection.aggregate(List.of(
    Aggregates.match(Filters.eq("year", year))
))
```

**Example — average rating per book using `$group`:**
```java
reviewsCollection.aggregate(List.of(
    Aggregates.match(Filters.eq("bookId", bookId)),
    Aggregates.group("$bookId",
        Accumulators.avg("averageRating", "$rating"),
        Accumulators.sum("totalReviews", 1)
    )
), AverageBookRating.class)
```

### 4. Capped Embedded Array — `$push` + `$each` + `$sort` + `$slice`
When a review is created, it is persisted to the `reviews` collection (full history) and also embedded in the corresponding `book` document. The book keeps only the **5 most recent reviews**, trimmed atomically by MongoDB in a single operation — no read required.

```java
Updates.pushEach("reviews", List.of(embedded),
    new PushOptions()
        .sortDocument(new Document("createdAt", 1))
        .slice(-5))
```

### 5. Centralized MongoDB Configuration — `@ConfigMapping`
All custom MongoDB properties are grouped in a single `MongoSettings` interface using SmallRye Config's `@ConfigMapping`. Method names map automatically from camelCase to kebab-case.

```java
@ConfigMapping(prefix = "mongodb")
public interface MongoSettings {
    String readPreference();
    Pool pool();

    interface Pool {
        int maxSize();
        long maxWaitTimeMs();
        // ...
    }
}
```

### 7. Read Preference
Read preference is configurable via `application.properties` without code changes:

```properties
# primary | primaryPreferred | secondary | secondaryPreferred | nearest
mongodb.read-preference=primary
```

---

## Prerequisites

Install `mongodb-cli-lab` to manage local MongoDB instances:

```shell
npm install -g @ricardohsmello/mongodb-cli-lab
```

---

## Running the application

**1. Start a local MongoDB replica set:**

```shell
mongodb-cli-lab up --topology replica-set --replicas 3 --mongodb-version 8.2 --port 28000
```

> A replica set is required for the `$push/$slice` atomic update and the connection pool simulation.

**2. Export the connection string and start the app:**

```shell
export MONGODB_URI="mongodb://localhost:28000,localhost:28001,localhost:28002/?replicaSet=rs0"
mvn quarkus:dev
```

The app starts on `http://localhost:8080`. Dev UI: `http://localhost:8080/q/dev/`.

> On first start, the seeder inserts 50 books, 20 authors, and 41 reviews automatically.

---

## API Endpoints

### Books
| Method | Path                          | Description                         |
|--------|-------------------------------|-------------------------------------|
| GET    | `/books`                      | List all books                      |
| GET    | `/books/{id}`                 | Find book by ID                     |
| GET    | `/books/min-pages/{minPages}` | Filter books by minimum page count  |
| GET    | `/books/year/{year}`          | Aggregate books by publication year |
| POST   | `/books`                      | Create a book                       |
| PUT    | `/books/{id}`                 | Update a book                       |
| DELETE | `/books/{id}`                 | Delete a book                       |

### Authors
| Method | Path                              | Description                  |
|--------|-----------------------------------|------------------------------|
| GET    | `/authors`                        | List all authors             |
| GET    | `/authors/nationality/{value}`    | Filter authors by nationality|
| POST   | `/authors`                        | Create an author             |
| PUT    | `/authors/{id}`                   | Update an author             |
| DELETE | `/authors/{id}`                   | Delete an author             |

### Reviews
| Method | Path                              | Description                                      |
|--------|-----------------------------------|--------------------------------------------------|
| GET    | `/reviews`                        | List all reviews                                 |
| GET    | `/reviews/avg-rating/{bookId}`    | Aggregate average rating and total count         |
| POST   | `/reviews`                        | Create review (persists + embeds in book)        |
| PUT    | `/reviews/{id}`                   | Update a review                                  |
| DELETE | `/reviews/{id}`                   | Delete a review                                  |

---

## HTTP test files

Ready-to-use HTTP request files are in `src/test/http/`:

- `book.http`
- `author.http`
- `review.http` *(if present)*
