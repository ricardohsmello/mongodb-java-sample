package com.example.model;

import org.bson.codecs.pojo.annotations.BsonProperty;

public record AverageBookRating(
		@BsonProperty("_id") String bookId, double averageRating, int totalReviews
) {
}
