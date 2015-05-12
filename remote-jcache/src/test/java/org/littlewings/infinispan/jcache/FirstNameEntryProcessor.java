package org.littlewings.infinispan.jcache;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

public class FirstNameEntryProcessor implements EntryProcessor<String, Person, String> {
    @Override
    public String process(MutableEntry<String, Person> entry, Object... arguments) {
        return entry.getValue().firstName();
    }
}
