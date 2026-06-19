# library-quarkus

A hands-on Quarkus + Java project designed to explore MongoDB driver concepts in practice. It covers:

- **Panache** — repository pattern with `PanacheMongoRepositoryBase`
- **Direct MongoClient access** — bypassing Panache to run operations within explicit sessions and transactions
- **Aggregation** — common pipeline stages via the MongoDB Java driver
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

