package org.littlewings.infinispan.distexec.protostream;

import java.util.stream.Collectors;

import org.infinispan.Cache;
import org.jboss.logging.Logger;
import org.littlewings.infinispan.distexec.protostream.entity.Price;
import org.littlewings.infinispan.distexec.protostream.entity.ProtoBook;
import org.littlewings.infinispan.distexec.protostream.entity.ProtoSummary;

public class StreamSummaryReturnOnlyProtoTask {
    Cache<String, ProtoBook> cache;

    public StreamSummaryReturnOnlyProtoTask(Cache<String, ProtoBook> cache) {
        this.cache = cache;
    }

    public ProtoSummary execute() {
        return cache
                .values()
                .stream()
                .map(book -> {
                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute, filter price] map %s", book.getIsbn());
                    return Price.create(book.getIsbn(), book.getPrice());
                })
                .collect(() ->
                        Collectors.reducing(
                                ProtoSummary.create(0),
                                p -> {
                                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute] collect %s", p.getIsbn());
                                    return ProtoSummary.create(p.getValue());
                                },
                                (s1, s2) -> {
                                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute] collect %d, %d", s1.getValue(), s2.getValue());
                                    return ProtoSummary.create(s1.getValue() + s2.getValue());
                                })
                );
    }

    public ProtoSummary execute(int greaterThanPrice) {
        return cache
                .values()
                .stream()
                .filter(book -> {
                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute, filter price] filter %s", book.getIsbn());
                    return book.getPrice() > greaterThanPrice;
                })
                .map(book -> {
                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute, filter price] map %s", book.getIsbn());
                    return Price.create(book.getIsbn(), book.getPrice());
                })
                .collect(() ->
                        Collectors.reducing(
                                ProtoSummary.create(0),
                                p -> {
                                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute, filter price] collect %s", p.getIsbn());
                                    return ProtoSummary.create(p.getValue());
                                },
                                (s1, s2) -> {
                                    Logger.getLogger(StreamSummaryReturnOnlyProtoTask.class).infof("[execute, filter price] collect %d, %d", s1.getValue(), s2.getValue());
                                    return ProtoSummary.create(s1.getValue() + s2.getValue());
                                })
                );
    }
}
