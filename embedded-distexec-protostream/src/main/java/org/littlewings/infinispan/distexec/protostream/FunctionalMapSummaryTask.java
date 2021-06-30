package org.littlewings.infinispan.distexec.protostream;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.infinispan.functional.FunctionalMap;
import org.infinispan.functional.Traversable;
import org.infinispan.functional.impl.FunctionalMapImpl;
import org.infinispan.functional.impl.ReadOnlyMapImpl;
import org.jboss.logging.Logger;
import org.littlewings.infinispan.distexec.protostream.entity.ProtoBook;
import org.littlewings.infinispan.distexec.protostream.entity.SerializablePrice;
import org.littlewings.infinispan.distexec.protostream.entity.Summary;

public class FunctionalMapSummaryTask implements Serializable {
    private static final long serialVersionUID = 1L;

    Logger logger = Logger.getLogger(FunctionalMapSummaryTask.class);

    transient Cache<String, ProtoBook> cache;

    public FunctionalMapSummaryTask(Cache<String, ProtoBook> cache) {
        this.cache = cache;
    }

    public Summary execute(Set<String> isbns) {
        FunctionalMapImpl<String, ProtoBook> functionalMap = FunctionalMapImpl.create(cache.getAdvancedCache());
        FunctionalMap.ReadOnlyMap<String, ProtoBook> readOnlyMap = ReadOnlyMapImpl.create(functionalMap);

        Traversable<SerializablePrice> prices = readOnlyMap.evalMany(isbns, view -> {
            String isbn = view.key();
            ProtoBook book = view.get();

            logger.infof("[execute] get %s", isbn);
            return SerializablePrice.create(view.key(), book.getPrice());
        });

        return prices
                .collect(
                        Collectors.reducing(
                                Summary.create(0),
                                p -> {
                                    logger.infof("[execute] collect %s", p.getIsbn());
                                    return Summary.create(p.getValue());
                                },
                                (s1, s2) -> {
                                    logger.infof("[execute] collect %d, %d", s1.getValue(), s2.getValue());
                                    return Summary.create(s1.getValue() + s2.getValue());
                                })
                );
    }
}
