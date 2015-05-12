package org.littlewings.infinispan.jcache;

import java.io.Serializable;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

public class DoublingEntryProcessor implements EntryProcessor<String, Integer, Integer> {
    @Override
    public Integer process(MutableEntry<String, Integer> entry, Object... arguments) {
        return entry.getValue() * 2;
    }
}
