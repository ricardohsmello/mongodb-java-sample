package com.example.service;

import com.example.model.AverageBookRating;
import com.example.model.Review;
import com.example.repository.ReviewRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.Updates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class ReviewService {

    private static final int MAX_EMBEDDED_REVIEWS = 5;

    private final ReviewRepository reviewRepository;
    private final MongoCollection<Document> booksCollection;
    private final MongoCollection<Document> reviewsCollection;

    @Inject
    ReviewService(MongoDatabase db, ReviewRepository reviewRepository) {
        this.booksCollection = db.getCollection("books");
        this.reviewsCollection = db.getCollection("reviews");
        this.reviewRepository = reviewRepository;
    }

    public String create(Review review) {
        review.id = new ObjectId().toHexString();
        if (review.createdAt == null) {
            review.createdAt = Instant.now();
        }
        reviewRepository.persist(review);
        pushReviewToBook(review);
        return review.id;
    }

    public AverageBookRating getAverageBookRating(String bookId) {
        var aggregates = List.of(
                Aggregates.match(Filters.eq("bookId", bookId)),
                Aggregates.group(
                        "$bookId",
                        Accumulators.avg("averageRating", "$rating"),
                        Accumulators.sum("totalReviews", 1)
                )

        );

        return reviewsCollection.aggregate(
                aggregates, AverageBookRating.class
        ).first();
    }

    public List<Review> findAll() {
        return reviewRepository.listAll();
    }

    public boolean update(String id, Review review) {
        review.id = id;
        return reviewRepository.updateReview(review);
    }

    public boolean delete(String id) {
        return reviewRepository.deleteReview(id);
    }

    // Embeds the review in the book, keeping only the most recent MAX_EMBEDDED_REVIEWS.
    private void pushReviewToBook(Review review) {
        Document embedded = new Document()
                .append("_id", review.id)
                .append("text", review.text)
                .append("user", review.user)
                .append("rating", review.rating)
                .append("createdAt", Date.from(review.createdAt));

        booksCollection.updateOne(
                Filters.eq("_id", review.bookId),
                Updates.pushEach("reviews", List.of(embedded),
                        new PushOptions()
                                .sortDocument(new Document("createdAt", 1))
                                .slice(-MAX_EMBEDDED_REVIEWS))
        );
    }

}
