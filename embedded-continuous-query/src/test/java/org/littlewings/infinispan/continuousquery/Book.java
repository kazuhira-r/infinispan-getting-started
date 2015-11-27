package org.littlewings.infinispan.continuousquery;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private String isbn;

    private String title;

    private int price;

    private List<Tag> tags;

    public Book() {
    }

    public Book(String isbn, String title, int price, List<Tag> tags) {
        this.isbn = isbn;
        this.title = title;
        this.price = price;
        this.tags = tags;
    }

    public static Book create(String isbn, String title, int price, String... tags) {
        return new Book(isbn,
                title,
                price,
                Arrays.stream(tags).map(Tag::new).collect(Collectors.toList()));
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Book) {
            Book otherBook = (Book) other;
            return Objects.equals(isbn, otherBook.getIsbn()) &&
                    Objects.equals(title, otherBook.getTitle()) &&
                    Objects.equals(price, otherBook.getPrice()) &&
                    Objects.equals(tags, otherBook.getTags());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, title, price, tags);
    }

    @Override
    public String toString() {
        return String.format("Book[%s, %s, %d, %s]", isbn, title, price, tags);
    }
}
