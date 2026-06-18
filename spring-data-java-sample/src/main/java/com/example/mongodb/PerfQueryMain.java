package com.example.mongodb;

import com.example.mongodb.model.Book;
import com.example.mongodb.util.BookCodec;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

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
		String uri = args.length > 0 ? args[0] : "mongodb+srv://ricardohsmello:devrel@cluster0.1nxmr8u.mongodb.net/?appName=ricardo-content";
		String dbName = args.length > 1 ? args[1] : "library";
		String coll = args.length > 2 ? args[2] : "books";

		try (MongoClient client = MongoClients.create(uri)) {
			MongoDatabase db = client.getDatabase(dbName);

//			CodecRegistry registry = CodecRegistries.fromRegistries(
//					MongoClientSettings.getDefaultCodecRegistry(),
//					CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
//			);

			CodecRegistry registry = CodecRegistries.fromRegistries(
					CodecRegistries.fromCodecs(new BookCodec()),
					MongoClientSettings.getDefaultCodecRegistry()
			);


			MongoCollection<Book> books = db.getCollection("books", Book.class).withCodecRegistry(registry);

			MongoCollection<Document> debug = db.getCollection("books");
			Document first = debug.find().first();
			System.out.println("Sample doc: " + (first != null ? first.toJson() : "COLLECTION EMPTY"));

			AggregateIterable<Book> aggregate = books.aggregate(
					List.of(
							match(gt("year", 2010)),
							limit(5)
					)
			);

			System.out.println("Aggregate");
			aggregate.forEach(System.out::println);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void findByPublishedYear(MongoCollection<Document> c, int publishedYear) {

		try (MongoCursor<Document> cur = c.find(eq("publishedYear", publishedYear))
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
			c.countDocuments(eq("publishedYear", rndpublishedYear(rnd)));
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
