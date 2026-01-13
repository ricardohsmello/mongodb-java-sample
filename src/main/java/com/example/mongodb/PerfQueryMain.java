package com.example.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class PerfQueryMain {

	static int PAGE_SIZE = 25;
	static long SLEEP_MS = 0;
	static int MIN_publishedYear = 1990;
	static int MAX_publishedYear = 2026;
	static double MIN_PRICE = 10.0;
	static double MAX_PRICE = 100.0;
	static long STATS_EVERY_N = 200;

	static final String[] TITLE_KEYS = {
			"program", "data", "machine", "web", "cloud", "algo",
			"database", "software", "intelligence", "security", "devops", "mobile"
	};

	public static void main(String[] args) {
		String uri = args.length > 0 ? args[0] : "mongodb://localhost:28000";
		String dbName = args.length > 1 ? args[1] : "bookstore";
		String coll = args.length > 2 ? args[2] : "books";

		try (MongoClient client = MongoClients.create(uri)) {
			MongoDatabase db = client.getDatabase(dbName);
			MongoCollection<Document> books = db.getCollection(coll);

			System.out.printf("[perf] target=%s.%s | page=%d | delay=%dms%n",
					dbName, coll, PAGE_SIZE, SLEEP_MS);

			final AtomicBoolean running = new AtomicBoolean(true);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\n[perf] finishing...");
				running.set(false);
			}));

			Random rnd = new Random();
			long ops = 0;
			long totalNanos = 0;
			Instant startWall = Instant.now();

			while (running.get()) {
				long before = System.nanoTime();

				switch (rnd.nextInt(7)) {
					case 0 -> findByPublishedYear(books, rndpublishedYear(rnd));
					case 1 -> findBypublishedYearLt(books, rndpublishedYear(rnd));
					case 2 -> findBypublishedYearGt(books, rndpublishedYear(rnd));
					case 3 -> findBypublishedYearBetween(books, rndpublishedYear(rnd), rndpublishedYear(rnd));
					case 4 -> findByTitleContains(books, TITLE_KEYS[rnd.nextInt(TITLE_KEYS.length)]);
					case 5 -> findByPriceBetween(books, rndPrice(rnd), rndPrice(rnd));
					case 6 -> countVariants(books, rnd);
				}

				long dur = System.nanoTime() - before;
				totalNanos += dur;
				ops++;

				if (ops % STATS_EVERY_N == 0) {
					double avgMs = (totalNanos / 1_000_000.0) / ops;
					Duration wall = Duration.between(startWall, Instant.now());
					System.out.printf("[perf] ops=%d | avg=%.2f ms | wall=%ds%n",
							ops, avgMs, wall.toSeconds());
				}

				if (SLEEP_MS > 0) Thread.sleep(SLEEP_MS);
			}

			double avgMs = ops == 0 ? 0.0 : (totalNanos / 1_000_000.0) / ops;
			System.out.printf("[perf] fim. ops=%d | avg=%.2f ms%n", ops, avgMs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void findByPublishedYear(MongoCollection<Document> c, int publishedYear) {

		try (MongoCursor<Document> cur = c.find(Filters.eq("publishedYear", publishedYear))
				.limit(PAGE_SIZE)
				.projection(Projections.include("_id", "title", "publishedYear", "price"))
				.iterator()) {
			print(cur);
		}
	}

	static void findBypublishedYearLt(MongoCollection<Document> c, int bound) {
		try (MongoCursor<Document> cur = c.find(Filters.lt("publishedYear", bound))
				.sort(Sorts.descending("publishedYear"))
				.limit(PAGE_SIZE)
				.projection(Projections.include("_id", "title", "publishedYear"))
				.iterator()) {
			print(cur);
		}
	}

	static void findBypublishedYearGt(MongoCollection<Document> c, int bound) {
		try (MongoCursor<Document> cur = c.find(Filters.gt("publishedYear", bound))
				.sort(Sorts.ascending("publishedYear"))
				.limit(PAGE_SIZE)
				.projection(Projections.include("_id", "title", "publishedYear"))
				.iterator()) {
			print(cur);
		}
	}

	static void findBypublishedYearBetween(MongoCollection<Document> c, int a, int b) {
		int lo = Math.min(a, b), hi = Math.max(a, b);
		try (MongoCursor<Document> cur = c.find(Filters.and(Filters.gte("publishedYear", lo), Filters.lte("publishedYear", hi)))
				.limit(PAGE_SIZE)
				.projection(Projections.include("_id", "title", "publishedYear"))
				.iterator()) {
			print(cur);
		}
	}

	static void findByTitleContains(MongoCollection<Document> c, String key) {
		Document regex = new Document("$regex", key).append("$options", "i");
		try (MongoCursor<Document> cur = c.find(new Document("title", regex))
				.limit(PAGE_SIZE)
				.projection(Projections.include("_id", "title"))
				.iterator()) {
			print(cur);
		}
	}

	static void findByPriceBetween(MongoCollection<Document> c, double a, double b) {
		double lo = Math.min(a, b), hi = Math.max(a, b);
		try (MongoCursor<Document> cur = c.find(Filters.and(Filters.gte("price", lo), Filters.lte("price", hi)))
				.limit(PAGE_SIZE)
				.projection(Projections.include("_id", "title", "price"))
				.iterator()) {
			print(cur);
		}
	}

	static void countVariants(MongoCollection<Document> c, Random rnd) {
		if (rnd.nextBoolean()) {
			c.countDocuments(Filters.eq("publishedYear", rndpublishedYear(rnd)));
		} else {
			double a = rndPrice(rnd), b = rndPrice(rnd);
			double lo = Math.min(a, b), hi = Math.max(a, b);
			c.countDocuments(Filters.and(Filters.gte("price", lo), Filters.lte("price", hi)));
		}
	}

	static void print(MongoCursor<Document> cur) {
		if (cur.hasNext()) System.out.println(cur.next().toJson());
	}

	static int rndpublishedYear(Random r) {
		return MIN_publishedYear + r.nextInt((MAX_publishedYear - MIN_publishedYear) + 1);
	}

	static double rndPrice(Random r) {
		return MIN_PRICE + r.nextDouble() * (MAX_PRICE - MIN_PRICE);
	}
}
