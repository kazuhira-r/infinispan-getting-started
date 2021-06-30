package org.littlewings.infinispan.distexec.protostream.entity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class ProtoSummary {
    @ProtoField(number = 1, defaultValue = "0")
    int value;

    @ProtoFactory
    public static ProtoSummary create(int value) {
        ProtoSummary summary = new ProtoSummary();

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
