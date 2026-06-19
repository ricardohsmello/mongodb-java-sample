package com.example.model;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

@MongoEntity(collection = "authors")
public class Author extends PanacheMongoEntityBase {

    @BsonId
    public String id;
    public String name;
    public String nationality;
    public int birthYear;

    public static Author findByName(String name) {
        return find("name", name).firstResult();
    }

    public static List<Author> findByNationality(String nationality) {
        return list("nationality", nationality);
    }

}
