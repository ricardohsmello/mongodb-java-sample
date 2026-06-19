


# library-quarkus

A hands-on Quarkus + Java project designed to explore MongoDB driver concepts in practice. It covers:

- **Panache** — repository pattern with `PanacheMongoRepositoryBase`
- **Direct MongoClient access** — bypassing Panache to run operations within explicit sessions and transactions
- **Aggregation** — common pipeline stages via the MongoDB Java driver
- **Connection pool simulation** — reproducing `MongoTimeoutException` by holding connections with transactions and firing concurrent requests
- **Read preference** — configuring and switching between `primary`, `secondary`, `primaryPreferred`, and other modes via `application.properties`

## Prerequisites

Install `mongodb-cli-lab` to manage local MongoDB instances:

```shell
npm install -g @ricardohsmello/mongodb-cli-lab
```

## Running the application

**1. Start MongoDB:**

```shell
mongodb-cli-lab up --topology replica-set --replicas 3 --mongodb-version 8.2 --port 28000
```

**2. Set the connection string and start the app:**

```shell
export MONGODB_URI="mongodb://localhost:28000,localhost:28001,localhost:28002/?replicaSet=rs0"
mvn quarkus:dev
```

The app starts on `http://localhost:8080`. Dev UI is available at `http://localhost:8080/q/dev/`.

## API endpoints

| Method | Path                        | Description              |
|--------|-----------------------------|--------------------------|
| GET    | /books                      | List all books           |
| GET    | /books/min-pages/{minPages} | Filter by minimum pages  |
| POST   | /books                      | Create a book            |
| PUT    | /books/{id}                 | Update a book            |
| DELETE | /books/{id}                 | Delete a book            |
| GET    | /books/slow                 | Slow endpoint (4s sleep) |

## Simulating connection pool exhaustion

The `/books/slow` endpoint opens a MongoDB transaction, fetches all books, holds the connection pinned for 20 seconds, then commits. This guarantees the connection is NOT returned to the pool during the sleep.

### 1. Start a replica set

Transactions require a replica set. Use `mongodb-cli-lab` to spin one up locally:

```shell
mongodb-cli-lab up --topology replica-set --replicas 3 --mongodb-version 8.2 --port 28000
```

Then set the connection string accordingly:

```shell
export MONGODB_URI="mongodb://localhost:28000,localhost:28001,localhost:28002/?replicaSet=rs0"
```

### 2. Start the application with a small pool

Activate the `simulation` profile, which sets `max-size=2` and `timeoutMS=500`:

```shell
QUARKUS_PROFILE=simulation mvn quarkus:dev
```

> **Why `timeout-ms=500`?** `mongodb.timeout-ms` maps to CSOT (`timeoutMS`), which covers the full operation — server selection, connection checkout from the pool, and server execution — in a single deadline. The production default is 0 (disabled). With only 2 connections and each `/slow` request holding one for 20 seconds, requests 3+ will fail after 500ms with `MongoOperationTimeoutException`.

### 3. Fire concurrent requests

In a separate terminal:

```shell
for i in {1..10}; do curl -s http://localhost:8080/books/slow & done; wait
```

With only 2 connections in the pool and each request holding one for 20 seconds, requests 3+ wait up to 500ms for a free connection. When that timeout expires they fail with `MongoTimeoutException`. Watch the app logs for the error.

## Connection pool settings

| Property | Default | Description |
|---|---|---|
| `mongodb.pool.max-size` | `100` | Maximum connections in the pool |
| `mongodb.pool.min-size` | `0` | Minimum idle connections kept open |
| `mongodb.pool.max-connection-idle-time-ms` | `0` | Max idle time before a connection is closed (`0` = no limit) |
| `mongodb.pool.max-connection-life-time-ms` | `0` | Max total lifetime of a connection (`0` = no limit) |
| `mongodb.timeout-ms` | `0` | CSOT — single deadline covering server selection, checkout, and execution (`0` = disabled) |
