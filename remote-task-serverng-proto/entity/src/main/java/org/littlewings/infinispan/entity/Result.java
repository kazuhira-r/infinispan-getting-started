package org.littlewings.infinispan.entity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

public class Result {
    @ProtoField(number = 1, defaultValue = "0")
    int value;

    @ProtoFactory
    public static Result create(int value) {
        Result result = new Result();

        result.setValue(value);

        return result;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
