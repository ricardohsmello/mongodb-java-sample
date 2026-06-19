package com.example.repository;

import com.example.model.Review;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReviewRepository implements PanacheMongoRepositoryBase<Review, String> {

    public boolean updateReview(Review review) {
        Review existing = findById(review.id);
        if (existing == null) {
            return false;
        }
        existing.text = review.text;
        existing.user = review.user;
        existing.rating = review.rating;
        persistOrUpdate(existing);

        return true;
    }

    public boolean deleteReview(String id) {
        Review existing = findById(id);
        if (existing == null) {
            return false;
        }
        delete(existing);
        return true;
    }

}