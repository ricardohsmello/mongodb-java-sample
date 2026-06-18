package com.example.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "books")
public class Book {
	@BsonId
	public String id;
	public String title;
	public int pages;
	public int year;
}
