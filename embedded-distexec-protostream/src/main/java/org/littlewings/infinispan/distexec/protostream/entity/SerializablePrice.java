package org.littlewings.infinispan.distexec.protostream.entity;

import java.io.Serializable;

public class SerializablePrice implements Serializable {
    private static final long serialVersionUID = 1L;

    String isbn;
    int value;

    public static SerializablePrice create(String isbn, int value) {
        SerializablePrice price = new SerializablePrice();

        price.setIsbn(isbn);
        price.setValue(value);

        return price;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
