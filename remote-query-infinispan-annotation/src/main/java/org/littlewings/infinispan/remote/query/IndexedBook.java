package org.littlewings.infinispan.remote.query;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.descriptors.Type;

@Indexed
public class IndexedBook {
    @Keyword(sortable = true)
    @ProtoField(number = 1, type = Type.STRING)
    String isbn;

    // デフォルトのAnalyzerはstandard
    @Text(analyzer = "standard")
    @ProtoField(number = 2, type = Type.STRING)
    String title;

    @Basic(sortable = true)
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
