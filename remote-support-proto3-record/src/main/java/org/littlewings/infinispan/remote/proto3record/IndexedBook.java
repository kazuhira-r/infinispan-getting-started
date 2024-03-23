package org.littlewings.infinispan.remote.proto3record;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.protostream.annotations.Proto;

@Proto
@Indexed
public record IndexedBook(
        @Keyword(sortable = true)
        String isbn,
        @Text(analyzer = "standard")
        String title,
        @Basic(sortable = true)
        int price
) {
}
