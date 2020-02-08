package org.littlewings.infinispan.marshalling;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class Book {
    @ProtoField(number = 1, required = true)
    String isbn;

    @ProtoField(number = 2, required = true)
    String title;

    @ProtoField(number = 3, required = true, defaultValue = "0")
    int price;

    @ProtoFactory
    public static Book create(String isbn, String title, int price) {
        Book book = new Book();

        book.setIsbn(isbn);
        book.setTitle(title);
        book.setPrice(price);

        return book;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
