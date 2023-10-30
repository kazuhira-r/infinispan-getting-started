package org.littlewings.infinispan.remote.otlp;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.descriptors.Type;

public class Book {
    @ProtoField(number = 1, type = Type.STRING)
    String isbn;

    @ProtoField(number = 2, type = Type.STRING)
    String title;

    @ProtoField(number = 3, type = Type.INT32, defaultValue = "0")
    int price;

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
