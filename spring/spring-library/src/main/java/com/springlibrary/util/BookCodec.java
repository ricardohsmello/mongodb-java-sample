package com.springlibrary.util;

import com.springlibrary.model.Book;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

public class BookCodec implements Codec<Book> {

    @Override
    public Book decode(BsonReader reader, DecoderContext decoderContext) {
        Book book = new Book();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            switch (reader.readName()) {
                case "_id"           -> book.setId(reader.readObjectId().toHexString());
                case "title"         -> book.setTitle(reader.readString());
                case "author"        -> book.setAuthor(reader.readString());
                case "isbn"          -> book.setIsbn(reader.readString());
                case "publishedYear" -> book.setPublishedYear(reader.readInt32());
                case "price"         -> book.setPrice(reader.readDouble());
                default              -> reader.skipValue();
            }
        }
        reader.readEndDocument();
        return book;
    }

    @Override
    public void encode(BsonWriter writer, Book book, EncoderContext encoderContext) {
        writer.writeStartDocument();
        if (book.getId() != null) {
            writer.writeObjectId("_id", new ObjectId(book.getId()));
        }
        writer.writeString("title", book.getTitle());
        writer.writeString("author", book.getAuthor());
        writer.writeString("isbn", book.getIsbn());
        writer.writeInt32("publishedYear", book.getPublishedYear());
        writer.writeDouble("price", book.getPrice());
        writer.writeEndDocument();
    }

    @Override
    public Class<Book> getEncoderClass() {
        return Book.class;
    }
}
