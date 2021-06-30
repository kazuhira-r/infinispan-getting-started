package org.littlewings.infinispan.distexec.protostream.entity;

import java.io.Serializable;

public class SerializableSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    int value;

    public static SerializableSummary create(int value) {
        SerializableSummary summary = new SerializableSummary();

        summary.setValue(value);

        return summary;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
