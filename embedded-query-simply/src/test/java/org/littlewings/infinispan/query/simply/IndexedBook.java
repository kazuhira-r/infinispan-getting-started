package org.littlewings.infinispan.query.simply;

import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.descriptors.Type;

@Indexed
public class IndexedBook {
    @Field(analyze = Analyze.NO)
    @ProtoField(number = 1, type = Type.STRING)
    String isbn;

    @Field(analyzer = @Analyzer(impl = JapaneseAnalyzer.class))
    @ProtoField(number = 2, type = Type.STRING)
    String title;

    @Field
    @ProtoField(number = 3, type = Type.INT32, defaultValue = "0")
    int price;

    @ProtoFactory
    public static IndexedBook create(String isbn, String title, int price) {
        IndexedBook book = new IndexedBook();

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
