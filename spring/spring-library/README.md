# spring-library

A small **Spring Boot + Spring Data MongoDB** sample built around a `Book`
domain. It shows a clean, layered REST API (controller → service → repository)
with DTOs, a mapper, bean validation, and centralized error handling — plus a
raw MongoDB Java Driver playground for performance queries.

## Prerequisites

- **Java 17**
- **Maven 3.9+**
- **MongoDB** on `localhost:28000` (see below)

## Start a MongoDB cluster

Use the companion CLI to spin up a simple local cluster on port `28000`:

```bash
npm install -g @ricardohsmello/mongodb-cli-lab
mongodb-cli-lab up --topology replica-set --replicas 1 --port 28000
```

This matches the app's default connection settings
(`src/main/resources/application.properties`):

```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=28000
spring.data.mongodb.database=bookstore
```

When you're done, tear it down with:

```bash
mongodb-cli-lab down
```

## Run the application

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

> **First run:** `DataInitializer` seeds the `books` collection with a large
> randomly generated dataset for the Java Driver playground. This can take a
> while; it is skipped automatically once the collection already has documents.

## API endpoints

Base path: `/api/books`

| Method | Path | Description |
|---|---|---|
| GET | `/api/books?page=&size=` | List books, paginated (`PageResponse`) |
| GET | `/api/books/{id}` | Find a book by id |
| GET | `/api/books/author/{author}` | Find books by author |
| GET | `/api/books/title/{title}` | Find books by title |
| GET | `/api/books/isbn/{isbn}` | Find a book by ISBN |
| POST | `/api/books` | Create a book (returns `201` + `Location`) |
| PUT | `/api/books/{id}` | Update a book |
| DELETE | `/api/books/{id}` | Delete a book |

Example:

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"978-0132350884","publishedYear":2008,"price":32.99}'

curl "http://localhost:8080/api/books?page=0&size=5"
```

## Run the tests

```bash
mvn test
```

Tests use an **embedded MongoDB** (flapdoodle) on a random port, so no running
MongoDB is required.

## Java Driver playground

`com.springlibrary.playground.PerfQueryMain` is a standalone `main` that queries
the `books` collection directly with the MongoDB Java Driver. Connection comes
from an argument, then the `MONGODB_URI` env var, then a local default:

```bash
java -cp target/classes com.springlibrary.playground.PerfQueryMain "mongodb://localhost:28000" bookstore books
```
