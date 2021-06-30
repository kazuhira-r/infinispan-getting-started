package org.littlewings.infinispan.distexec.protostream.entity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class ProtoBook {
    @ProtoField(number = 1)
    String isbn;

    @ProtoField(number = 2)
    String title;

    @ProtoField(number = 3, defaultValue = "0")
    int price;

    @ProtoFactory
    public static ProtoBook create(String isbn, String title, int price) {
        ProtoBook book = new ProtoBook();

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
