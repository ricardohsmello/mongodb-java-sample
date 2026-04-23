package com.example.mongodb.util;

import com.example.mongodb.model.Book;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class BookCodec implements Codec<Book> {
	@Override
	public Book decode(BsonReader reader, DecoderContext decoderContext) {
		reader.readStartDocument();
		Book book = new Book();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			switch (reader.readName()) {
				case "title" -> book.setTitle(reader.readString());
				case "year"  -> book.setYear(reader.readInt32());
				case "pages" -> book.setPages(reader.readInt32());
				default      -> reader.skipValue();
			}
		}
		reader.readEndDocument();
		return book;
	}

	@Override
	public void encode(BsonWriter writer, Book book, EncoderContext encoderContext) {
		writer.writeStartDocument();
		writer.writeString("title", book.getTitle());
		writer.writeInt32("pages", book.getPages());
		writer.writeEndDocument();

	}

	@Override
	public Class<Book> getEncoderClass() {
		return Book.class;
	}
}
