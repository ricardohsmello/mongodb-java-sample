package com.example.service;

import com.example.model.Book;
import com.example.model.Review;
import com.example.repository.ReviewRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MongoCollection<Document> collection;

    @Inject
	ReviewService(MongoDatabase db, ReviewRepository reviewRepository) {
        this.collection = db.getCollection("reviews");
        this.reviewRepository = reviewRepository;
    }

    public String create(Review review) {
        review.id = new ObjectId().toHexString();
        reviewRepository.persist(review);
        return review.id;
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

    private Review toReview(Document doc) {
        Review r = new Review();
        r.id = doc.get("_id").toString();
        r.text = doc.getString("text");
        r.rating  = doc.getDouble("rating");
        r.bookId = doc.getString("bookId");
        return r;
    }

}
