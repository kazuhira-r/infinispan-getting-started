package org.littlewings.infinispan.remote.testcontainers;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class Book {
    @ProtoField(number = 1)
    String isbn;

    @ProtoField(number = 2)
    String title;

    @ProtoField(number = 3, defaultValue = "0")
    int price;

    @ProtoFactory
    public static Book create(String isbn, String title, int price) {
        Book book = new Book();

        book.isbn = isbn;
        book.title = title;
        book.price = price;

        return book;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }
}
