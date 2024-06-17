package org.littlewings.infinispan.remote.vectorsearch;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.api.annotations.indexing.Vector;
import org.infinispan.api.annotations.indexing.option.VectorSimilarity;
import org.infinispan.protostream.annotations.Proto;

@Proto
@Indexed
public record Movie(
        @Text
        String name,
        @Text
        String description,
        @Vector(dimension = 768, similarity = VectorSimilarity.L2)
        float[] descriptionVector,
        @Text
        String author,
        @Basic(sortable = true)
        int year
) {
}
