package org.littlewings.infinispan.distexec.protostream.entity;

public class Price {
    String isbn;
    int value;

    public static Price create(String isbn, int value) {
        Price price = new Price();

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
