package org.littlewings.infinispan.session;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter implements Serializable {
    private static final long serialVersionUID = 1L;

    AtomicInteger value = new AtomicInteger();

    public int increment() {
        return value.incrementAndGet();
    }

    public int getValue() {
        return value.get();
    }
}
